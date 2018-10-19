package com.valenguard.server.network.packet.out;

import com.valenguard.server.entity.Entity;
import com.valenguard.server.entity.Player;
import com.valenguard.server.network.shared.Opcodes;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class EntitySpawnPacket extends ServerOutPacket {

    private Entity entityToSpawn;

    public EntitySpawnPacket(Player player, Entity entityToSpawn) {
        super(Opcodes.SPAWN_ENTITY, player);
        this.entityToSpawn = entityToSpawn;
    }

    @Override
    protected void createPacket(ObjectOutputStream write) throws IOException {
        write.writeInt(entityToSpawn.getEntityID());
        write.writeInt(entityToSpawn.getLocation().getX());
        write.writeInt(entityToSpawn.getLocation().getY());
        write.writeFloat(entityToSpawn.getHealth());
        write.writeInt(entityToSpawn.getLevel());
        write.writeUTF(entityToSpawn.getName());
    }
}
