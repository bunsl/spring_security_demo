package com.wang.demo.base.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @author wangjianhau
 * @Description 封装分页及其属性
 * @date 2021/3/17/017 21:00
 **/
@Data
@ApiModel(value = "BasePage<T>【分页对象及其属性】")
public class BasePage<T> {
    /**
     *封装分页参数 例如current size
     */
    @ApiModelProperty(value = "分页查询参数",dataType = "page")
     private Page<T> page;
    /**
     * 封装当前模块参数 name date等
     */
    @ApiModelProperty(value = "当前查询实体类",dataType = "entity")
    private T entity;
}
