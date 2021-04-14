package com.wang.demo.component.config;

import com.wang.demo.component.security.filter.AntiSqlInjectionFilter;
import com.wang.demo.component.security.filter.XssFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.DispatcherType;
import java.util.HashMap;
import java.util.Map;

/**
 * filter配置 xss攻击过滤与sql注入过滤
 * @author wangjianhua
 * @date 2021-03-24 18:00
 */
@Configuration
public class FilterConfig {
    /**
     * xss过滤开关 可以在配置文件中配置开启或关闭 默认开启
     */
    @Value("${xss.enabled}")
    private String enabled;

    /**
     * xss excludes用于配置不需要参数过滤的请求url;
     */
    @Value("${xss.excludes}")
    private String excludes;
    /**
     *xss 过滤文件或者资源 默认过滤所有 为 /* 也可以自定义
     */
    @Value("${xss.urlPatterns}")
    private String urlPatterns;

    /**
     * 将xss攻击过滤器加入过滤链
     * @return 过滤链
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Bean
    public FilterRegistrationBean xssFilterRegistration(){
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setDispatcherTypes(DispatcherType.REQUEST);
        registrationBean.setFilter(new XssFilter());
        registrationBean.addUrlPatterns(urlPatterns);
        //如果有自定义的urlPatterns请用下面的方式注册且用逗号分隔
        //registrationBean.addUrlPatterns(StringUtils.split(urlPatterns, ","));
        registrationBean.setName("xssFilter");
        registrationBean.setOrder(FilterRegistrationBean.HIGHEST_PRECEDENCE);
        Map<String,String> initParameters = new HashMap<>(16);
        initParameters.put("excludes",excludes);
        initParameters.put("enabled",enabled);
        registrationBean.setInitParameters(initParameters);
        return registrationBean;
    }

    /**
     * 将sql注入过滤器加入过滤链
     * todo 可以设置sql注入白名单等 尚未设置 与上面xss配置类似
     * @return 过滤链
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Bean
    public FilterRegistrationBean sqlFilterRegistration(){
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setDispatcherTypes(DispatcherType.REQUEST);
        registrationBean.setFilter(new AntiSqlInjectionFilter());
        registrationBean.setName("sqlFilter");
        Map<String,String> initParameters = new HashMap<>(16);
        registrationBean.addUrlPatterns("/*");
        initParameters.put("excludes","/login,/logout");
        initParameters.put("enabled","true");
        registrationBean.setInitParameters(initParameters);
        return registrationBean;
    }
}
