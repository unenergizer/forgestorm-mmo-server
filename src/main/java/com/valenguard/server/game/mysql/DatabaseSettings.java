package com.valenguard.server.game.mysql;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DatabaseSettings {
    private String ip;
    private int port;
    private String database;
    private String username;
    private String password;
}
