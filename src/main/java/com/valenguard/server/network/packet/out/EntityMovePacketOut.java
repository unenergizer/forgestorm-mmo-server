package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.MovingEntity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.maps.Location;
import com.valenguard.server.game.maps.MoveDirection;
import com.valenguard.server.network.shared.Opcodes;
import com.valenguard.server.util.Log;

import static com.google.common.base.Preconditions.checkArgument;

public class EntityMovePacketOut extends ServerAbstractOutPacket {

    private static final boolean PRINT_DEBUG = false;

    private final MovingEntity movingEntity;
    private final Location attemptLocation;

    public EntityMovePacketOut(Player sendTo, MovingEntity movingEntity, Location attemptLocation) {
        super(Opcodes.ENTITY_MOVE_UPDATE, sendTo);
        this.movingEntity = movingEntity;
        this.attemptLocation = attemptLocation;
    }

    @Override
    protected void createPacket(ValenguardOutputStream write) {
        checkArgument(movingEntity.getFacingDirection() != MoveDirection.NONE, "Server tried to send a NONE type face direction!");
        checkArgument(!movingEntity.getCurrentMapLocation().equals(movingEntity.getFutureMapLocation()), "FutureLocation and CurrentLocation should not be equal!");

        write.writeShort(movingEntity.getServerEntityId());
        write.writeShort(attemptLocation.getX());
        write.writeShort(attemptLocation.getY());

        Log.println(getClass(), "EntityName: " + movingEntity.getName() + ", ServerID: " + movingEntity.getServerEntityId() + ", " + attemptLocation, false, PRINT_DEBUG);

    }
}
