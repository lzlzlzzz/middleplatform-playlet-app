package com.hehe.playletapp.service;

import io.jsonwebtoken.Claims;

public interface IJWTService {

     String createJWT(String id, long ttlMillis) throws Exception;

     Claims parseJWT(String jwt) throws Exception;
}
