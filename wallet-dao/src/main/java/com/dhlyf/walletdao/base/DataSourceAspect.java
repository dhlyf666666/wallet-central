package com.dhlyf.walletdao.base;

import com.baomidou.dynamic.datasource.toolkit.DynamicDataSourceContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
@Slf4j
public class DataSourceAspect {

    @Before("execution(* com.dhlyf.exchangedao.mybatis.mapper.exchange..*.*(..))")
    public void switchToExchangeDb() {
        log.info("切换到exchange数据库");
        DynamicDataSourceContextHolder.push("exchange");
    }

    @AfterReturning("execution(* com.dhlyf.exchangedao.mybatis.mapper.exchange..*.*(..))")
    public void clearDataSource() {
        DynamicDataSourceContextHolder.clear();
    }

    // 新增用于future数据库的切面
    @Before("execution(* com.dhlyf.exchangedao.mybatis.mapper.future..*.*(..))")
    public void switchToFutureDb() {
        log.info("切换到future数据库");
        DynamicDataSourceContextHolder.push("future");
    }

    @AfterReturning("execution(* com.dhlyf.exchangedao.mybatis.mapper.future..*.*(..))")
    public void clearFutureDataSource() {
        DynamicDataSourceContextHolder.clear();
    }
}
