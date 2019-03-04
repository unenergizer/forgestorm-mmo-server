package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.EntityType;
import com.valenguard.server.game.entity.MovingEntity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.maps.Location;
import com.valenguard.server.game.maps.MoveDirection;
import com.valenguard.server.network.shared.Opcodes;

import static com.google.common.base.Preconditions.checkArgument;
import static com.valenguard.server.util.Log.println;

public class EntityMovePacketOut extends ServerAbstractOutPacket {

    private static final boolean PRINT_DEBUG = false;

    private final MovingEntity movingEntity;
    private final Location attemptLocation;

    public EntityMovePacketOut(Player packetReceiver, MovingEntity movingEntity, Location attemptLocation) {
        super(Opcodes.ENTITY_MOVE_UPDATE, packetReceiver);
        this.movingEntity = movingEntity;
        this.attemptLocation = attemptLocation;
    }

    @Override
    protected void createPacket(ValenguardOutputStream write) {
        checkArgument(movingEntity.getFacingDirection() != MoveDirection.NONE, "Server tried to send a NONE type face direction!");

        write.writeShort(movingEntity.getServerEntityId());
        if (packetReceiver.equals(movingEntity)) {
            write.writeByte(EntityType.CLIENT_PLAYER.getEntityTypeByte());
        } else {
            write.writeByte(movingEntity.getEntityType().getEntityTypeByte());
        }
        write.writeShort(attemptLocation.getX());
        write.writeShort(attemptLocation.getY());

        println(getClass(), "EntityName: " + movingEntity.getName() + ", ServerID: " + movingEntity.getServerEntityId() + ", " + attemptLocation, false, PRINT_DEBUG);

    }
}
