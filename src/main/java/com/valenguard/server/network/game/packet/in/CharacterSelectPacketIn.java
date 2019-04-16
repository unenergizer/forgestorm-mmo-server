package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.Server;
import com.valenguard.server.network.game.packet.AllowNullPlayer;
import com.valenguard.server.network.game.shared.*;
import lombok.AllArgsConstructor;

@AllowNullPlayer
@Opcode(getOpcode = Opcodes.CHARACTER_SELECT)
public class CharacterSelectPacketIn implements PacketListener<CharacterSelectPacketIn.CharacterSelectDataPacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        final byte characterId = clientHandler.readByte();
        return new CharacterSelectDataPacket(characterId);
    }

    @Override
    public boolean sanitizePacket(CharacterSelectDataPacket packetData) {
        return true;
    }

    @Override
    public void onEvent(CharacterSelectDataPacket packetData) {
        Server.getInstance().getCharacterManager().characterLogin(packetData.getClientHandler(), packetData.characterId);
    }

    @AllArgsConstructor
    class CharacterSelectDataPacket extends PacketData {
        private byte characterId;
    }
}
