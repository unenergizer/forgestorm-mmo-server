package com.valenguard.server.game.mysql;

import lombok.Getter;

@Getter
public class AuthenticatedUser {

    private final String ip;
    private final int databaseUserId;
    private String username;

    public AuthenticatedUser(String ip, int databaseUserId, String username) {
        this.ip = ip;
        this.databaseUserId = databaseUserId;
        this.username = username;
    }

}
