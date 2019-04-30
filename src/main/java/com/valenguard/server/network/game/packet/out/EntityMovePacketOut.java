package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.world.entity.MovingEntity;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.game.world.maps.MoveDirection;
import com.valenguard.server.network.game.shared.Opcodes;

import static com.google.common.base.Preconditions.checkArgument;
import static com.valenguard.server.util.Log.println;

public class EntityMovePacketOut extends AbstractServerOutPacket {

    private static final boolean PRINT_DEBUG = false;

    private final MovingEntity movingEntity;
    private final Location attemptLocation;

    public EntityMovePacketOut(final Player packetReceiver, final MovingEntity movingEntity, final Location attemptLocation) {
        super(Opcodes.ENTITY_MOVE_UPDATE, packetReceiver.getClientHandler());
        this.movingEntity = movingEntity;
        this.attemptLocation = new Location(attemptLocation);
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        checkArgument(movingEntity.getFacingDirection() != MoveDirection.NONE, "Server tried to send a NONE type face direction!");

        write.writeShort(movingEntity.getServerEntityId());
        write.writeByte(getEntityType(movingEntity).getEntityTypeByte());
        write.writeShort(attemptLocation.getX());
        write.writeShort(attemptLocation.getY());

        println(getClass(), "EntityName: " + movingEntity.getName() + ", ServerID: " + movingEntity.getServerEntityId() + ", " + attemptLocation, false, PRINT_DEBUG);

    }
}
