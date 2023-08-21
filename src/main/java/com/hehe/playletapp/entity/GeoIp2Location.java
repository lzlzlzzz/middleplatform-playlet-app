package com.hehe.playletapp.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import lombok.Data;

@ApiModel("")
@Data
public class GeoIp2Location {
private static final long serialVersionUID = 1L;

    @TableField("locale_code")
    private String localeCode;

    @TableField("continent_code")
    private String continentCode;

    @TableField("continent_name")
    private String continentName;

    @TableField("country_iso_code")
    private String countryIsoCode;

    @TableField("country_name")
    private String countryName;

    @TableField("is_in_european_union")
    private Integer isInEuropeanUnion;


}
