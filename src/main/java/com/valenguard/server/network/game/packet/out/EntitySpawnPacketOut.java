package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.world.entity.*;
import com.valenguard.server.game.world.maps.MoveDirection;
import com.valenguard.server.network.game.shared.Opcodes;

import static com.google.common.base.Preconditions.checkArgument;
import static com.valenguard.server.util.Log.println;

public class EntitySpawnPacketOut extends AbstractServerOutPacket {

    private static final boolean PRINT_DEBUG = false;
    private final Entity entityToSpawn;

    public EntitySpawnPacketOut(Player player, Entity entityToSpawn) {
        super(Opcodes.ENTITY_SPAWN, player);
        this.entityToSpawn = entityToSpawn;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        if (entityToSpawn instanceof MovingEntity) {
            spawnMovingEntity(write);
        } else if (entityToSpawn instanceof StationaryEntity) {
            spawnStationaryEntity(write);
        } else if (entityToSpawn instanceof ItemStackDrop) {
            spawnItemStackDrop(write);
        }
    }

    private void spawnMovingEntity(GameOutputStream write) {
        write.writeByte(getEntityType(entityToSpawn).getEntityTypeByte());
        write.writeShort(entityToSpawn.getServerEntityId());
        write.writeString(entityToSpawn.getName());

        MovingEntity movingEntity = (MovingEntity) entityToSpawn;

        checkArgument(movingEntity.getFacingDirection() != MoveDirection.NONE, "Server tried to send a NONE type face direction!");

        write.writeShort(movingEntity.getFutureMapLocation().getX());
        write.writeShort(movingEntity.getFutureMapLocation().getY());

        Appearance appearance = movingEntity.getAppearance();
        switch (entityToSpawn.getEntityType()) {
            case MONSTER:
                write.writeShort(appearance.getTextureId(Appearance.BODY));
                write.writeShort(((AiEntity) entityToSpawn).getShopId());
                write.writeByte(((Monster) entityToSpawn).getAlignment().getEntityAlignmentByte());
                break;
            case NPC:
                write.writeByte(appearance.getColorId());
                write.writeShort(appearance.getTextureId(Appearance.BODY));
                write.writeShort(appearance.getTextureId(Appearance.HEAD));
                write.writeShort(((AiEntity) entityToSpawn).getShopId());
                write.writeByte(((NPC) entityToSpawn).getAlignmentByPlayer(packetReceiver).getEntityAlignmentByte());
                write.writeByte(((NPC) entityToSpawn).getFaction());
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

        println(getClass(), "===================================", false, PRINT_DEBUG);
        println(getClass(), "entityType: " + (entityToSpawn.equals(packetReceiver) ? EntityType.CLIENT_PLAYER : entityToSpawn.getEntityType()), false, PRINT_DEBUG);
        println(getClass(), "entityId: " + movingEntity.getServerEntityId(), false, PRINT_DEBUG);
        println(getClass(), "entityName: " + movingEntity.getName(), false, PRINT_DEBUG);
        println(getClass(), "tileX: " + movingEntity.getFutureMapLocation().getX(), false, PRINT_DEBUG);
        println(getClass(), "tileY: " + movingEntity.getFutureMapLocation().getY(), false, PRINT_DEBUG);
        println(getClass(), "directional Byte: " + movingEntity.getFacingDirection().getDirectionByte(), false, PRINT_DEBUG);
        println(getClass(), "move speed: " + movingEntity.getMoveSpeed(), false, PRINT_DEBUG);

        for (int i = 0; i < entityToSpawn.getAppearance().getTextureIds().length; i++) {
            println(getClass(), "textureIds #" + i + ": " + entityToSpawn.getAppearance().getTextureIds()[i], false, PRINT_DEBUG);
        }
    }

    private void spawnStationaryEntity(GameOutputStream write) {
        write.writeByte(entityToSpawn.getEntityType().getEntityTypeByte());
        write.writeShort(entityToSpawn.getServerEntityId());
        write.writeString(entityToSpawn.getName());
        write.writeShort(entityToSpawn.getCurrentMapLocation().getX());
        write.writeShort(entityToSpawn.getCurrentMapLocation().getY());
        write.writeShort(entityToSpawn.getAppearance().getTextureId(Appearance.BODY));
    }

    private void spawnItemStackDrop(GameOutputStream write) {
        write.writeByte(entityToSpawn.getEntityType().getEntityTypeByte());
        write.writeShort(entityToSpawn.getServerEntityId());
        write.writeString(entityToSpawn.getName());
        write.writeShort(entityToSpawn.getCurrentMapLocation().getX());
        write.writeShort(entityToSpawn.getCurrentMapLocation().getY());
        write.writeShort(entityToSpawn.getAppearance().getTextureId(Appearance.BODY));
    }
}
