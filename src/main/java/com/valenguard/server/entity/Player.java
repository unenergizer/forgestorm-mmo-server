package com.valenguard.server.entity;

import com.valenguard.server.network.shared.ClientHandler;
import com.valenguard.server.network.shared.Write;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player extends Entity {

    private ClientHandler clientHandler;

    private MoveDirection latestMoveRequest;

    public void sendPacket(byte opcode, Write writeCallback) {
        clientHandler.write(opcode, writeCallback);
    }

    @Override
    protected void finalize() {
        System.out.println("Player Destroyed! IP: " + clientHandler.getSocket().getInetAddress() + ", eID: " + getServerEntityId());
    }
}
