package com.valenguard.server.command;

import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.packet.out.ChatMessagePacketOut;
import lombok.Getter;

import static com.valenguard.server.util.Log.println;

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
            println(CommandSource.class, message);
        } else {
            new ChatMessagePacketOut(player, message).sendPacket();
        }
    }
}
