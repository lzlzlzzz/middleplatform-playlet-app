package com.hehe.playletapp.service;

import com.hehe.playletapp.entity.AppApiInterface;

import java.util.List;

public interface AppApiInterfaceService extends IBaseService<AppApiInterface>{

    List<AppApiInterface> getInfo(String path, String type);

}
