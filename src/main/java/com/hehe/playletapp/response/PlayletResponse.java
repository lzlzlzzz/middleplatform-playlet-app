package com.hehe.playletapp.response;

import lombok.Data;

@Data
public class PlayletResponse<T> {

    private Integer code;
    private String msg;
    private T data;

    public PlayletResponse() {
    }

    public PlayletResponse(Integer code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }
}
