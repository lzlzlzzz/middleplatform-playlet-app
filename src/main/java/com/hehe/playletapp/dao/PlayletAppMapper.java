package com.hehe.playletapp.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hehe.playletapp.entity.PlayletApp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PlayletAppMapper extends BaseMapper<PlayletApp> {

    PlayletApp getAppByPackageName(@Param("packageName") String packageName);
}
