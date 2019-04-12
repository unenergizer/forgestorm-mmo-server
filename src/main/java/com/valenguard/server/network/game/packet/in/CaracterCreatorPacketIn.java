package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.Server;
import com.valenguard.server.game.character.CharacterCreatorErrors;
import com.valenguard.server.game.character.CharacterManager;
import com.valenguard.server.network.game.packet.AllowNullPlayer;
import com.valenguard.server.network.game.packet.out.CharacterCreatorPacketOut;
import com.valenguard.server.network.game.shared.*;
import lombok.AllArgsConstructor;

@AllowNullPlayer
@Opcode(getOpcode = Opcodes.APPEARANCE)
public class CaracterCreatorPacketIn implements PacketListener<CaracterCreatorPacketIn.NewCharacterDataPacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {

        String characterName = clientHandler.readString();
        byte factionId = clientHandler.readByte();
        byte bodyId = clientHandler.readByte();
        byte headId = clientHandler.readByte();
        byte colorId = clientHandler.readByte();

        return new NewCharacterDataPacket(characterName, factionId, bodyId, headId, colorId);
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

        // TODO: Make sure we have not exceeded max number of characters allowed

        // TODO: first check username
        // TODO: check list of unacceptable names
        if (characterManager.isNameUnique(packetData.characterName)) {
            // Character name is unique, allow creation
            Server.getInstance().getCharacterManager().createCharacter(
                    packetData.getPlayer(),
                    packetData.characterName,
                    packetData.factionId,
                    packetData.bodyId,
                    packetData.headId,
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
        private byte bodyId;
        private byte headId;
        private byte colorId;
    }
}