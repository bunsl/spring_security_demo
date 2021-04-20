package com.wang.demo.modules.security.controller;

import com.wang.demo.base.response.ResultCode;
import com.wang.demo.base.response.ResultMessage;
import com.wang.demo.modules.security.service.TokenService;
import com.wang.demo.modules.system.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录验证的controller
 * @author wangjianhua
 * @date 2021-04-19 16:11
 */
@RestController
@RequestMapping("system")
public class LoginController {

    @Autowired
    private TokenService tokenService;

    @PostMapping("login")
    public ResultMessage login(@RequestBody User user){
        String name = user.getName();
        String pass = user.getPass();
        if(StringUtils.isEmpty(name) || StringUtils.isEmpty(pass)){
            return ResultMessage.failure(ResultCode.USER_PARAM_EMPTY);
        }
        //remember选项写死 可以在登录页面上传进来
        Map<String, Object> map;
        //异常处理也可以在继承userdetailsService的类中自定义
        // 前提是有一个继承了userdetails的实体类
        try{
          map  = tokenService.generateToken(name, pass, "1");
        }catch (Exception exception){
            //处理返回认证失败提示信息到客户端
            if(exception instanceof UsernameNotFoundException){
                //用户不存在
                return ResultMessage.failure(ResultCode.USER_NOT_EXIST);
            }
            else if(exception instanceof BadCredentialsException){
                System.err.println(exception.getMessage());
                //账户不存在或密码错误
                return ResultMessage.failure(ResultCode.USER_LOGIN_ERROR);
            }
            else if(exception.getCause() instanceof LockedException){
                //账户被锁定
                return ResultMessage.failure(ResultCode.USER_ACCOUNT_LOCKED);
            }
            else if(exception.getCause() instanceof DisabledException){
                //账户被禁用
                return ResultMessage.failure(ResultCode.USER_ACCOUNT_FORBIDDEN);
            }
            else{
                //其他的登录失败 可以在后续完善
                return ResultMessage.failure(ResultCode.USER_LOGIN_OTHER_ERROR);
            }
        }

        return ResultMessage.success(ResultCode.LOGIN_SUCCESS,map);
    }
}
