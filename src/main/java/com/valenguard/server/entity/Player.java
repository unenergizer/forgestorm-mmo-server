package com.valenguard.server.entity;

import com.valenguard.server.maps.data.Warp;
import com.valenguard.server.network.shared.ClientHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Queue;

@Getter
@Setter
public class Player extends Entity {
    private ClientHandler clientHandler;
    private Queue<MoveDirection> latestMoveRequests = new LinkedList<>();
    private Warp warp;
    private long pingOutTime = 0;
    private long lastPingTime = 0;

    public void addDirectionToFutureQueue(MoveDirection moveDirection) {
        latestMoveRequests.add(moveDirection);
    }

}
