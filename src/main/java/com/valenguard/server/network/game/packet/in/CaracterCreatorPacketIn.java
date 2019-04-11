package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.Server;
import com.valenguard.server.game.character.CharacterCreatorErrors;
import com.valenguard.server.game.character.CharacterManager;
import com.valenguard.server.network.game.packet.out.CharacterCreatorPacketOut;
import com.valenguard.server.network.game.shared.*;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.APPEARANCE)
public class CaracterCreatorPacketIn implements PacketListener<CaracterCreatorPacketIn.NewCharacterDataPacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {

        String characterName = clientHandler.readString();
        byte factionId = clientHandler.readByte();
        byte headId = clientHandler.readByte();
        byte bodyId = clientHandler.readByte();
        byte colorId = clientHandler.readByte();

        return new NewCharacterDataPacket(characterName, factionId, headId, bodyId, colorId);
    }

    @Override
    public boolean sanitizePacket(NewCharacterDataPacket packetData) {
//        if (packetData.textureIds[0] < 0 || packetData.textureIds[0] > GameConstants.HUMAN_MAX_HEADS) return false;
//        return packetData.textureIds[1] >= 0 && packetData.textureIds[1] <= GameConstants.HUMAN_MAX_BODIES;
        return true;
    }

    @Override
    public void onEvent(NewCharacterDataPacket packetData) {
        CharacterManager characterManager = Server.getInstance().getCharacterManager();

        // TODO: first check username
        if(characterManager.checkUniqueName(packetData.characterName)) {
            // Character name is unique, allow creation
            Server.getInstance().getCharacterManager().createCharacter(
                    packetData.getPlayer(),
                    packetData.characterName,
                    packetData.factionId,
                    packetData.headId,
                    packetData.bodyId,
                    packetData.colorId);
        } else {
            // TODO: Character name is not unique, send error response
            new CharacterCreatorPacketOut(packetData.getPlayer(), CharacterCreatorErrors.NAME_TAKEN).sendPacket();
        }
    }

    @AllArgsConstructor
    class NewCharacterDataPacket extends PacketData {
        private String characterName;
        private byte factionId;
        private byte headId;
        private byte bodyId;
        private byte colorId;
    }
}