package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.shared.Opcodes;
import com.valenguard.server.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;

public class MessagePacket extends ServerOutPacket {

    private String message;

    public MessagePacket(Player player, String message) {
        super(Opcodes.CHAT, player);
        this.message = message;
    }

    @Override
    protected void createPacket(DataOutputStream write) throws IOException {
        write.writeUTF(message);
        Log.println(getClass(), message);
    }
}
