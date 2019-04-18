package com.valenguard.server.network.login;

import lombok.Getter;

@Getter
public class LoginState {

    private Integer userId;
    private Boolean loginSuccess;
    private String failReason;
    private String username;
    private boolean isAdmin;

    public LoginState failState(String failReason) {
        this.failReason = failReason;
        loginSuccess = false;
        return this;
    }

    public LoginState successState(int userId, String username, boolean isAdmin) {
        this.userId = userId;
        loginSuccess = true;
        this.username = username;
        this.isAdmin = isAdmin;
        return this;
    }

}
