package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.EntityType;
import com.forgestorm.server.game.world.entity.MovingEntity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.shared.network.game.Opcodes;
import com.forgestorm.shared.network.game.GameOutputStream;

public class EntityUpdatePacketOut extends AbstractPacketOut {

    private final short serverEntityId;
    private final EntityType entityType;
    private final float moveSpeed;

    public EntityUpdatePacketOut(final Player player, final MovingEntity updateTarget, float moveSpeed) {
        super(Opcodes.ENTITY_UPDATE_SPEED, player.getClientHandler());

        this.serverEntityId = updateTarget.getServerEntityId();
        this.entityType = detectEntityType(updateTarget);
        this.moveSpeed = moveSpeed;
    }

    @Override
    public void createPacket(GameOutputStream write) {
        write.writeShort(serverEntityId);
        write.writeByte(entityType.getEntityTypeByte());
        write.writeFloat(moveSpeed);
    }
}
