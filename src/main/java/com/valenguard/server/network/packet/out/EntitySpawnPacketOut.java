package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.*;
import com.valenguard.server.game.maps.MoveDirection;
import com.valenguard.server.network.shared.Opcodes;
import com.valenguard.server.util.Log;

import static com.google.common.base.Preconditions.checkArgument;

public class EntitySpawnPacketOut extends ServerAbstractOutPacket {

    private static final boolean PRINT_DEBUG = false;
    private final Entity entityToSpawn;

    public EntitySpawnPacketOut(Player player, Entity entityToSpawn) {
        super(Opcodes.ENTITY_SPAWN, player);
        this.entityToSpawn = entityToSpawn;
    }

    @Override
    protected void createPacket(ValenguardOutputStream write) {

        if (entityToSpawn instanceof MovingEntity) {
            spawnMovingEntity(write);
        } else if (entityToSpawn instanceof StationaryEntity) {
            spawnStationaryEntity(write);
        } else if (entityToSpawn instanceof ItemStackDrop) {
            spawnItemStackDrop(write);
        }
    }

    private void spawnMovingEntity(ValenguardOutputStream write) {
        write.writeByte(entityToSpawn.equals(player) ? EntityType.CLIENT_PLAYER.getEntityTypeByte() : entityToSpawn.getEntityType().getEntityTypeByte());
        write.writeShort(entityToSpawn.getServerEntityId());
        write.writeString(entityToSpawn.getName());

        MovingEntity movingEntity = (MovingEntity) entityToSpawn;

        checkArgument(movingEntity.getFacingDirection() != MoveDirection.NONE, "Server tried to send a NONE type face direction!");

        write.writeShort(movingEntity.getFutureMapLocation().getX());
        write.writeShort(movingEntity.getFutureMapLocation().getY());

        Appearance appearance = movingEntity.getAppearance();
        switch (entityToSpawn.getEntityType()) {
            case SKILL_NODE:
            case MONSTER:
            case ITEM_STACK:
                write.writeShort(appearance.getTextureId(Appearance.BODY));
                break;
            case NPC:
                write.writeByte(appearance.getColorId());
                write.writeShort(appearance.getTextureId(Appearance.BODY));
                write.writeShort(appearance.getTextureId(Appearance.HEAD));
                break;
            case CLIENT_PLAYER:
            case PLAYER:
                write.writeByte(appearance.getColorId());
                write.writeShort(appearance.getTextureId(Appearance.BODY));
                write.writeShort(appearance.getTextureId(Appearance.HEAD));
                write.writeShort(appearance.getTextureId(Appearance.ARMOR));
                write.writeShort(appearance.getTextureId(Appearance.HELM));
                break;
        }

        write.writeByte(movingEntity.getFacingDirection().getDirectionByte());
        write.writeFloat(movingEntity.getMoveSpeed());

        // send hp
        write.writeInt(movingEntity.getMaxHealth());
        write.writeInt(movingEntity.getCurrentHealth());

        // send alignment
        write.writeByte(movingEntity.getEntityAlignment().getEntityAlignmentByte());

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
    }

    private void spawnStationaryEntity(ValenguardOutputStream write) {
        write.writeByte(entityToSpawn.getEntityType().getEntityTypeByte());
        write.writeShort(entityToSpawn.getServerEntityId());
        write.writeString(entityToSpawn.getName());
        write.writeShort(entityToSpawn.getCurrentMapLocation().getX());
        write.writeShort(entityToSpawn.getCurrentMapLocation().getY());
        write.writeShort(entityToSpawn.getAppearance().getTextureId(0));
    }

    private void spawnItemStackDrop(ValenguardOutputStream write) {
        write.writeByte(entityToSpawn.getEntityType().getEntityTypeByte());
        write.writeShort(entityToSpawn.getServerEntityId());
        write.writeString(entityToSpawn.getName());
        write.writeShort(entityToSpawn.getCurrentMapLocation().getX());
        write.writeShort(entityToSpawn.getCurrentMapLocation().getY());
        write.writeShort(entityToSpawn.getAppearance().getTextureId(0));
    }
}
