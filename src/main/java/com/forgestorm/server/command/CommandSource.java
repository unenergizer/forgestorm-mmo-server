package com.forgestorm.server.command;

import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.packet.out.ChatMessagePacketOut;
import lombok.Getter;

import static com.forgestorm.server.util.Log.println;

public class CommandSource {

    @Getter
    private Player player;

    CommandSource() {
    }

    public CommandSource(Player player) {
        this.player = player;
    }

    public void sendMessage(String message) {
        if (player == null) {
            sendMessage(message, null);
        } else {
            sendMessage(message, ChatChannelType.GENERAL);
        }
    }

    public void sendMessage(String message, ChatChannelType chatChannelType) {
        if (player == null) {
            println(CommandSource.class, message);
        } else {
            new ChatMessagePacketOut(player, chatChannelType, message).sendPacket();
        }
    }
}
