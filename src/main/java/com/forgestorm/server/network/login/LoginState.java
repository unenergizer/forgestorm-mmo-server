package com.forgestorm.server.network.login;

import lombok.Getter;

@Getter
public class LoginState {

    private Integer userId;
    private Boolean loginSuccess;
    private LoginFailReason loginFailReason;
    private String username;
    private boolean isAdmin;
    private boolean isModerator;

    public LoginState failState(LoginFailReason loginFailReason) {
        this.loginFailReason = loginFailReason;
        loginSuccess = false;
        return this;
    }

    public LoginState successState(int userId, String username, boolean isAdmin, boolean isModerator) {
        this.userId = userId;
        loginSuccess = true;
        this.username = username;
        this.isAdmin = isAdmin;
        this.isModerator = isModerator;
        return this;
    }

}
