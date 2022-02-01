package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.shared.game.world.maps.building.LayerDefinition;
import com.forgestorm.server.network.game.shared.ClientHandler;
import com.forgestorm.shared.network.game.Opcodes;
import com.forgestorm.shared.network.game.GameOutputStream;

public class WorldBuilderPacketOut extends AbstractPacketOut {

    private final LayerDefinition layerDefinition;
    private final int textureId;
    private final int tileX, tileY;
    private final short worldZ;

    public WorldBuilderPacketOut(final ClientHandler clientHandler, LayerDefinition layerDefinition, int textureId, int tileX, int tileY, short worldZ) {
        super(Opcodes.WORLD_BUILDER, clientHandler);
        this.layerDefinition = layerDefinition;
        this.textureId = textureId;
        this.tileX = tileX;
        this.tileY = tileY;
        this.worldZ = worldZ;
    }

    @Override
    public void createPacket(GameOutputStream write) {
        write.writeByte(layerDefinition.getLayerDefinitionByte());
        write.writeInt(textureId);
        write.writeInt(tileX);
        write.writeInt(tileY);
        write.writeShort(worldZ);
    }
}
