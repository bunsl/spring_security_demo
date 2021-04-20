package com.wang.demo.modules.system.user.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wang.demo.base.entity.Base;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * User实体类
 * @author wangjianhua
 * @date 2021-03-18 14:01
 */
public class User extends Base {

    @NotNull(message = "昵称不能为空")
    @Size(max = 18,message = "用户昵称不能超过18个字符")
    @ApiModelProperty(value = "用户昵称",dataType = "string")
    private String nick;

    @NotNull(message = "昵称不能为空")
    @Size(max = 18,message = "用户昵称不能超过18个字符")
    @ApiModelProperty(value = "用户名称",dataType = "string")
    private String name;

    @NotNull(message = "密码不能为空")
    @Pattern(regexp = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,16}$",
            message = "密码6-16位，必须包含字母和数字")
    @ApiModelProperty(value = "用户密码",dataType = "string")
    private String pass;

    @ApiModelProperty(value = "用户状态(0:启用,1:禁用,2:锁定)", dataType = "int")
    private Integer state;
    /**
     * roles可以通过自定义查询获得 且roles已添加到userDetails中 所以roles不必展示
     */
    @TableField(exist = false)
    private List<String> roles;

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    @JsonProperty(value = "username")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty(value = "password")
    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "User{" +
                "nick='" + nick + '\'' +
                ", name='" + name + '\'' +
                ", pass='" + pass + '\'' +
                ", state=" + state +
                ", roles=" + roles +
                '}';
    }
}
