package com.forgestorm.server.game;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.network.game.packet.out.ChatMessagePacketOut;

import java.util.concurrent.TimeUnit;

public class CheckIdlePlayerTask {

    private static final int IDLE_LOGOUT_TIME_MINUTES = 5;
    private static final int IDLE_LOGOUT_TIME_SECONDS = IDLE_LOGOUT_TIME_MINUTES * 60;

    public void tick(long numberOfTicksPassed) {
        long currentTime = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis());
        if (numberOfTicksPassed % 20 == 0) {
            ServerMain.getInstance().getGameManager().forAllPlayers(player -> {
                long lastMoveTime = TimeUnit.MILLISECONDS.toSeconds(player.getIdleTimestamp());

                if (currentTime - lastMoveTime == IDLE_LOGOUT_TIME_SECONDS - 60) {
                    // Send warning message
                    new ChatMessagePacketOut(player, ChatChannelType.GENERAL, MessageText.WARNING + "You will be kicked in 1 minute for inactivity.").sendPacket();

                } else if (currentTime - lastMoveTime > IDLE_LOGOUT_TIME_SECONDS) {
                    // Kick the player client
                    ServerMain.getInstance().getGameManager().kickPlayer(player);
                    ServerMain.getInstance().getNetworkManager().getOutStreamManager().removeClient(player.getClientHandler());
                    player.getClientHandler().closeConnection();
                }
            });
        }
    }

}
