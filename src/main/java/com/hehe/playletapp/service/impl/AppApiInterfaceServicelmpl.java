package com.hehe.playletapp.service.impl;

import com.hehe.playletapp.dao.AppApiInterfaceMapper;
import com.hehe.playletapp.entity.AppApiInterface;
import com.hehe.playletapp.service.AppApiInterfaceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppApiInterfaceServicelmpl extends BaseServiceImpl<AppApiInterfaceMapper, AppApiInterface> implements AppApiInterfaceService {

        private static final Logger logger = LoggerFactory.getLogger(AppApiInterfaceServicelmpl.class);

        @Override
        public List<AppApiInterface> getInfo(String path,String type){
            return baseMapper.getInfo(path,type);
        }

}
