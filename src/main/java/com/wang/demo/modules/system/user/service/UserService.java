package com.wang.demo.modules.system.user.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wang.demo.base.entity.BasePage;
import com.wang.demo.modules.system.user.entity.User;
import com.wang.demo.modules.system.user.mapper.UserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * @author wangjianhua
 * @date 2021-03-18 14:22
 */
@Service
@Transactional(readOnly = true)
public class UserService extends ServiceImpl<UserMapper, User> {


    /**
     * 分页查询
     * @param basePage 查询条件集
     * @return 分页结果
     */
    public IPage<User> find(BasePage<User> basePage){
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        User user = basePage.getEntity();
        if(!StringUtils.isEmpty(user.getName())){
            queryWrapper.like("name",user.getName());
        }
        if(!StringUtils.isEmpty(user.getNick())){
            queryWrapper.like("nick",user.getNick());
        }
        Page<User> userPage = baseMapper.selectPage(basePage.getPage(), queryWrapper);
        for (User user1 : userPage.getRecords()) {
            user1.setPass(null);
        }
        return userPage;


    }

    /**
     * 用户新增
     * @param entity 用户实体类
     * @return 结果
     */
    @Transactional(readOnly = false)
    public int saveUser(User entity){
        /*
            密码强加密 否则密码无法通过过滤器
         */
        entity.setPass(new BCryptPasswordEncoder().encode(entity.getPass()));
        return baseMapper.insert(entity);
    }

    /**
     * 根据主键id删除用户
     * @param id id
     */
    @Transactional(readOnly = false)
    public void deleteById(int id){
        baseMapper.deleteById(id);
    }

    /**
     * 根据主键获取用户
     * @param id 主键
     * @return 用户
     */
    public User findById(int id){
        return baseMapper.selectById(id);
    }

    /**
     * 根据用户名获得用户
     * @param username 用户名
     * @return 用户
     */
    public User findUserByUserName(String username){
        return baseMapper.findUserByUserName(username);
    }

    /**
     * 根据用户名获取主键
     * @param username 用户名
     * @return 主键
     */
    public int findIdByUserName(String username){
        return baseMapper.findIdByUserName(username);
    }

    /**
     * 根据用户主键获取用户拥有的角色
     * @param id 主键
     * @return 角色集合
     */
    public List<String> findUserRolesByUserId(int id){
        return baseMapper.findUserRolesByUserId(id);
    }

    /**
     * 根据用户主键获得拥有的权限码
     * @param id 用户主键
     * @return 权限码集合
     */
    public List<String> findAuthorityCodeByUserId(int id){
        return baseMapper.findAuthorityCodeByUserId(id);
    }
}
