package com.wang.demo.modules.security.service;

import com.wang.demo.component.redis.RedisComponent;
import com.wang.demo.component.security.jwt.JsonWebToken;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 自定义token创建逻辑
 * @author wangjianhua
 * @date 2021-04-19 16:22
 */
@Service
public class TokenService {
    private static Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Autowired
    private JsonWebToken jsonWebToken;
    /**
     * 认证管理对象
     */
    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisComponent redisComponent;


    /**
     * 生成token
     * @param name 名字
     * @param pass 密码
     * @param remember 记住我
     * @return 用于返回的map
     */
    public Map<String,Object> generateToken(String name, String pass, String remember){
        Map<String,Object> map = new HashMap<>(16);
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(name, pass, new ArrayList<>()));
        if(!authentication.isAuthenticated()){
            //验证失败
            return null;
        }
        //获取认证成功的账号相关信息
        String username = ((org.springframework.security.core.userdetails.User)authentication.getPrincipal()).getUsername();
        //获取认证成功的权限相关信息
        List<?> list = (List<?>)authentication.getAuthorities();
        logger.info("认证成功:"+list);
        Claims claims = Jwts.claims();
        claims.put("authorities",authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        String token = null;
        if("1".equals(remember)){
            token = jsonWebToken.createToken(claims,username,jsonWebToken.getRemember());
        }
        else {
            //普通token 过期时间 1小时
            token = jsonWebToken.createToken(claims,username,jsonWebToken.getExpiration());
        }
        redisComponent.set(username,token,3600);
        map.put("access_token",token);
        //可刷新token 过期时间12小时 方便开发
        map.put("refresh_token",jsonWebToken.createToken(claims,username,jsonWebToken.getExpiration()*12));
        return map;
    }
}
