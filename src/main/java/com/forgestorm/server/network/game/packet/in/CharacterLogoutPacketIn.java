package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.character.CharacterLogout;
import com.forgestorm.server.game.character.CharacterManager;
import com.forgestorm.server.network.game.packet.AllowNullPlayer;
import com.forgestorm.server.network.game.shared.*;
import com.forgestorm.shared.network.game.Opcode;
import com.forgestorm.shared.network.game.Opcodes;
import lombok.AllArgsConstructor;

@AllowNullPlayer
@Opcode(getOpcode = Opcodes.CHARACTER_LOGOUT)
public class CharacterLogoutPacketIn implements PacketListener<CharacterLogoutPacketIn.CharacterLogoutDataPacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        final byte logoutType = clientHandler.readByte();
        return new CharacterLogoutDataPacket(CharacterLogout.getType(logoutType));
    }

    @Override
    public boolean sanitizePacket(CharacterLogoutDataPacket packetData) {
        return true;
    }

    @Override
    public void onEvent(CharacterLogoutDataPacket packetData) {
        CharacterManager characterManager = ServerMain.getInstance().getCharacterManager();

        switch (packetData.logoutType) {
            case LOGOUT_CHARACTER:
                characterManager.characterLogout(packetData.getClientHandler());
                break;
            case LOGOUT_SERVER:
                characterManager.clientDisconnect(packetData.getClientHandler());
                break;
        }
    }

    @AllArgsConstructor
    class CharacterLogoutDataPacket extends PacketData {
        private CharacterLogout logoutType;
    }
}
