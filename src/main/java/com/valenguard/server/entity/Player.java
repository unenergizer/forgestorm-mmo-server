package com.valenguard.server.entity;

import com.valenguard.server.network.shared.ClientHandler;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player extends Entity {
    private ClientHandler clientHandler;
    private MoveDirection latestMoveRequest;
    private long pingOutTime = 0;
    private long lastPingTime = 0;
}
