package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.world.entity.MovingEntity;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.Opcodes;

public class EntityUpdatePacketOut extends AbstractServerOutPacket {

    private static final boolean PRINT_DEBUG = false;
    private final MovingEntity updateTarget;
    private final float moveSpeed;

    public EntityUpdatePacketOut(final Player player, final MovingEntity updateTarget, float moveSpeed) {
        super(Opcodes.ENTITY_UPDATE_SPEED, player.getClientHandler());
        this.updateTarget = updateTarget;
        this.moveSpeed = moveSpeed;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeShort(updateTarget.getServerEntityId());
        write.writeByte(getEntityType(updateTarget).getEntityTypeByte());
        write.writeFloat(moveSpeed);
    }
}
