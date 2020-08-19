package com.upgrad.quora.service.common;

public class AuthTokenParser {

    public static String parseAuthToken(String authorization) {
        String[] authData = authorization.split("Bearer ");
        String accessToken = null;
        if (authorization.startsWith("Bearer ") == true) {
            accessToken = authData[1];
        } else {
            accessToken = authData[0];
        }
        return accessToken;
    }
}