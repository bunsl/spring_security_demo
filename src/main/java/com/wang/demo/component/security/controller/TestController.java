package com.wang.demo.component.security.controller;

import com.wang.demo.base.response.ResultMessage;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试单接口用户权限
 * @author wangjianhua
 * @date 2021-04-06 11:01
 */
@RestController
public class TestController {

    /**
     * @PreAuthorize("hasRole('ROLE_GUEST')") 该方法使用了ROLE前缀对于角色的认证
     * 这是判断用户是否有某角色的权限控制 ROLE前缀的角色权限跟接口权限一样 保存在authorities中
     * @return 统一的返回值
     */
    @GetMapping("testrole")
    @PreAuthorize("hasRole('ROLE_GUEST')")
    public ResultMessage testRole(){
        return ResultMessage.success("测试用户角色访问成功");
    }

    /**
     * @PreAuthorize("hasAuthority('testSecurity')") 该方法使用了单接口的权限认证
     * 这是判断该用户是否拥有某接口的权限控制 这个权限字符同样保存在authorities中
     * @return 统一的返回值
     */
    @GetMapping("testauthority")
    @PreAuthorize("hasAuthority('testSecurity')")
    public ResultMessage testAuthority(){
        return ResultMessage.success("测试接口权限访问成功");
    }

}
