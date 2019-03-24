package com.valenguard.server.network.game;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.maps.GameMap;
import com.valenguard.server.network.game.packet.out.PingPacketOut;

public class PingManager {
    public void tick() {
        for (GameMap gameMap : ValenguardMain.getInstance().getGameManager().getGameMaps().values()) {
            for (Player player : gameMap.getPlayerController().getPlayerList()) new PingPacketOut(player).sendPacket();
        }
    }
}
