package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.maps.building.LayerDefinition;
import com.forgestorm.server.network.game.shared.ClientHandler;
import com.forgestorm.server.network.game.shared.Opcodes;

public class WorldBuilderPacketOut extends AbstractServerOutPacket {

    private final LayerDefinition layerDefinition;
    private final short textureId;
    private final short tileX, tileY;

    public WorldBuilderPacketOut(final ClientHandler clientHandler, LayerDefinition layerDefinition, short textureId, short tileX, short tileY) {
        super(Opcodes.WORLD_BUILDER, clientHandler);
        this.layerDefinition = layerDefinition;
        this.textureId = textureId;
        this.tileX = tileX;
        this.tileY = tileY;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeByte(layerDefinition.getLayerDefinitionByte());
        write.writeShort(textureId);
        write.writeShort(tileX);
        write.writeShort(tileY);
    }
}
