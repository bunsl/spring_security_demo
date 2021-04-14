package com.wang.demo.modules.system.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wang.demo.modules.system.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 系统管理中用户管理
 * @author wangjianhua
 * @date 2021-03-18 14:00
 */
public interface UserMapper extends BaseMapper<User> {
    /**
     * 根据用户名获得id
     * @param username 用户名
     * @return id
     */
    int findIdByUserName(String username);

    /**
     * 根据用户名获得用户信息
     * @param username 用户名
     * @return user实体
     */
    User findUserByUserName(String username);

    /**
     * 根据用户id获取用户拥有的角色
     * @param id id
     * @return 角色集合
     */
    List<String> findUserRolesByUserId(int id);

    /**
     * 根据用户id获得授权码集合 用来判断是否有对应接口权限
     * @param id id
     * @return 权限码集合
     */
    List<String> findAuthorityCodeByUserId(int id);

    /**
     * 自定义的获取用户列表
     * @param end 结束
     * @param start 开始
     * @return 结果
     */
    List<User> selectListByPage(int start,int end);
}
