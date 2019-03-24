package com.valenguard.server.database;

import lombok.Getter;

@Getter
public class AuthenticatedUser {

    private final String ip;
    private final int databaseUserId;
    private final String username;

    public AuthenticatedUser(final String ip, final int databaseUserId, final String username) {
        this.ip = ip;
        this.databaseUserId = databaseUserId;
        this.username = username;
    }

}
