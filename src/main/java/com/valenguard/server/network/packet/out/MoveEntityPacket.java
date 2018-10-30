package com.valenguard.server.network.packet.out;

import com.valenguard.server.entity.MoveDirection;
import com.valenguard.server.entity.Entity;
import com.valenguard.server.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class MoveEntityPacket extends ServerOutPacket {

    private Entity entityToMove;
    private MoveDirection direction;

    public MoveEntityPacket(Player sendTo, Entity entityToMove, MoveDirection direction) {
        super(Opcodes.ENTITY_MOVE_UPDATE, sendTo);
        this.direction = direction;
        this.entityToMove = entityToMove;
    }

    @Override
    protected void createPacket(ObjectOutputStream write) throws IOException {
        write.writeShort(entityToMove.getServerEntityId());
        write.writeByte(direction.getDirectionByte());
    }
}
