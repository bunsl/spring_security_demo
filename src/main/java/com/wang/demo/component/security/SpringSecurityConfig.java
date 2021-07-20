package com.wang.demo.component.security;

import com.wang.demo.base.response.ResultCode;
import com.wang.demo.base.response.ResultMessage;
import com.wang.demo.component.redis.RedisComponent;
import com.wang.demo.component.security.filter.JwtAuthenticationFilter;
import com.wang.demo.component.security.filter.JwtAuthorizationFilter;
import com.wang.demo.component.security.jwt.JsonWebToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


/**
 * SpringSecurity配置类
 * 开启单接口权限控制
 * @author wangjianhua
 * @date 2021-03-18 16:34
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    private JsonWebToken jsonWebToken;

    /**
     * 注入该类 使用@Qualifier注解是为了指定实现类
     */
    @Autowired
    @Qualifier("SecurityUserDetailService")
    private UserDetailsService userDetailsService;

    @Autowired
    private RedisComponent redisComponent;

    /**
     * 强密码加密 无解密方法 只能通过服务解密
     * @return BCryptPasswordEncoder
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /**
     * 自定义处理提供器并加载到ioc中 提供器暂不使用 可以注入authenticationManagerBean();
     * @return DaoAuthenticationProvider
     */
//    @Bean
//    public DaoAuthenticationProvider authenticationProvider(){
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setHideUserNotFoundExceptions(false);
//        provider.setUserDetailsService(userDetailsService);
//        provider.setPasswordEncoder(bCryptPasswordEncoder());
//        return provider;
//    }

    /**
     * 认证管理构造器使用上面的提供器
     * 提供器暂不使用 这样可以注入authenticationManagerBean();
     * @param authenticationManagerBuilder 认证管理构造器
     * @throws Exception 异常
     */
    @Override
    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(bCryptPasswordEncoder());
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    /**
     * 请求认证配置
     * @param http http安全
     * @throws Exception 异常
     */
    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.cors();
        /*
         * 关闭跨站点请求伪造(csrf)
         */
        http.csrf().disable();
        /*
         * spring security会话管理
         * SessionCreationPolicy.ALWAYS         一直创建HttpSession
         * SessionCreationPolicy.NEVER          Spring Security不会创建HttpSession，但如果它已经存在则使用HttpSession
         * SessionCreationPolicy.IF_REQUIRED    Spring Security只会在需要时创建一个HttpSession
         * SessionCreationPolicy.STATELESS      Spring Security永远不会创建HttpSession，它不会使用HttpSession来获取SecurityContext
         * 使用token 就没有必要使用session
         */
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.authorizeRequests()
        //开放登录接口
        .antMatchers(HttpMethod.POST,"/system/login").permitAll()
        //Swagger index页面： https://localhost:8008/swagger-ui/index.html swagger未完善 请使用postman
        .antMatchers("/swagger-ui/**").permitAll()
        .antMatchers("/swagger-resources/**").permitAll()
        .antMatchers("/v2/api-docs").permitAll()
         //除了上面的接口不需要验证 剩余的接口都需要验证token
        .anyRequest().authenticated();
                /*
                 * authenticationEntryPoint 匿名用户访问无权限资源
                 * accessDeniedHandler 已认证用户访问无权限资源
                 * logoutSuccessHandler 处理退出功能
                 */
        http
                .exceptionHandling()
                .authenticationEntryPoint((request,response,exception) ->{
                    //用户未登录
                    ResultMessage.response(response,ResultMessage.failure(ResultCode.USER_NOT_LOGIN));
                })
                .accessDeniedHandler((request,response,exception)->{
                    //权限错误
                    ResultMessage.response(response,ResultMessage.failure(ResultCode.PERMISSION_NO_ACCESS));
                })
                .and()
                //todo 可以写后台的登录页面
                .logout()
                //这是后台的登出页面 可以自定义
                .logoutUrl("/logout")
                .logoutSuccessHandler((request,response,exception)->{
                    //登出需要token
                    Authentication authentication = jsonWebToken.getAuthenticationFromToken(request, response);
                    String username = authentication.getName();
                    System.err.println("当前操作人:"+username);
                    redisComponent.del(username);
                    ResultMessage.response(response,ResultMessage.success(ResultCode.USER_LOGOUT_SUCCESS));
                });
                /*
                 * Spring Security配置过滤器
                 * JWTAuthenticationFilter 登陆认证过滤器
                 * JWTAuthorizationFilter 权限授权过滤器
                 */
        http
//                .addFilter(new JwtAuthenticationFilter(jsonWebToken,authenticationManager(),redisComponent))
                .addFilter(new JwtAuthorizationFilter(authenticationManager(),jsonWebToken,redisComponent))
        ;

    }
}
