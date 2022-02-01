package com.forgestorm.server.game.world.task;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.game.MessageText;
import com.forgestorm.server.network.game.packet.out.ChatMessagePacketOut;
import com.forgestorm.server.util.ServerTimeUtil;

import java.util.concurrent.TimeUnit;

public class CheckIdlePlayerTask {

    private static final int IDLE_LOGOUT_TIME_SECONDS = ServerTimeUtil.getMinutes(5);

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
