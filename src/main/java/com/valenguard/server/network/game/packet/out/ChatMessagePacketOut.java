package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.Opcodes;
import com.valenguard.server.util.Log;

public class ChatMessagePacketOut extends AbstractServerOutPacket {

    private static final boolean PRINT_DEBUG = false;
    private final String message;

    public ChatMessagePacketOut(Player player, String message) {
        super(Opcodes.CHAT, player);
        this.message = message;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeString(message);
        Log.println(getClass(), message, false, PRINT_DEBUG);
    }
}
