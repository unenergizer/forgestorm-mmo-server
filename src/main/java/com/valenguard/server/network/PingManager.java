package com.valenguard.server.network;

import com.valenguard.server.entity.PlayerManager;
import com.valenguard.server.network.packet.out.PingOut;
import com.valenguard.server.network.shared.ClientHandler;

public class PingManager {

    public void tick() {
//        System.out.println("PING: TICK");
        for (ClientHandler clientHandler : PlayerManager.getInstance().getClientHandles()) {
            new PingOut(clientHandler.getPlayer()).sendPacket();
        }
    }
}
