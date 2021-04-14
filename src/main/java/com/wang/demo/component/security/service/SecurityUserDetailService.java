package com.wang.demo.component.security.service;

import com.wang.demo.modules.system.user.entity.User;
import com.wang.demo.modules.system.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangjianhua
 * @Description 实现UserDetailsService类
 * 想要使用springsecurity 实现该类可以更加方便快捷实现验证功能
 * 还可以在这添加用户权限或者是做相应查询
 * @date 2021/3/17/017 20:48
 **/
@Service("SecurityUserDetailService")
public class SecurityUserDetailService implements UserDetailsService {

    @Autowired
    private UserService userService;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findUserByUserName(username);
        if(StringUtils.isEmpty(user)){
            throw new UsernameNotFoundException("该用户不存在");
        }
        else if(1==user.getState()){
            throw new DisabledException("该账户已禁用");
        }
        else if(2==user.getState()){
            throw  new LockedException("账号已锁定");
        }
        //查询用户权限
        List<GrantedAuthority> authorities = new ArrayList<>();
        //角色
        userService.findUserRolesByUserId(user.getId()).forEach(roles ->{
            authorities.add(new SimpleGrantedAuthority(roles));
        });
        //权限
        userService.findAuthorityCodeByUserId(user.getId()).forEach(authority ->{
            authorities.add(new SimpleGrantedAuthority(authority));
        });
        return new org.springframework.security.core.userdetails.User(user.getName(),user.getPass(),authorities);
    }
}
