package com.wang.demo.component.security.filter;

import com.wang.demo.component.redis.RedisComponent;
import com.wang.demo.component.security.jwt.JsonWebToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.util.StringUtils;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 权限认证过滤器
 * @author wangjianhua
 * @date 2021-03-18 16:18
 */
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    private JsonWebToken jsonWebToken;

    private RedisComponent redisComponent;

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager,JsonWebToken jsonWebToken, RedisComponent redisComponent) {
        super(authenticationManager);
        this.jsonWebToken = jsonWebToken;
        this.redisComponent = redisComponent;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        //先从redis中获取token
        //username可以根据前端自由写
//        String username =request.getParameter("username");
        String username;
        String token;
//        if(!StringUtils.isEmpty(redisComponent.get("Authority"))){
//            token = redisComponent.get(username).toString();
//        }
//        else {
            //从请求头中获取token
            token = request.getHeader(jsonWebToken.getHeader());
//        }
        //请求头没有Authorization直接放行
        if(StringUtils.isEmpty(token) || !token.startsWith(jsonWebToken.getPrefix())){
            chain.doFilter(request,response);
            return;
        }
        //有就解析token并设置认证信息
        SecurityContextHolder.getContext().setAuthentication(getAuthentication(token,request,response));
        super.doFilterInternal(request, response, chain);
    }

    /**
     * 账号密码认证token
     * 可以增加过滤器做其他验证及功能
     * @param token 请求携带token
     * @return 账号密码认证的token
     */
        private UsernamePasswordAuthenticationToken getAuthentication(String token,HttpServletRequest request,HttpServletResponse response){
            token = token.replace(jsonWebToken.getPrefix(),"");
            String username = jsonWebToken.getUsername(token);
            //这里解析了token并从其中拿到需要的信息
            if(!StringUtils.isEmpty(username)){
                return new UsernamePasswordAuthenticationToken(username,null,jsonWebToken.getAuthenticationFromToken(request,response).getAuthorities());
            }
            return null;
        }
}
