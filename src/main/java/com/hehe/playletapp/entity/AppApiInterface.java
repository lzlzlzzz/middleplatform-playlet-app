package com.hehe.playletapp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.hehe.playletapp.entity.base.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("产品-api接口映射表")
@TableName("tb_product_interface_mapping_params")
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AppApiInterface extends BaseEntity {

    @ApiModelProperty(value = "产品应用id")
    @TableField("package_name")
    private String packageName;

    @ApiModelProperty(value = "mappingId")
    @TableField("mapping_id")
    private Long mappingId;

    @ApiModelProperty(value = "接口Id")
    @TableField("api_interface_id")
    private String apiInterfaceId;

    @ApiModelProperty(value = "新生成参数")
    @TableField("product_mapping_alias")
    private String productMappingAlias;


    @ApiModelProperty(value = "接口Id")
    @TableField("mapping_type")
    private String mappingType;

    @ApiModelProperty(value = "mappingKey")
    @TableField(exist = false)
    private String mappingKey;

    @ApiModelProperty(value = "请求路径")
    @TableField(exist = false)
    private String requestPath;

}
