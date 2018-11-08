package com.valenguard.server.network;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.maps.GameMap;
import com.valenguard.server.network.packet.out.PingOut;

public class PingManager {

    public void tick() {
        for (GameMap gameMap : ValenguardMain.getInstance().getGameManager().getGameMaps().values()) {
            for (Player player : gameMap.getPlayerList()) {
                new PingOut(player).sendPacket();
            }
        }
    }
}
