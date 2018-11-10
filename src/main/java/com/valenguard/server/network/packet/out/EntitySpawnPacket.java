package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.MovingEntity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.maps.MoveDirection;
import com.valenguard.server.network.shared.Opcodes;
import com.valenguard.server.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;

import static com.google.common.base.Preconditions.checkArgument;

public class EntitySpawnPacket extends ServerOutPacket {

    private static final boolean showDebug = false;
    private final MovingEntity entityToSpawn;

    public EntitySpawnPacket(Player player, MovingEntity entityToSpawn) {
        super(Opcodes.ENTITY_SPAWN, player);
        this.entityToSpawn = entityToSpawn;
    }

    @Override
    protected void createPacket(ObjectOutputStream write) throws IOException {
        checkArgument(entityToSpawn.getFacingDirection() != MoveDirection.NONE, "Server tried to send a NONE type face direction!");

        write.writeShort(entityToSpawn.getServerEntityId());
        write.writeInt(entityToSpawn.getFutureMapLocation().getX());
        write.writeInt(entityToSpawn.getFutureMapLocation().getY());
        write.writeUTF(entityToSpawn.getName());
        write.writeByte(entityToSpawn.getFacingDirection().getDirectionByte());
        write.writeFloat(entityToSpawn.getMoveSpeed());
        write.writeShort(entityToSpawn.getEntityType());

        Log.println(getClass(), "--------------------------", false, showDebug);
        Log.println(getClass(),
                "\nID -> " + entityToSpawn.getServerEntityId() + " --> " + player.getServerEntityId() +
                        "\nMAP -> " + entityToSpawn.getCurrentMapLocation().getMapName() +
                        "\nX -> " + entityToSpawn.getFutureMapLocation().getX() +
                        "\nY -> " + entityToSpawn.getFutureMapLocation().getY() +
                        "\nName -> " + entityToSpawn.getName() +
                        "\nMoveSpeed -> " + entityToSpawn.getMoveSpeed() +
                        "\nFaceDir -> " + entityToSpawn.getFacingDirection().getDirectionByte(), false, showDebug);
    }
}
