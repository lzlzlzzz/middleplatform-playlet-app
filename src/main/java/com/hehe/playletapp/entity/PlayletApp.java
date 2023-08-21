package com.hehe.playletapp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.hehe.playletapp.entity.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@TableName("tb_playlet_app")
public class PlayletApp extends BaseEntity {

    @ApiModelProperty(value = "APP名称")
    @TableField("app_name")
    private String appName;

    @ApiModelProperty(value = "APP包名")
    @TableField("package_name")
    private String packageName;

    @ApiModelProperty(value = "response_key")
    @TableField("response_key")
    private String responseKey;

    @ApiModelProperty(value = "token_key")
    @TableField("token_key")
    private String tokenKey;
}
