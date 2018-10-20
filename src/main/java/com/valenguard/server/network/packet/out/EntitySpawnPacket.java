package com.valenguard.server.network.packet.out;

import com.valenguard.server.entity.Entity;
import com.valenguard.server.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class EntitySpawnPacket extends ServerOutPacket {

    private Entity entityToSpawn;

    public EntitySpawnPacket(Player player, Entity entityToSpawn) {
        super(Opcodes.ENTITY_SPAWN, player);
        this.entityToSpawn = entityToSpawn;
    }

    @Override
    protected void createPacket(ObjectOutputStream write) throws IOException {
        write.writeShort(entityToSpawn.getServerEntityId());
        write.writeInt(entityToSpawn.getLocation().getX());
        write.writeInt(entityToSpawn.getLocation().getY());
        write.writeUTF(entityToSpawn.getName());
        write.writeByte(entityToSpawn.getFacingDirection().getDirectionByte());
        write.writeShort(entityToSpawn.getEntityType());

        System.out.println("[PACKET] " +
                "\nID -> " + entityToSpawn.getServerEntityId() +
                "\nX -> " + entityToSpawn.getLocation().getX() +
                "\nY -> " + entityToSpawn.getLocation().getY() +
                "\nName -> " + entityToSpawn.getName() +
                "\nFaceDir -> " + entityToSpawn.getFacingDirection().getDirectionByte());
    }
}
