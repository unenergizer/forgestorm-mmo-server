package com.valenguard.server.entity;

import com.valenguard.server.maps.data.Location;
import com.valenguard.server.network.shared.ClientHandler;
import com.valenguard.server.network.shared.Write;
import lombok.Getter;

public class Player extends Entity {

    @Getter
    private ClientHandler clientHandler;

    public Player(int entityID, Location location, float moveSpeed, ClientHandler clientHandler) {
        super(entityID, location, moveSpeed);
        this.clientHandler = clientHandler;
    }

    public void sendPacket(byte opcode, Write writeCallback) {
        clientHandler.write(opcode, writeCallback);
    }

    @Override
    protected void finalize() {
        System.out.println("Player Destroyed! IP: " + clientHandler.getSocket().getInetAddress() + ", eID: " + getEntityID());
    }
}
