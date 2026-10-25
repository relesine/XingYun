package com.xingyun.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyun.common.BusinessException;
import com.xingyun.dto.QuizSubmitDTO;
import com.xingyun.entity.Quiz;
import com.xingyun.entity.QuizAnswer;
import com.xingyun.entity.QuizQuestion;
import com.xingyun.entity.WrongQuestion;
import com.xingyun.mapper.QuizAnswerMapper;
import com.xingyun.mapper.QuizMapper;
import com.xingyun.mapper.QuizQuestionMapper;
import com.xingyun.mapper.WrongQuestionMapper;
import com.xingyun.vo.QuizResultVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 随堂测验服务：拉取题目、交卷自动判分、错题自动进入错题本。
 */
@Service
public class QuizService {

    @Resource
    private QuizMapper quizMapper;
    @Resource
    private QuizQuestionMapper questionMapper;
    @Resource
    private QuizAnswerMapper answerMapper;
    @Resource
    private WrongQuestionMapper wrongMapper;

    /**
     * 根据课时取测验（含题目，按 sort 排序）。
     * 【安全】下发题目时把「正确答案 answer」置空，防止学员 F12 看答案刷满分；
     * 判分完全在后端进行，前端拿不到标准答案。交卷后结果里才会返回每题对错与正确答案。
     */
    public Map<String, Object> getByLesson(Long lessonId) {
        Quiz quiz = quizMapper.selectOne(new LambdaQueryWrapper<Quiz>()
                .eq(Quiz::getLessonId, lessonId).eq(Quiz::getStatus, 1).last("limit 1"));
        Map<String, Object> map = new HashMap<>();
        if (quiz == null) {
            map.put("quiz", null);
            map.put("questions", Collections.emptyList());
            return map;
        }
        List<QuizQuestion> questions = questionMapper.selectList(new LambdaQueryWrapper<QuizQuestion>()
                .eq(QuizQuestion::getQuizId, quiz.getId()).orderByAsc(QuizQuestion::getSort));
        // 拷贝一份安全的题目列表：隐藏 answer 字段（解析 analysis 保留，交卷后用于学习反馈）
        List<Map<String, Object>> safeQuestions = new ArrayList<>(questions.size());
        for (QuizQuestion q : questions) {
            Map<String, Object> m = new HashMap<>();
            m.put("id", q.getId());
            m.put("quizId", q.getQuizId());
            m.put("questionType", q.getQuestionType());
            m.put("content", q.getContent());
            m.put("optionA", q.getOptionA());
            m.put("optionB", q.getOptionB());
            m.put("optionC", q.getOptionC());
            m.put("optionD", q.getOptionD());
            m.put("score", q.getScore());
            m.put("analysis", q.getAnalysis());
            m.put("sort", q.getSort());
            safeQuestions.add(m);
        }
        map.put("quiz", quiz);
        map.put("questions", safeQuestions);
        return map;
    }

    public Quiz getById(Long id) {
        return quizMapper.selectById(id);
    }

    /**
     * 提交测验：后端逐题判分（绝不以前端传入的对错为准，防止刷分）
     */
    @Transactional(rollbackFor = Exception.class)
    public QuizResultVO submit(QuizSubmitDTO dto, Long userId) {
        Quiz quiz = quizMapper.selectById(dto.getQuizId());
        if (quiz == null) {
            throw new BusinessException("测验不存在");
        }
        List<QuizQuestion> questions = questionMapper.selectList(new LambdaQueryWrapper<QuizQuestion>()
                .eq(QuizQuestion::getQuizId, quiz.getId()));
        Map<Long, QuizQuestion> qMap = questions.stream()
                .collect(Collectors.toMap(QuizQuestion::getId, q -> q));
        Map<Long, String> userAnswerMap = new HashMap<>();
        if (dto.getAnswers() != null) {
            for (QuizSubmitDTO.AnswerItem item : dpt(dto.getAnswers())) {
                userAnswerMap.put(item.getQuestionId(), item.getUserAnswer());
            }
        }

        QuizResultVO result = new QuizResultVO();
        result.setQuizId(quiz.getId());
        result.setQuizName(quiz.getQuizName());
        result.setTotalScore(quiz.getTotalScore());
        result.setPassScore(quiz.getPassScore());
        result.setQuestionCount(questions.size());

        List<QuizResultVO.ItemDetail> details = new ArrayList<>();
        int userScore = 0;
        int rightCount = 0;
        for (QuizQuestion q : questions) {
            String userAnswer = userAnswerMap.getOrDefault(q.getId(), "");
            // 判断题兼容：前端可能提交 A/B（选项顺序）或 TRUE/FALSE，统一归一化为 TRUE/FALSE 再判分
            if (q.getQuestionType() != null && q.getQuestionType() == 3) {
                userAnswer = normalizeJudge(userAnswer);
            }
            boolean right = isAnswerRight(q.getAnswer(), userAnswer);
            int gain = right ? q.getScore() : 0;
            userScore += gain;
            if (right) {
                rightCount++;
            }

            QuizAnswer ans = new QuizAnswer();
            ans.setQuizId(quiz.getId());
            ans.setUserId(userId);
            ans.setQuestionId(q.getId());
            ans.setUserAnswer(userAnswer);
            ans.setIsRight(right ? 1 : 0);
            ans.setScore(gain);
            answerMapper.insert(ans);

            // 错题本：答错则新增/累加错误次数；答对则移出错题本
            handleWrongQuestion(userId, quiz.getId(), q.getId(), right);

            QuizResultVO.ItemDetail detail = new QuizResultVO.ItemDetail();
            detail.setQuestionId(q.getId());
            detail.setUserAnswer(userAnswer);
            detail.setRightAnswer(q.getAnswer());
            detail.setRight(right);
            detail.setScore(gain);
            details.add(detail);
        }
        result.setDetails(details);
        result.setUserScore(userScore);
        result.setRightCount(rightCount);
        result.setPassed(userScore >= quiz.getPassScore());
        return result;
    }

    /** 我的错题本 */
    public List<Map<String, Object>> wrongBook(Long userId) {
        List<WrongQuestion> wrongs = wrongMapper.selectList(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getUserId, userId)
                .orderByDesc(WrongQuestion::getLastWrongTime));
        if (wrongs.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> qIds = wrongs.stream().map(WrongQuestion::getQuestionId).collect(Collectors.toList());
        Map<Long, QuizQuestion> qMap = questionMapper.selectBatchIds(qIds).stream()
                .collect(Collectors.toMap(QuizQuestion::getId, q -> q));
        List<Map<String, Object>> list = new ArrayList<>();
        for (WrongQuestion w : wrongs) {
            Map<String, Object> m = new HashMap<>();
            m.put("wrong", w);
            m.put("question", qMap.get(w.getQuestionId()));
            list.add(m);
        }
        return list;
    }

    /** 答案是否正确：统一大写去空格，多选题将选项字母排序后比较，避免“AB/BA”误判 */
    private boolean isAnswerRight(String right, String user) {
        if (right == null || user == null) {
            return false;
        }
        String r = normalize(right);
        String u = normalize(user);
        if (r.length() > 1) {
            char[] rc = r.toCharArray();
            char[] uc = u.toCharArray();
            Arrays.sort(rc);
            Arrays.sort(uc);
            return new String(rc).equals(new String(uc));
        }
        return r.equals(u);
    }

    private String normalize(String s) {
        return s.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 判断题用户答案归一化。题库正确答案存 TRUE/FALSE，
     * 但前端选项是「A.正确 B.错误」，学员提交可能是 A/B、TRUE/FALSE、中文对错，统一转成 TRUE/FALSE。
     */
    private String normalizeJudge(String s) {
        if (s == null) {
            return "";
        }
        String v = s.trim().toUpperCase(Locale.ROOT);
        if (v.equals("A") || v.equals("TRUE") || v.equals("T") || v.equals("Y")
                || v.contains("正确") || v.contains("对")) {
            return "TRUE";
        }
        if (v.equals("B") || v.equals("FALSE") || v.equals("F") || v.equals("N")
                || v.contains("错误") || v.contains("错")) {
            return "FALSE";
        }
        return v;
    }

    private void handleWrongQuestion(Long userId, Long quizId, Long questionId, boolean right) {
        WrongQuestion exist = wrongMapper.selectOne(new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getUserId, userId).eq(WrongQuestion::getQuestionId, questionId));
        if (right) {
            // 重做正确，移出错题本
            if (exist != null) {
                wrongMapper.deleteById(exist.getId());
            }
            return;
        }
        if (exist == null) {
            WrongQuestion w = new WrongQuestion();
            w.setUserId(userId);
            w.setQuizId(quizId);
            w.setQuestionId(questionId);
            w.setWrongCount(1);
            w.setLastWrongTime(new Date());
            wrongMapper.insert(w);
        } else {
            exist.setWrongCount(exist.getWrongCount() + 1);
            exist.setLastWrongTime(new Date());
            wrongMapper.updateById(exist);
        }
    }

    private List<QuizSubmitDTO.AnswerItem> dpt(List<QuizSubmitDTO.AnswerItem> list) {
        return list == null ? Collections.emptyList() : list;
    }
}
