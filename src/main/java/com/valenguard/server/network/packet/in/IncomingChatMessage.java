package com.valenguard.server.network.packet.in;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.network.packet.out.MessagePacket;
import com.valenguard.server.network.shared.*;
import com.valenguard.server.util.Log;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.CHAT)
public class IncomingChatMessage implements PacketListener<IncomingChatMessage.TextMessage> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        return new TextMessage(clientHandler.readString());
    }

    @Override
    public void onEvent(TextMessage packetData) {
        String text = packetData.text;
        Log.println(getClass(), "Text before making any checks: " + text);

        if (text == null) {
            Log.println(getClass(), "Text was null somehow");
            return;
        }

        text = text.trim();
        Log.println(getClass(), "After Trim: " + text);

        if (text.isEmpty()) {
            Log.println(getClass(), "The text was empty");
            return;
        }

        if (text.contains("\n") || text.contains("\r") || text.contains("\t")) {
            Log.println(getClass(), "Not sending text messages with \\n \\r or \\t");
            return;
        }

        final String sendMessage = text;
        Log.println(getClass(), "This string is being sent to the clients: " + sendMessage);

        // TODO use string builder when we get around to properly formatting.
        ValenguardMain.getInstance().getGameManager().forAllPlayers(onlinePlayer ->
                new MessagePacket(onlinePlayer, packetData.getPlayer().getName() + ": " + sendMessage).sendPacket());
    }

    @AllArgsConstructor
    class TextMessage extends PacketData {
        private String text;
    }
}
