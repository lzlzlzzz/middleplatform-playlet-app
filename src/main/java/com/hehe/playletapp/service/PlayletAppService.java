package com.hehe.playletapp.service;

import com.hehe.playletapp.entity.PlayletApp;

public interface PlayletAppService extends IBaseService<PlayletApp> {

    PlayletApp getIntegralAppByAppId(String appId);
}
