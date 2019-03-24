package com.valenguard.server.network.game;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.maps.GameMap;
import com.valenguard.server.network.game.packet.out.PingPacketOut;

public class PingManager {
    public void tick() {
        for (GameMap gameMap : Server.getInstance().getGameManager().getGameMaps().values()) {
            for (Player player : gameMap.getPlayerController().getPlayerList()) new PingPacketOut(player).sendPacket();
        }
    }
}
