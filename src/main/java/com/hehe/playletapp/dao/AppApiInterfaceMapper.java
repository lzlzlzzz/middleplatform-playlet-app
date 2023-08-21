package com.hehe.playletapp.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hehe.playletapp.entity.AppApiInterface;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AppApiInterfaceMapper extends BaseMapper<AppApiInterface> {

    List<AppApiInterface> getInfo(@Param("path") String path,@Param("type") String type);

    List<AppApiInterface> getParam(@Param("path") String path);

}
