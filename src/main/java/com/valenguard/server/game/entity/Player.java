package com.valenguard.server.game.entity;

import com.valenguard.server.game.maps.MoveDirection;
import com.valenguard.server.game.maps.Warp;
import com.valenguard.server.network.shared.ClientHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Queue;

@Getter
@Setter
public class Player extends MovingEntity {

    private ClientHandler clientHandler;
    private Queue<MoveDirection> latestMoveRequests = new LinkedList<>();

    private Warp warp;

    private long pingOutTime = 0;
    private long lastPingTime = 0;

    public void addDirectionToFutureQueue(MoveDirection moveDirection) {
        latestMoveRequests.add(moveDirection);
    }

    @Override
    public void gameMapDeregister() {
        super.gameMapDeregister();
        getLatestMoveRequests().clear();
    }

}
