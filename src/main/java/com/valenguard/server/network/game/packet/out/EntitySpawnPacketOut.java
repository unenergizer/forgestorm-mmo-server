package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.world.entity.*;
import com.valenguard.server.game.world.maps.MoveDirection;
import com.valenguard.server.network.game.shared.Opcodes;

import static com.google.common.base.Preconditions.checkArgument;
import static com.valenguard.server.util.Log.println;

public class EntitySpawnPacketOut extends AbstractServerOutPacket {

    private static final boolean PRINT_DEBUG = true;
    private final Entity entityToSpawn;

    public EntitySpawnPacketOut(final Player player, final Entity entityToSpawn) {
        super(Opcodes.ENTITY_SPAWN, player.getClientHandler());
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
        Player packetReceiver = clientHandler.getPlayer();

        write.writeShort(entityToSpawn.getServerEntityId());
        write.writeByte(getEntityType(entityToSpawn).getEntityTypeByte());
        if (!(entityToSpawn instanceof Player)) {
            write.writeString(entityToSpawn.getName() + " " + entityToSpawn.getServerEntityId());
        } else {
            write.writeString(entityToSpawn.getName());
        }

        MovingEntity movingEntity = (MovingEntity) entityToSpawn;

        checkArgument(movingEntity.getFacingDirection() != MoveDirection.NONE, "Server tried to send a NONE type face direction!");

        write.writeShort(movingEntity.getFutureMapLocation().getX());
        write.writeShort(movingEntity.getFutureMapLocation().getY());

        Appearance appearance = movingEntity.getAppearance();
        switch (entityToSpawn.getEntityType()) {
            case MONSTER:
                write.writeShort(((AiEntity) entityToSpawn).getShopId());

                write.writeByte(((Monster) entityToSpawn).getAlignment().getEntityAlignmentByte());

                write.writeByte(movingEntity.getFacingDirection().getDirectionByte());
                write.writeFloat(movingEntity.getMoveSpeed());
                write.writeInt(movingEntity.getMaxHealth());
                write.writeInt(movingEntity.getCurrentHealth());

                write.writeByte(appearance.getMonsterBodyTexture());
                break;
            case NPC:
                write.writeShort(((AiEntity) entityToSpawn).getShopId());

                write.writeByte(((NPC) entityToSpawn).getAlignmentByPlayer(packetReceiver).getEntityAlignmentByte());
                write.writeByte(((NPC) entityToSpawn).getFaction());

                write.writeByte(movingEntity.getFacingDirection().getDirectionByte());
                write.writeFloat(movingEntity.getMoveSpeed());
                write.writeInt(movingEntity.getMaxHealth());
                write.writeInt(movingEntity.getCurrentHealth());

                write.writeByte(appearance.getHairTexture());
                write.writeByte(appearance.getHelmTexture());
                write.writeByte(appearance.getChestTexture());
                write.writeByte(appearance.getPantsTexture());
                write.writeByte(appearance.getShoesTexture());
                write.writeByte(appearance.getHairColor());
                write.writeByte(appearance.getEyeColor());
                write.writeByte(appearance.getSkinColor());
                write.writeByte(appearance.getGlovesColor());
                break;
            case CLIENT_PLAYER:
            case PLAYER:
                write.writeByte(movingEntity.getFacingDirection().getDirectionByte());
                write.writeFloat(movingEntity.getMoveSpeed());
                write.writeInt(movingEntity.getMaxHealth());
                write.writeInt(movingEntity.getCurrentHealth());

                write.writeByte(appearance.getHairTexture());
                write.writeByte(appearance.getHelmTexture());
                write.writeByte(appearance.getChestTexture());
                write.writeByte(appearance.getPantsTexture());
                write.writeByte(appearance.getShoesTexture());
                write.writeByte(appearance.getHairColor());
                write.writeByte(appearance.getEyeColor());
                write.writeByte(appearance.getSkinColor());
                write.writeByte(appearance.getGlovesColor());
                break;
        }

        println(getClass(), "===================================", false, PRINT_DEBUG);
        println(getClass(), "entityType: " + (entityToSpawn.equals(packetReceiver) ? EntityType.CLIENT_PLAYER : entityToSpawn.getEntityType()), false, PRINT_DEBUG);
        println(getClass(), "entityId: " + movingEntity.getServerEntityId(), false, PRINT_DEBUG);
        println(getClass(), "entityName: " + movingEntity.getName(), false, PRINT_DEBUG);
        println(getClass(), "tileX: " + movingEntity.getFutureMapLocation().getX(), false, PRINT_DEBUG);
        println(getClass(), "tileY: " + movingEntity.getFutureMapLocation().getY(), false, PRINT_DEBUG);
        println(getClass(), "directional Byte: " + movingEntity.getFacingDirection().getDirectionByte(), false, PRINT_DEBUG);
        println(getClass(), "move speed: " + movingEntity.getMoveSpeed(), false, PRINT_DEBUG);

        println(getClass(), "MonsterBody: " + appearance.getMonsterBodyTexture(), false, PRINT_DEBUG);
        println(getClass(), "Hair: " + appearance.getHairTexture(), false, PRINT_DEBUG);
        println(getClass(), "Helm: " + appearance.getHelmTexture(), false, PRINT_DEBUG);
        println(getClass(), "Chest: " + appearance.getChestTexture(), false, PRINT_DEBUG);
        println(getClass(), "Pants: " + appearance.getPantsTexture(), false, PRINT_DEBUG);
        println(getClass(), "Shoes: " + appearance.getShoesTexture(), false, PRINT_DEBUG);
        println(getClass(), "HairColor: " + appearance.getHairColor(), false, PRINT_DEBUG);
        println(getClass(), "EyeColor: " + appearance.getEyeColor(), false, PRINT_DEBUG);
        println(getClass(), "SkinColor: " + appearance.getSkinColor(), false, PRINT_DEBUG);
        println(getClass(), "GlovesColor: " + appearance.getGlovesColor(), false, PRINT_DEBUG);
    }

    private void spawnStationaryEntity(GameOutputStream write) {
        write.writeShort(entityToSpawn.getServerEntityId());
        write.writeByte(entityToSpawn.getEntityType().getEntityTypeByte());
        write.writeString(entityToSpawn.getName());
        write.writeShort(entityToSpawn.getCurrentMapLocation().getX());
        write.writeShort(entityToSpawn.getCurrentMapLocation().getY());
        write.writeByte(entityToSpawn.getAppearance().getMonsterBodyTexture());
    }

    private void spawnItemStackDrop(GameOutputStream write) {
        write.writeShort(entityToSpawn.getServerEntityId());
        write.writeByte(entityToSpawn.getEntityType().getEntityTypeByte());
        write.writeString(entityToSpawn.getName());
        write.writeShort(entityToSpawn.getCurrentMapLocation().getX());
        write.writeShort(entityToSpawn.getCurrentMapLocation().getY());
        write.writeByte(entityToSpawn.getAppearance().getMonsterBodyTexture());
    }
}
