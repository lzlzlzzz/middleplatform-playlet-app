package com.hehe.playletapp.response;

public enum AppStatus {

    /**
     *  app 端请求token 过期
     */
    APP_TOKEN_EXPIRED(40017,  "Access Token Expired."),

    /**
     *  无效 token 无法解密
     */
    APP_TOKEN_INVALID(40018,  "Access Token Invalid."),

    /**
     * 取token 提交数据有误
     */
    APP_GET_TOKEN_INVALID(40019,  "Get Token Data Invalid."),

    /**
     * 取token 提交数据 sign有误
     */
    APP_GET_TOKEN_SIGN_INVALID(40020,  "Get Token Data have Invalid sign."),

    /**
     * 取token 提交数据 time有误
     */
    APP_GET_TOKEN_TIME_INVALID(40021,  "Get Token Data have Invalid Time."),

    /**
     * 取token 包名异常，和现有包名不符
     */
    APP_GET_TOKEN_PACKAGENAME_INVALID(40022,  "Get Token Data have Invalid packageName."),

    /**
     * 请求头中token 为空
     */
    APP_TOKEN_EMPTY(40023,  "Empty Token."),

    /**
     * 取token post加密数据解密异常
     */
    APP_GET_TOKEN_DECRYPTED_FAILURE(40024,  "Get Token Post Data Decrypted Error."),

    /**
     * 取token post加密数据为空
     */
    APP_GET_TOKEN_DATA_EMPTY(40025,  "Get Token Post Data Empty."),

    /**
     * url路径错误
     */
    APP_GET_TOKEN_URL_INVALID(40026, "Get Token Data have Invalid url."),

    /**
     * 成功
     */
    SUCCESS(200,  "success"),

    /**
     * 服务器异常
     */
    SERVER_ERROR(500,  "server error");


    private final int code;

    private final String message;

    AppStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return this.code;
    }
    public String message() {
        return this.message;
    }
}
