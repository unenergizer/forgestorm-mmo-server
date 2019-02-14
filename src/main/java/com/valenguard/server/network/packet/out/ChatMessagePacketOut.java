package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.shared.Opcodes;
import com.valenguard.server.util.Log;

public class ChatMessagePacketOut extends ServerAbstractOutPacket {

    private static final boolean PRINT_DEBUG = false;
    private final String message;

    public ChatMessagePacketOut(Player player, String message) {
        super(Opcodes.CHAT, player);
        this.message = message;
    }

    @Override
    protected void createPacket(ValenguardOutputStream write) {
        write.writeString(message);
        Log.println(getClass(), message, false, PRINT_DEBUG);
    }
}
