package com.valenguard.server.network.game;

import com.valenguard.server.network.game.shared.ClientHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerSessionData {
    private short serverID;
    private String username;
    private ClientHandler clientHandler;
}
