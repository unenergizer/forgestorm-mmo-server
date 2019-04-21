package com.valenguard.server.database;

import lombok.Getter;

@Getter
public class AuthenticatedUser {

    private final String ip;
    private final int databaseUserId;
    private final String xfAccountName;
    private final boolean isAdmin;

    public AuthenticatedUser(final String ip, final int databaseUserId, final String xfAccountName, final boolean isAdmin) {
        this.ip = ip;
        this.databaseUserId = databaseUserId;
        this.xfAccountName = xfAccountName;
        this.isAdmin = isAdmin;
    }

}
