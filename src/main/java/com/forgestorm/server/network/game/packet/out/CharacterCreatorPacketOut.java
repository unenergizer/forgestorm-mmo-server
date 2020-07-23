package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.character.CharacterCreatorResponses;
import com.forgestorm.server.network.game.shared.ClientHandler;
import com.forgestorm.server.network.game.shared.Opcodes;

public class CharacterCreatorPacketOut extends AbstractServerOutPacket {

    private final CharacterCreatorResponses characterCreatorResponses;

    public CharacterCreatorPacketOut(final ClientHandler clientHandler, CharacterCreatorResponses characterCreatorResponses) {
        super(Opcodes.CHARACTER_CREATOR_ERROR, clientHandler);
        this.characterCreatorResponses = characterCreatorResponses;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeByte(characterCreatorResponses.getCharacterCreatorResponsesByte());
    }
}
