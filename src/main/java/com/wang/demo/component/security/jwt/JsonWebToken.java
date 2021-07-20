package com.wang.demo.component.security.jwt;

import com.wang.demo.component.redis.RedisComponent;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * jwt工具类
 * @author wangjianhua
 * @date 2021-03-17 17:48
 */
@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "spring.security.jwt")
public class JsonWebToken {

    private String header;

    private String prefix;

    private String secret;
    /**
     * 颁发者身份标识
     */
    private String issuer;
    /**
     * 过期时间
     */
    private long expiration;
    /**
     * 记住我 过期时间为7天
     */
    private long remember;
    /**
     * claims权限标识符
     */
    private String claimsAuthorities;

    private RedisComponent redisComponent;

    public JsonWebToken(RedisComponent redisComponent) {
        this.redisComponent = redisComponent;
    }

    /**
     * 生成认证token
     * @param claims 权限标识
     * @param username 用户名
     * @param time 过期时间
     * @return token
     */
    public String createToken(Claims claims,String username,long time){
        return Jwts.builder()
                //.setHeaderParam("typ","JWT")
                //权限集合
            .setClaims(claims)
                //签发标识
            .setIssuer(issuer)
                //签发账号
            .setSubject(username)
                //签发时间
            .setIssuedAt(new Date())
                //签名秘钥
            .signWith(SignatureAlgorithm.HS256,secret)
                //过期时间
            .setExpiration(new Date(System.currentTimeMillis()+time*1000))
            .compact();
    }

    /**
     * 从token中获取用户名
     * @return
     */
    public String getUsername(String token){
        return getTokenClaims(token).getSubject();
    }

    /**
     *根据token获取签署认证信息
     * @param token
     * @return
     */
    public Claims getTokenClaims(String token){
        Claims claims;
        try{
            claims=Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
        }catch (ExpiredJwtException e){
            claims=e.getClaims();
        }
        return claims;
    }

    public Authentication getAuthenticationFromToken(HttpServletRequest request, HttpServletResponse response){
        Authentication authentication=null;
        String token = getToken(request);
        if(token != null){
            //请求令牌不能为空
            if(getAuthentication()==null){
                //claims在token中包含了用户自定义的信息(权限),账号，过期时间
                Claims claims = getTokenClaims(token);
                if(claims==null){
                    return  null;
                }
                String username = claims.getSubject();
                if(username==null){
                    return null;
                }
                //验证是否过期 并添加redis查询是否存在
                String redisToken =redisComponent.get(username).toString();
                if(StringUtils.isEmpty(redisToken)){
                    return null;
                }
                //如果token过期了 看是否保存了可以刷新的token 然后取出来
                //if(isExpiration(token)) {
                //    String refreshToken = request.getHeader("refresh_token");
                //
                //    if(StringUtils.isEmpty(refreshToken) || isExpiration(refreshToken))
                //    {
                //        return null;
                //    }
                //    //else if(!StringUtils.isEmpty(redisToken)){
                //    //    createToken(claims,username);
                //    //    System.out.println("进入测试语句====");
                //    //
                //    //}
                //    else
                //    {
                //        createToken(null,username, expiration);
                //        response.addHeader(header, token);
                //    }
                //}
                //从Claims中获取用户权限
                Object authors =claims.get("authorities");
                List<GrantedAuthority> authorities =new ArrayList<>();
                if(!StringUtils.isEmpty(authors)&&authors instanceof List){
                    for (Object object : (List)authors){
                        authorities.add(new SimpleGrantedAuthority((String) object));
                    }
                }
                authentication= new UsernamePasswordAuthenticationToken(username, null, authorities);
            }
            else {
                if(validateToken(token,getUsername(token))){
                    //如果上下文中Authentication非空 且令牌合法 直接返回当前登录认证信息
                    authentication = getAuthentication();
                }
            }

        }
        return authentication;
    }


    /**
     * 从http请求中获取token
     * @param request 请求
     * @return token令牌
     */
    public String getToken(HttpServletRequest request){
        String token = request.getHeader(header);
        if(StringUtils.isEmpty(token)){
            token=request.getHeader("token");
        }
        else if(token.contains(prefix)){
            token = token.substring(prefix.length());
        }
        if("".equals(token)){
            token=null;
        }
        return token;
    }

    /**
     * 获取当前登录信息
     * @return 认证对象
     */
    public Authentication getAuthentication(){
        if(StringUtils.isEmpty(SecurityContextHolder.getContext())){
            return null;
        }
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 验证令牌
     * @param token 令牌
     * @param username 账号名
     * @return 验证结果
     */
    public Boolean validateToken(String token,String username){
        String userName = getUsername(token);
        return (userName.equals(username) && !isExpiration(token));
    }

    /**
     * 验证token是否过期
     * @param token 令牌
     * @return boolean
     */
    public boolean isExpiration(String token){
        return getTokenClaims(token).getExpiration().before(new Date());
    }
}
