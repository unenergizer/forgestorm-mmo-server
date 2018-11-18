package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.Entity;
import com.valenguard.server.game.entity.EntityType;
import com.valenguard.server.game.entity.MovingEntity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.maps.MoveDirection;
import com.valenguard.server.network.shared.Opcodes;
import com.valenguard.server.util.Log;

import java.io.IOException;
import java.io.ObjectOutputStream;

import static com.google.common.base.Preconditions.checkArgument;

public class EntitySpawnPacket extends ServerOutPacket {

    private static final boolean PRINT_DEBUG = false;
    private final Entity entityToSpawn;

    public EntitySpawnPacket(Player player, Entity entityToSpawn) {
        super(Opcodes.ENTITY_SPAWN, player);
        this.entityToSpawn = entityToSpawn;
    }

    @Override
    protected void createPacket(ObjectOutputStream write) throws IOException {

        write.writeByte(entityToSpawn.equals(player) ? EntityType.CLIENT_PLAYER.getEntityTypeByte() : entityToSpawn.getEntityType().getEntityTypeByte());
        write.writeShort(entityToSpawn.getServerEntityId());
        write.writeUTF(entityToSpawn.getName());

        if (entityToSpawn instanceof MovingEntity) {
            MovingEntity movingEntity = (MovingEntity) entityToSpawn;

            checkArgument(movingEntity.getFacingDirection() != MoveDirection.NONE, "Server tried to send a NONE type face direction!");

            write.writeInt(movingEntity.getFutureMapLocation().getX());
            write.writeInt(movingEntity.getFutureMapLocation().getY());

            // Writing entity base texture id.
            write.writeShort(entityToSpawn.getAppearance().getTextureId(0)); // head npc / body monster

            switch (entityToSpawn.getEntityType()) {
                case PLAYER:
                case CLIENT_PLAYER:
                case NPC:
                    // Writing additional texture ids.
                    write.writeShort(entityToSpawn.getAppearance().getTextureId(1)); // body
                    break;
            }

            write.writeByte(movingEntity.getFacingDirection().getDirectionByte());
            write.writeFloat(movingEntity.getMoveSpeed());


            Log.println(getClass(), "===================================", false, PRINT_DEBUG);
            Log.println(getClass(), "entityType: " + (entityToSpawn.equals(player) ? EntityType.CLIENT_PLAYER : entityToSpawn.getEntityType()), false, PRINT_DEBUG);
            Log.println(getClass(), "entityId: " + movingEntity.getServerEntityId(), false, PRINT_DEBUG);
            Log.println(getClass(), "entityName: " + movingEntity.getName(), false, PRINT_DEBUG);
            Log.println(getClass(), "tileX: " + movingEntity.getFutureMapLocation().getX(), false, PRINT_DEBUG);
            Log.println(getClass(), "tileY: " + movingEntity.getFutureMapLocation().getY(), false, PRINT_DEBUG);
            Log.println(getClass(), "directional Byte: " + movingEntity.getFacingDirection().getDirectionByte(), false, PRINT_DEBUG);
            Log.println(getClass(), "move speed: " + movingEntity.getMoveSpeed(), false, PRINT_DEBUG);
            for (int i = 0; i < entityToSpawn.getAppearance().getTextureIds().length; i++) {
                Log.println(getClass(), "textureIds #" + i + ": " + entityToSpawn.getAppearance().getTextureIds()[i], false, PRINT_DEBUG);
            }

        } else {

            write.writeInt(entityToSpawn.getCurrentMapLocation().getX());
            write.writeInt(entityToSpawn.getCurrentMapLocation().getY());

            write.writeShort(entityToSpawn.getAppearance().getTextureId(0));

        }
    }
}
