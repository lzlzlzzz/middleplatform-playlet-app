package com.hehe.playletapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hehe.playletapp.dao.PlayletAppMapper;
import com.hehe.playletapp.entity.PlayletApp;
import com.hehe.playletapp.service.PlayletAppService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class PlayletAppServiceImpl extends BaseServiceImpl<PlayletAppMapper, PlayletApp> implements PlayletAppService {

    @Override
    public PlayletApp getIntegralAppByAppId(String appId) {
        QueryWrapper<PlayletApp> queryWrapper = getQueryWrapper(null);
        if (StringUtils.isNotEmpty(appId)) {
            queryWrapper.eq("package_name", appId);
        }
        return getOne(queryWrapper);
    }

}
