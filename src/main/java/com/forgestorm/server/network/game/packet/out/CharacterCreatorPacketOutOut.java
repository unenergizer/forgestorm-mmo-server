package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.shared.network.game.CharacterCreatorResponses;
import com.forgestorm.server.network.game.shared.ClientHandler;
import com.forgestorm.shared.network.game.Opcodes;
import com.forgestorm.shared.network.game.GameOutputStream;

public class CharacterCreatorPacketOutOut extends AbstractPacketOut {

    private final CharacterCreatorResponses characterCreatorResponses;

    public CharacterCreatorPacketOutOut(final ClientHandler clientHandler, CharacterCreatorResponses characterCreatorResponses) {
        super(Opcodes.CHARACTER_CREATOR_ERROR, clientHandler);
        this.characterCreatorResponses = characterCreatorResponses;
    }

    @Override
    public void createPacket(GameOutputStream write) {
        write.writeByte(characterCreatorResponses.getCharacterCreatorResponsesByte());
    }
}
