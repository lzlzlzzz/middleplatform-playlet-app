package com.hehe.playletapp.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

public interface IBaseService<T> extends IService<T> {

    QueryWrapper getQueryWrapper(T entity);

}
