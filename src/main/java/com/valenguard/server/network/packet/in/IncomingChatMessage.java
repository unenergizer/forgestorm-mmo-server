package com.valenguard.server.network.packet.in;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.network.packet.out.MessagePacket;
import com.valenguard.server.network.shared.*;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.CHAT)
public class IncomingChatMessage implements PacketListener<IncomingChatMessage.TextMessage> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        return new TextMessage(clientHandler.readString());
    }

    @Override
    public boolean sanitizePacket(TextMessage packetData) {
        String text = packetData.text;

        if (text == null) return false;
        text = text.trim();

        if (text.isEmpty()) return false;
        return !text.contains("\n") && !text.contains("\r") && !text.contains("\t");
    }

    @Override
    public void onEvent(TextMessage packetData) {
        // TODO : Use StringBuilder
        ValenguardMain.getInstance().getGameManager().forAllPlayers(onlinePlayer ->
                new MessagePacket(onlinePlayer, packetData.getPlayer().getName() + ": " + packetData.text).sendPacket());
    }

    @AllArgsConstructor
    class TextMessage extends PacketData {
        private String text;
    }
}
