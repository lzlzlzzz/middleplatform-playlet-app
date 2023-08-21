package com.hehe.playletapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.hehe.playletapp.entity.base.BaseEntity;
import com.hehe.playletapp.service.IBaseService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;

public class BaseServiceImpl<M extends BaseMapper<T>, T extends BaseEntity> extends ServiceImpl<M, T> implements IBaseService<T> {
    @Override
    public QueryWrapper<T> getQueryWrapper(T entity) {
        QueryWrapper<T> wrapper = new QueryWrapper<>();
        if(entity != null) {
            return wrapper.setEntity(entity);
        }
        return wrapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateById(T entity) {
        entity.setUpdateTime(new Date());
        return SqlHelper.retBool(this.getBaseMapper().updateById(entity));
    }

    /**
     * 重写mp的基础方法
     * @param entity
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean save(T entity) {
        entity.setCreateTime(new Date(System.currentTimeMillis()));
        return SqlHelper.retBool(this.getBaseMapper().insert(entity));
    }

    @Override
    @Transactional
    public boolean saveBatch(Collection<T> entityList) {
        if (null != entityList && entityList.size() > 0) {
            for (T t : entityList) {
                t.setCreateTime(new Date(System.currentTimeMillis()));
            }
        }
        return saveBatch(entityList, 1000);
    }
}
