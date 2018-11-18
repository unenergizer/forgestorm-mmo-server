package com.valenguard.server.network.packet.in;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.GameConstants;
import com.valenguard.server.game.entity.Appearance;
import com.valenguard.server.network.packet.out.AppearanceUpdate;
import com.valenguard.server.network.shared.*;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.APPEARANCE)
public class AppearanceChange implements PacketListener<AppearanceChange.AppearancePacket> {

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

        packetData.getPlayer().setAppearance(new Appearance(packetData.textureIds));

        ValenguardMain.getInstance().getGameManager().sendToAllButPlayer(packetData.getPlayer(), clientHandler ->
                new AppearanceUpdate(clientHandler.getPlayer(), packetData.getPlayer()).sendPacket());
    }

    @AllArgsConstructor
    class AppearancePacket extends PacketData {
        private short[] textureIds;
    }
}