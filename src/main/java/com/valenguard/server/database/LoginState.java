package com.valenguard.server.database;

import lombok.Getter;

@Getter
public class LoginState {

    private Integer userId;
    private Boolean loginSuccess;
    private String failReason;
    private String username;

    LoginState failState(String failReason) {
        this.failReason = failReason;
        loginSuccess = false;
        return this;
    }

    LoginState successState(int userId, String username) {
        this.userId = userId;
        loginSuccess = true;
        this.username = username;
        return this;
    }

}
