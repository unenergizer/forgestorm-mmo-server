package com.valenguard.server.network.packet.out;

import com.valenguard.server.entity.Entity;
import com.valenguard.server.entity.Player;
import com.valenguard.server.maps.data.Location;
import com.valenguard.server.network.shared.Opcodes;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class MoveEntityPacket extends ServerOutPacket {


    private static int packetId = 0;

    private Entity entityToMove;
    private Location attemptLocation;

    public MoveEntityPacket(Player sendTo, Entity entityToMove, Location attemptLocation) {
        super(Opcodes.ENTITY_MOVE_UPDATE, sendTo);
        this.entityToMove = entityToMove;
        this.attemptLocation = attemptLocation;
    }

    @Override
    protected void createPacket(ObjectOutputStream write) throws IOException {
        write.writeShort(entityToMove.getServerEntityId());
        write.writeInt(attemptLocation.getX());
        write.writeInt(attemptLocation.getY());
        write.writeInt(packetId++);
        write.writeLong(System.currentTimeMillis());
    }
}
