package com.valenguard.server.game.mysql;

import lombok.Getter;

@Getter
public class LoginState {

    private Integer userId;
    private Boolean loginSuccess;
    private String failReason;

    public LoginState failState(String failReason) {
        this.failReason = failReason;
        loginSuccess = false;
        return this;
    }

    public LoginState successState(int userId) {
        this.userId = userId;
        loginSuccess = true;
        return this;
    }

}
