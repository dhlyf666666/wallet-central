package com.dhlyf.walletdao.base;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@ComponentScan(basePackages = "com.dhlyf.walletdao")
@Configuration
@MapperScan("com.dhlyf.walletdao.mybatis.mapper")
public class AutoConfiguration {
}
