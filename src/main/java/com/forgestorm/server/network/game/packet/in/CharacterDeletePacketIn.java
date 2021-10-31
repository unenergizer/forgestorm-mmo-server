package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.network.game.shared.*;
import com.forgestorm.shared.network.game.Opcode;
import com.forgestorm.shared.network.game.Opcodes;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.CHARACTER_DELETE)
public class CharacterDeletePacketIn implements PacketListener<CharacterDeletePacketIn.CharacterDeleteDataPacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        final byte characterId = clientHandler.readByte();
        return new CharacterDeleteDataPacket(characterId);
    }

    @Override
    public boolean sanitizePacket(CharacterDeleteDataPacket packetData) {
        return true;
    }

    @Override
    public void onEvent(CharacterDeleteDataPacket packetData) {
        ServerMain.getInstance().getCharacterManager().deleteCharacter(packetData.getClientHandler(), packetData.characterId);
    }

    @AllArgsConstructor
    class CharacterDeleteDataPacket extends PacketData {
        private byte characterId;
    }
}
