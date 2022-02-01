package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.ServerMain;
import com.forgestorm.shared.network.game.CharacterCreatorResponses;
import com.forgestorm.server.game.character.CharacterManager;
import com.forgestorm.server.network.game.packet.AllowNullPlayer;
import com.forgestorm.server.network.game.packet.out.CharacterCreatorPacketOut;
import com.forgestorm.server.network.game.shared.*;
import com.forgestorm.shared.network.game.Opcode;
import com.forgestorm.shared.network.game.Opcodes;
import lombok.AllArgsConstructor;

import static com.forgestorm.server.util.Log.println;

@AllowNullPlayer
@Opcode(getOpcode = Opcodes.CHARACTER_CREATOR)
public class CharacterCreatorPacketIn implements PacketListener<CharacterCreatorPacketIn.NewCharacterDataPacket> {

    private static final boolean PRINT_DEBUG = false;

    @Override
    public NewCharacterDataPacket decodePacket(ClientHandler clientHandler) {

        String characterName = clientHandler.readString();
        byte hairTexture = clientHandler.readByte();
        byte hairColor = clientHandler.readByte();
        byte eyeColor = clientHandler.readByte();
        byte skinColor = clientHandler.readByte();

        println(getClass(), "Name: " + characterName, false, PRINT_DEBUG);
        println(getClass(), "HairTexture: " + hairTexture, false, PRINT_DEBUG);
        println(getClass(), "HairColor: " + hairColor + " (Ordinal)", false, PRINT_DEBUG);
        println(getClass(), "EyeColor: " + eyeColor + " (Ordinal)", false, PRINT_DEBUG);
        println(getClass(), "SkinColor: " + skinColor + " (Ordinal)", false, PRINT_DEBUG);

        return new NewCharacterDataPacket(characterName, hairTexture, hairColor, eyeColor, skinColor);
    }

    @Override
    public boolean sanitizePacket(NewCharacterDataPacket packetData) {
//        if (packetData.textureIds[0] < 0 || packetData.textureIds[0] > GameConstants.HUMAN_MAX_HEADS) return false;
//        return packetData.textureIds[1] >= 0 && packetData.textureIds[1] <= GameConstants.HUMAN_MAX_BODIES;
        return true;
    }

    @Override
    public void onEvent(NewCharacterDataPacket packetData) {
        CharacterManager characterManager = ServerMain.getInstance().getCharacterManager();

        // TODO: Make sure we have not exceeded max number of characters allowed

        // Check black list of unacceptable names
        if (characterManager.isNameBlacklisted(packetData.characterName)) {
            new CharacterCreatorPacketOut(packetData.getClientHandler(), CharacterCreatorResponses.FAIL_BLACKLIST_NAME).sendPacket();
            return;
        }

        // Character name is not unique, send error response
        if (!characterManager.isNameUnique(packetData.characterName)) {
            new CharacterCreatorPacketOut(packetData.getClientHandler(), CharacterCreatorResponses.FAIL_NAME_TAKEN).sendPacket();
            return;
        }

        // Character name is good, create the character
        new CharacterCreatorPacketOut(packetData.getClientHandler(), CharacterCreatorResponses.SUCCESS).sendPacket();
        ServerMain.getInstance().getCharacterManager().createCharacter(
                packetData.getClientHandler(),
                packetData.characterName,
                packetData.hairTexture,
                packetData.hairColor,
                packetData.eyeColor,
                packetData.skinColor);
    }

    @AllArgsConstructor
    class NewCharacterDataPacket extends PacketData {
        private String characterName;
        private byte hairTexture;
        private byte hairColor;
        private byte eyeColor;
        private byte skinColor;
    }
}