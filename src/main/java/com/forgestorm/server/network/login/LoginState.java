package com.forgestorm.server.network.login;

import lombok.Getter;

import java.util.List;

@Getter
public class LoginState {

    private Integer userId;
    private Boolean loginSuccess;
    private LoginFailReason loginFailReason;
    private String username;
    private List<Byte> secondaryGroupIds;
    private boolean isAdmin;
    private boolean isModerator;

    public LoginState failState(LoginFailReason loginFailReason) {
        this.loginFailReason = loginFailReason;
        loginSuccess = false;
        return this;
    }

    public LoginState successState(int userId, String username, List<Byte> secondaryGroupIds, boolean isAdmin, boolean isModerator) {
        this.userId = userId;
        loginSuccess = true;
        this.username = username;
        this.secondaryGroupIds = secondaryGroupIds;
        this.isAdmin = isAdmin;
        this.isModerator = isModerator;
        return this;
    }

}
