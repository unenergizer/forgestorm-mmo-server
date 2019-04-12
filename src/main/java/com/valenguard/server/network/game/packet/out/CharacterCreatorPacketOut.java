package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.character.CharacterCreatorErrors;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.Opcodes;

public class CharacterCreatorPacketOut extends AbstractServerOutPacket {

    private final CharacterCreatorErrors error;

    public CharacterCreatorPacketOut(final Player receiver, CharacterCreatorErrors error) {
        super(Opcodes.CHARACTER_SELECT, receiver.getClientHandler());
        this.error = error;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeByte(error.getCharacterErrorTypeByte());
    }
}