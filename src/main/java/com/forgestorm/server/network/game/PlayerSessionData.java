package com.forgestorm.server.network.game;

import com.forgestorm.server.network.game.shared.ClientHandler;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerSessionData {
    private short serverID;
    private ClientHandler clientHandler;
}
