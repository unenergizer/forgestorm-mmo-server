package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.character.CharacterDataOut;
import com.valenguard.server.network.game.shared.ClientHandler;
import com.valenguard.server.network.game.shared.Opcodes;

public class CharacterMenuLoadPacketOut extends AbstractServerOutPacket {

    private final CharacterDataOut[] characterDataOuts;

    public CharacterMenuLoadPacketOut(ClientHandler clientHandler, CharacterDataOut[] characterDataOuts) {
        super(Opcodes.CHARACTERS_MENU_LOAD, clientHandler);
        this.characterDataOuts = characterDataOuts;
    }

    @Override
    protected void createPacket(GameOutputStream write) {

        write.writeByte((byte) characterDataOuts.length);

        for (CharacterDataOut dataOut : characterDataOuts) {
            write.writeString(dataOut.getName());
            write.writeByte(dataOut.getCharacterId());
        }
    }
}
