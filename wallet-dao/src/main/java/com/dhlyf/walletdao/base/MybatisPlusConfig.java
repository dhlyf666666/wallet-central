package com.dhlyf.walletdao.base;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class MybatisPlusConfig {

    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        DynamicTableNameInnerInterceptor dynamicTableNameInnerInterceptor = new DynamicTableNameInnerInterceptor();
        // 设置动态表名处理逻辑
        dynamicTableNameInnerInterceptor.setTableNameHandler((sql, tableName) -> {

            if (tableName.startsWith("ex_order") || tableName.startsWith("ex_trade")) {

                // 获取当前线程的币对信息
                Map<String, Object> paramMap = MybatisRequestDataHelper.getRequestData();
                if(paramMap.get("symbol") != null){
                    return tableName + "_" + paramMap.get("symbol").toString();
                }
            }

            if (tableName.startsWith("co_order_e") || tableName.startsWith("co_trade_e")) {

                // 获取当前线程的币对信息
                Map<String, Object> paramMap = MybatisRequestDataHelper.getRequestData();
                if(paramMap.get("symbol") != null){
                    return tableName + "_" + paramMap.get("symbol").toString();
                }
            }

            return tableName;
        });
        interceptor.addInnerInterceptor(dynamicTableNameInnerInterceptor);
        return interceptor;
    }

    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusPageInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }

}
