package com.valenguard.server.network.packet.in;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.GameConstants;
import com.valenguard.server.game.entity.Appearance;
import com.valenguard.server.network.packet.out.EntityAppearancePacketOut;
import com.valenguard.server.network.shared.*;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.APPEARANCE)
public class PlayerAppearancePacketIn implements PacketListener<PlayerAppearancePacketIn.AppearancePacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        return new AppearancePacket(new short[]{ clientHandler.readShort(), clientHandler.readShort() });
    }

    @Override
    public boolean sanitizePacket(AppearancePacket packetData) {
        if (packetData.textureIds[0] < 0 || packetData.textureIds[0] > GameConstants.HUMAN_MAX_HEADS) return false;
        return packetData.textureIds[1] >= 0 && packetData.textureIds[1] <= GameConstants.HUMAN_MAX_BODIES;
    }

    @Override
    public void onEvent(AppearancePacket packetData) {

        short[] textureIds = packetData.getPlayer().getAppearance().getTextureIds();
        textureIds[Appearance.BODY] = packetData.textureIds[Appearance.BODY];
        textureIds[Appearance.HEAD] = packetData.textureIds[Appearance.HEAD];

        final byte appearanceByte = EntityAppearancePacketOut.BODY_INDEX | EntityAppearancePacketOut.HEAD_INDEX;

        ValenguardMain.getInstance().getGameManager().sendToAllButPlayer(packetData.getPlayer(), clientHandler ->
                new EntityAppearancePacketOut(clientHandler.getPlayer(), packetData.getPlayer(), appearanceByte).sendPacket());
    }

    @AllArgsConstructor
    class AppearancePacket extends PacketData {
        private short[] textureIds;
    }
}