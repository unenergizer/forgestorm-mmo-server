package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.shared.*;
import com.forgestorm.shared.network.game.Opcode;
import com.forgestorm.shared.network.game.Opcodes;
import lombok.AllArgsConstructor;

import static com.forgestorm.server.util.Log.println;

@Opcode(getOpcode = Opcodes.PROFILE_REQUEST)
public class ProfileRequestPacketIn implements PacketListener<ProfileRequestPacketIn.ProfileRequestPacket> {

    private static final boolean PRINT_DEBUG = false;

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        final short serverEntityID = clientHandler.readShort();
        return new ProfileRequestPacket(serverEntityID);
    }

    @Override
    public boolean sanitizePacket(ProfileRequestPacket packetData) {
        return true;
    }

    @Override
    public void onEvent(ProfileRequestPacket packetData) {
        Player playerRequester = packetData.getClientHandler().getPlayer();
        Player profileToGet = ServerMain.getInstance().getGameManager().findPlayer(packetData.serverEntityID);

        println(getClass(), "PlayerRequester: " + playerRequester.getName(), false, PRINT_DEBUG);
        println(getClass(), "ProfileToGet: " + profileToGet.getName(), false, PRINT_DEBUG);

        ServerMain.getInstance().getXenforoProfileManager().sendXenforoProfile(profileToGet, playerRequester);
    }

    @AllArgsConstructor
    static class ProfileRequestPacket extends PacketData {
        short serverEntityID;
    }
}
