package com.forgestorm.server.database;

import lombok.Getter;

@Getter
public class AuthenticatedUser {

    private final String ip;
    private final int databaseUserId;
    private final String xfAccountName;
    private final boolean isAdmin;
    private final boolean isModerator;

    public AuthenticatedUser(final String ip, final int databaseUserId, final String xfAccountName, final boolean isAdmin, final boolean isModerator) {
        this.ip = ip;
        this.databaseUserId = databaseUserId;
        this.xfAccountName = xfAccountName;
        this.isAdmin = isAdmin;
        this.isModerator = isModerator;
    }

}