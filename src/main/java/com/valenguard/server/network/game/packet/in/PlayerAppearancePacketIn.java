package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.game.GameConstants;
import com.valenguard.server.network.game.shared.*;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.APPEARANCE)
public class PlayerAppearancePacketIn implements PacketListener<PlayerAppearancePacketIn.AppearancePacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        return new AppearancePacket(new short[]{clientHandler.readShort(), clientHandler.readShort()});
    }

    @Override
    public boolean sanitizePacket(AppearancePacket packetData) {
        if (packetData.textureIds[0] < 0 || packetData.textureIds[0] > GameConstants.HUMAN_MAX_HEADS) return false;
        return packetData.textureIds[1] >= 0 && packetData.textureIds[1] <= GameConstants.HUMAN_MAX_BODIES;
    }

    @Override
    public void onEvent(AppearancePacket packetData) {
//        Player player = packetData.getClientHandler().getPlayer();
//
//        short[] textureIds = player.getAppearance().getTextureIds();
//        textureIds[Appearance.BODY] = packetData.textureIds[Appearance.BODY];
//        textureIds[Appearance.HEAD] = packetData.textureIds[Appearance.HEAD];
//
//        final byte appearanceByte = EntityAppearancePacketOut.BODY_INDEX | EntityAppearancePacketOut.HEAD_INDEX;
//
//        Server.getInstance().getGameManager().sendToAllButPlayer(player, clientHandler ->
//                new EntityAppearancePacketOut(clientHandler.getPlayer(), player, appearanceByte).sendPacket());
    }

    @AllArgsConstructor
    class AppearancePacket extends PacketData {
        private short[] textureIds;
    }
}