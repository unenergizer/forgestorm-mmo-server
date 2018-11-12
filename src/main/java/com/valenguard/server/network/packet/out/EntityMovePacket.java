package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.Entity;
import com.valenguard.server.game.entity.MovingEntity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.maps.Location;
import com.valenguard.server.game.maps.MoveDirection;
import com.valenguard.server.network.shared.Opcodes;
import com.valenguard.server.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;

import static com.google.common.base.Preconditions.checkArgument;

public class EntityMovePacket extends ServerOutPacket {

    private static final boolean PRINT_DEBUG = false;

    private final Entity entityToMove;
    private final Location attemptLocation;

    public EntityMovePacket(Player sendTo, Entity entityToMove, Location attemptLocation) {
        super(Opcodes.ENTITY_MOVE_UPDATE, sendTo);
        this.entityToMove = entityToMove;
        this.attemptLocation = attemptLocation;
    }

    @Override
    protected void createPacket(ObjectOutputStream write) throws IOException {
        if (entityToMove instanceof MovingEntity) {
            MovingEntity movingEntity = (MovingEntity) entityToMove;
            checkArgument(movingEntity.getFacingDirection() != MoveDirection.NONE, "Server tried to send a NONE type face direction!");
            checkArgument(!movingEntity.getCurrentMapLocation().equals(movingEntity.getFutureMapLocation()), "FutureLocation and CurrentLocation should not be equal!");
        }
        write.writeShort(entityToMove.getServerEntityId());
        write.writeInt(attemptLocation.getX());
        write.writeInt(attemptLocation.getY());

        Log.println(getClass(), "", false, PRINT_DEBUG);
        Log.println(getClass(), "ServerID: " + entityToMove.getServerEntityId() + ", " + attemptLocation, false, PRINT_DEBUG);

    }
}
