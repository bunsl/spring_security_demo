package com.wang.demo.base.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wangjianhua
 * @date 2021-02-26 17:29
 */
@Data
public class Base implements Serializable {

    private static final long serialVersionUID = 8925514045582235838L;

    @ApiModelProperty(value = "主键", dataType = "int")
    private int id;

    @TableField(fill = FieldFill.UPDATE)
    @ApiModelProperty(value = "添加人", dataType = "int")
    private int createdBy;

    @TableField(fill = FieldFill.UPDATE)
    @ApiModelProperty(value = "添加时间", dataType = "date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GTM+8")
    private Date createdTime;

    @ApiModelProperty(value = "修改人", dataType = "int")
    private int updatedBy;

    @ApiModelProperty(value = "修改时间", dataType = "date")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GTM+8")
    private Date updatedTime;
}
