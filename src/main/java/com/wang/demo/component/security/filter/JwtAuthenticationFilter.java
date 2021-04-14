package com.wang.demo.component.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wang.demo.base.response.ResultCode;
import com.wang.demo.base.response.ResultMessage;
import com.wang.demo.component.redis.RedisComponent;
import com.wang.demo.component.security.jwt.JsonWebToken;
import com.wang.demo.modules.system.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author wangjianhua
 * @date 2021-03-18 15:46
 */
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private JsonWebToken jsonWebToken;
    /**
     * 认证管理对象
     */
    private AuthenticationManager authenticationManager;

    private RedisComponent redisComponent;

    /**
     * 构造器注入
     * @param jsonWebToken jsonWebToken
     * @param authenticationManager 认证管理器对象
     * @param redisComponent redisComponent
     */
    public JwtAuthenticationFilter(JsonWebToken jsonWebToken, AuthenticationManager authenticationManager, RedisComponent redisComponent) {
        this.jsonWebToken = jsonWebToken;
        this.authenticationManager = authenticationManager;
        this.redisComponent = redisComponent;
    }

    /**
     * 收集客户端参数交由security完成认证
     * @param request 请求
     * @param response 响应
     * @return Authentication 认证
     * @throws AuthenticationException 异常
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try{
            //收集客户端的账号密码
            User user =  new ObjectMapper().readValue(request.getInputStream(), User.class);
            //交由security完成认证
            return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getName(),user.getPass(),new ArrayList<>()));
        }catch (IOException e){
            logger.error(e.getMessage());
            return null;
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {
        //获取认证成功账号相关信息
        String username = ((org.springframework.security.core.userdetails.User)authentication.getPrincipal()).getUsername();
        //获取认证成功账号权限信息
        List<?> list  = (List<?>) authentication.getAuthorities();
        System.out.println(list);
        Claims claims = Jwts.claims();
        claims.put("authorities",authentication.getAuthorities().stream().map(s -> s.getAuthority()).collect(Collectors.toList()));
        //生成认证成功的账号token
        String remember = request.getParameter("remember");
        String token = null;
        //如果用户没有勾选记住我
        if(StringUtils.isEmpty(remember)){
            token=jsonWebToken.createToken(claims,username,jsonWebToken.getExpiration());
        }
        //勾选记住我 给一个7天的token 可以在jsonWebToken里面改
        else if("1".equals(remember)){
            token = jsonWebToken.createToken(claims,username,jsonWebToken.getRemember());
        }
        response.addHeader(jsonWebToken.getHeader(),token);
        //向redis中写入token并添加过期时间1小时
        redisComponent.set(username,token,3600);
//        redisComponent.set("Authority",token,3600);
        Map<String,String> result = new HashMap<>(2);
        result.put("access_token",token);
        //可刷新的token为一天
        result.put("refresh_token",jsonWebToken.createToken(claims,username,jsonWebToken.getExpiration()*24));
        ResultMessage.response(response,ResultMessage.success(ResultCode.USER_LOGIN_SUCCESS,result));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        //处理返回认证失败提示信息到客户端
        if(exception instanceof UsernameNotFoundException){
            //用户不存在
            ResultMessage.response(response,ResultMessage.failure(ResultCode.USER_NOT_EXIST));
        }
        else if(exception instanceof BadCredentialsException){
            System.err.println(exception.getMessage());
            //账户不存在或密码错误
            ResultMessage.response(response,ResultMessage.failure(ResultCode.USER_LOGIN_ERROR));
        }
        else if(exception.getCause() instanceof LockedException){
            //账户被锁定
            ResultMessage.response(response,ResultMessage.failure(ResultCode.USER_ACCOUNT_LOCKED));
        }
        else if(exception.getCause() instanceof DisabledException){
            //账户被禁用
            ResultMessage.response(response,ResultMessage.failure(ResultCode.USER_ACCOUNT_FORBIDDEN));
        }
        else{
            //其他的登录失败 可以在后续完善
            ResultMessage.response(response,ResultMessage.failure(ResultCode.USER_LOGIN_OTHER_ERROR));
        }
    }
}
