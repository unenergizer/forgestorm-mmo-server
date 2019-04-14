package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.Server;
import com.valenguard.server.game.character.*;
import com.valenguard.server.network.game.packet.AllowNullPlayer;
import com.valenguard.server.network.game.shared.*;
import lombok.AllArgsConstructor;

@AllowNullPlayer
@Opcode(getOpcode = Opcodes.CHARACTER_CREATOR)
public class CharacterCreatorPacketIn implements PacketListener<CharacterCreatorPacketIn.NewCharacterDataPacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {

        byte characterClass = clientHandler.readByte();
        byte characterGender = clientHandler.readByte();
        byte characterRace = clientHandler.readByte();
        byte characterColor = clientHandler.readByte();
        String characterName = clientHandler.readString();

        return new NewCharacterDataPacket(CharacterClasses.getType(characterClass),
                CharacterGenders.getType(characterGender),
                CharacterRaces.getType(characterRace),
                ColorList.getType(characterColor),
                characterName);
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
                    packetData.getClientHandler(),
                    packetData.characterClass,
                    packetData.characterGender,
                    packetData.characterRace,
                    packetData.characterColor,
                    packetData.characterName);
        } else {
            // TODO: Character name is not unique, send error response
//            new CharacterCreatorPacketOut(packetData.getPlayer(), CharacterCreatorErrors.NAME_TAKEN).sendPacket();
        }
    }

    @AllArgsConstructor
    class NewCharacterDataPacket extends PacketData {
        private CharacterClasses characterClass;
        private CharacterGenders characterGender;
        private CharacterRaces characterRace;
        private ColorList characterColor;
        private String characterName;
    }
}