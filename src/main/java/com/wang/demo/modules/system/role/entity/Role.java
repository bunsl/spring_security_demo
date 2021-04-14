package com.wang.demo.modules.system.role.entity;

import com.wang.demo.base.entity.Base;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色
 * @author wangjianhua
 * @date 2021-03-24 13:55
 */
@Getter
@Setter
public class Role extends Base {

    @ApiModelProperty(value = "用户名称",dataType = "string")
    private String name;
}
