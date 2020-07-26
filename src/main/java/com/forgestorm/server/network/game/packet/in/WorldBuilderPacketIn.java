package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.world.maps.building.LayerDefinition;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.packet.out.WorldBuilderPacketOut;
import com.forgestorm.server.network.game.shared.*;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.WORLD_BUILDER)
public class WorldBuilderPacketIn implements PacketListener<WorldBuilderPacketIn.WorldBuilderPacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        LayerDefinition layerDefinition = LayerDefinition.getLayerDefinition(clientHandler.readByte());
        short textureId = clientHandler.readShort();
        short tileX = clientHandler.readShort();
        short tileY = clientHandler.readShort();
        return new WorldBuilderPacket(clientHandler.getPlayer(), layerDefinition, textureId, tileX, tileY);
    }

    @Override
    public boolean sanitizePacket(WorldBuilderPacket packetData) {
        // TODO: Make sure this person can build...
        return true;
    }

    @Override
    public void onEvent(WorldBuilderPacket packetData) {
        // TODO: Create manager that can send all map edits to the client
        // TODO: Clients who join late won't get the edits...

        // For now, just resend the building packet to all (but original playerSender)
        ServerMain.getInstance().getGameManager().sendToAllButPlayer(packetData.playerSender, clientHandler ->
                new WorldBuilderPacketOut(clientHandler, packetData.layerDefinition, packetData.textureId, packetData.tileX, packetData.tileY).sendPacket());
    }

    @AllArgsConstructor
    class WorldBuilderPacket extends PacketData {
        private Player playerSender;
        private LayerDefinition layerDefinition;
        private short textureId;
        private short tileX;
        private short tileY;
    }
}