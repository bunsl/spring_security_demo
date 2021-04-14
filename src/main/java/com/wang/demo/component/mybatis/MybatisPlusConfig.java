package com.wang.demo.component.mybatis;

import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.wang.demo.component.mybatis.interceptor.UpdateInterceptor;
import com.wang.demo.component.security.jwt.JsonWebToken;
import com.wang.demo.modules.system.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * mybatisPlus配置
 * @author wangjianhua
 * @date 2021-03-18 15:30
 */
@Configuration
@EnableTransactionManagement
public class MybatisPlusConfig {
    /**
     * 启用mybatis分页支持
     * @return mybatis分页拦截器
     */
    @Bean
    public PaginationInterceptor paginationInterceptor(){
        return new PaginationInterceptor();
    }

    /**
     * 更新拦截器 启用它可以在更新时自动插入字段
     * @return update 更新拦截器
     */
    @Bean
    ConfigurationCustomizer mybatisConfigurationCustomizer(@Autowired UserService userService, @Autowired JsonWebToken jsonWebToken){
        return new ConfigurationCustomizer() {
            @Override
            public void customize(org.apache.ibatis.session.Configuration configuration) {
                configuration.addInterceptor(new UpdateInterceptor(userService, jsonWebToken));
            }
        };
    }
}
