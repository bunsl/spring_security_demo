package com.wang.demo.modules.system.role.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wang.demo.modules.system.role.entity.Role;
import com.wang.demo.modules.system.role.service.RoleService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author wangjianhua
 * @date 2021-03-24 14:10
 */
@RestController
@RequestMapping("role")
@Api(tags = "【系统管理】【角色管理】")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @GetMapping
    @ApiOperation(value = "分页查询",notes = "角色分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name="current", value="页码（默认第1页）", dataType = "int", example = "1", paramType="body"),
            @ApiImplicitParam(name="size", value="每页条数（默认每页10条）", dataType = "int", example = "10", paramType="body")
    })
    public IPage<Role> get(@RequestBody Page<Role> page){
        return roleService.findByPage(page);
    }

    @GetMapping("{id}")
    @ApiOperation(value = "【主键查询】", notes = "主键ID必填信息")
    public Role get(@ApiParam(name = "id", value = "角色主键", required = true, example = "1") @PathVariable int id)
    {
        return roleService.findById(id);
    }

    @PostMapping(produces = "application/json")
    @ApiOperation(value = "【角色添加】", notes = "name角色名称是必填项")
    public void save(@RequestBody Role entity)
    {
        roleService.insert(entity);
    }
}
