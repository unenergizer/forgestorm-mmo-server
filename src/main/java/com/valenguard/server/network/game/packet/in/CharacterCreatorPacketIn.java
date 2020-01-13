package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.Server;
import com.valenguard.server.game.character.CharacterManager;
import com.valenguard.server.network.game.packet.AllowNullPlayer;
import com.valenguard.server.network.game.shared.*;
import lombok.AllArgsConstructor;

import static com.valenguard.server.util.Log.println;

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
        CharacterManager characterManager = Server.getInstance().getCharacterManager();

        // TODO: Make sure we have not exceeded max number of characters allowed

        // TODO: first check username
        // TODO: check list of unacceptable names
        if (characterManager.isNameUnique(packetData.characterName)) {
            // Character name is unique, allow creation
            Server.getInstance().getCharacterManager().createCharacter(
                    packetData.getClientHandler(),
                    packetData.characterName,
                    packetData.hairTexture,
                    packetData.hairColor,
                    packetData.eyeColor,
                    packetData.skinColor);
        } else {
            // TODO: Character name is not unique, send error response
//            new CharacterCreatorPacketOut(packetData.getPlayer(), CharacterCreatorErrors.NAME_TAKEN).sendPacket();
        }
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