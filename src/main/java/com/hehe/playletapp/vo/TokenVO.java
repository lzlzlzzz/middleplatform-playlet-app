package com.hehe.playletapp.vo;

import lombok.Data;

@Data
public class TokenVO {

    public Long id;
    public String name;
    public Long deptId;
    public Long partId;
    public Integer permissionLevel;
    public Integer permissionGroup;
}
