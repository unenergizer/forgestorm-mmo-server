package com.valenguard.server.network.packet.out;

import com.valenguard.server.entity.Entity;
import com.valenguard.server.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class EntitySpawnPacket extends ServerOutPacket {

    private final Entity entityToSpawn;

    public EntitySpawnPacket(Player player, Entity entityToSpawn) {
        super(Opcodes.ENTITY_SPAWN, player);
        this.entityToSpawn = entityToSpawn;
    }

    @Override
    protected void createPacket(ObjectOutputStream write) throws IOException {
        write.writeShort(entityToSpawn.getServerEntityId());
        write.writeUTF(entityToSpawn.getCurrentMapLocation().getMapName());
        write.writeInt(entityToSpawn.getCurrentMapLocation().getX());
        write.writeInt(entityToSpawn.getCurrentMapLocation().getY());
        write.writeUTF(entityToSpawn.getName());
        write.writeByte(entityToSpawn.getFacingDirection().getDirectionByte());
        write.writeFloat(entityToSpawn.getMoveSpeed());
        write.writeShort(entityToSpawn.getEntityType());

        System.out.println("[PACKET] " +
                "\nID -> " + entityToSpawn.getServerEntityId() +
                "\nMAP -> " + entityToSpawn.getCurrentMapLocation().getMapName() +
                "\nX -> " + entityToSpawn.getCurrentMapLocation().getX() +
                "\nY -> " + entityToSpawn.getCurrentMapLocation().getY() +
                "\nName -> " + entityToSpawn.getName() +
                "\nMoveSpeed -> " + entityToSpawn.getMoveSpeed() +
                "\nFaceDir -> " + entityToSpawn.getFacingDirection().getDirectionByte());
    }
}
