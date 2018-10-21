package com.valenguard.server.network.packet.out;

import com.valenguard.server.entity.Direction;
import com.valenguard.server.entity.Entity;
import com.valenguard.server.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class MoveEntityPacket extends ServerOutPacket {

    private Entity entityToMove;
    private Direction direction;

    public MoveEntityPacket(Player player, Entity entityToMove, Direction direction) {
        super(Opcodes.ENTITY_MOVE_UPDATE, player);
        this.direction = direction;
        this.entityToMove = entityToMove;
    }

    @Override
    protected void createPacket(ObjectOutputStream write) throws IOException {
        write.writeShort(entityToMove.getServerEntityId());
        write.writeByte(direction.getDirectionByte());
    }
}
