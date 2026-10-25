package com.xingyun;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * XingYun 星云在线教育平台 —— 后端启动类
 *
 * @author xingyun
 */
@SpringBootApplication
@MapperScan("com.xingyun.mapper") // 扫描 Mapper 接口，避免每个 Mapper 都写 @Mapper
public class XingyunApplication {

    public static void main(String[] args) {
        SpringApplication.run(XingyunApplication.class, args);
        System.out.println("\n" +
                "========================================\n" +
                "  星云在线教育平台后端启动成功！\n" +
                "  接口基础地址：http://localhost:8080/api\n" +
                "========================================\n");
    }
}
