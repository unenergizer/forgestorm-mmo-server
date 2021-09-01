package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.database.AuthenticatedUser;
import com.forgestorm.server.game.world.entity.*;
import com.forgestorm.server.game.world.maps.MoveDirection;
import com.forgestorm.server.network.game.shared.Opcodes;

import static com.forgestorm.server.util.Log.println;
import static com.google.common.base.Preconditions.checkArgument;

public class EntitySpawnPacketOut extends AbstractServerOutPacket {

    private static final boolean PRINT_DEBUG = false;
    private final Entity entityToSpawn;
    private final Player packetReceiver;
    private final AuthenticatedUser authenticatedUser;

    public EntitySpawnPacketOut(final Player player, final Entity entityToSpawn) {
        super(Opcodes.ENTITY_SPAWN, player.getClientHandler());
        this.entityToSpawn = entityToSpawn;
        packetReceiver = player;
        authenticatedUser = packetReceiver.getClientHandler().getAuthenticatedUser();
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        // TODO: Remove object reference from createPacket method
        if (entityToSpawn instanceof MovingEntity) {
            spawnMovingEntity(write);
        } else if (entityToSpawn instanceof StationaryEntity) {
            spawnStationaryEntity(write);
        } else if (entityToSpawn instanceof ItemStackDrop) {
            spawnItemStackDrop(write);
        }
    }

    @SuppressWarnings("DanglingJavadoc")
    private void spawnMovingEntity(GameOutputStream write) {

        write.writeShort(entityToSpawn.getServerEntityId());

        write.writeByte(detectEntityType(entityToSpawn).getEntityTypeByte());
        write.writeString(entityToSpawn.getName());

        MovingEntity movingEntity = (MovingEntity) entityToSpawn;

        checkArgument(movingEntity.getFacingDirection() != MoveDirection.NONE, "Server tried to send a NONE type face direction!");

        write.writeInt(movingEntity.getFutureWorldLocation().getX());
        write.writeInt(movingEntity.getFutureWorldLocation().getY());
        write.writeShort(movingEntity.getFutureWorldLocation().getZ());

        Appearance appearance = movingEntity.getAppearance();
        switch (entityToSpawn.getEntityType()) {
            case MONSTER:
                Monster monster = (Monster) entityToSpawn;
                if (authenticatedUser.isAdmin() || authenticatedUser.isContentDeveloper()) {
                    write.writeInt(movingEntity.getAttributes().getDamage());
                    write.writeInt(monster.getExpDrop());
                    write.writeInt(monster.getDropTable());
                    write.writeFloat(monster.getRandomRegionMoveGenerator().getProbabilityStill());
                    write.writeFloat(monster.getRandomRegionMoveGenerator().getProbabilityWalkStart());
                    write.writeString(monster.getDefaultSpawnLocation().getWorldName());
                    write.writeInt(monster.getDefaultSpawnLocation().getX());
                    write.writeInt(monster.getDefaultSpawnLocation().getY());
                    write.writeShort(monster.getDefaultSpawnLocation().getZ());
                }

                write.writeByte(FirstInteraction.getByte(monster.getFirstInteraction()));
                write.writeShort(monster.getShopId());

                write.writeByte(monster.getAlignment().getEntityAlignmentByte());

                write.writeByte(monster.getFacingDirection().getDirectionByte());
                write.writeFloat(monster.getMoveSpeed());
                write.writeInt(monster.getMaxHealth());
                write.writeInt(monster.getCurrentHealth());

                write.writeByte(appearance.getMonsterBodyTexture());
                break;
            case NPC:
                NPC npc = (NPC) entityToSpawn;
                if (authenticatedUser.isAdmin() || authenticatedUser.isContentDeveloper()) {
                    write.writeInt(npc.getAttributes().getDamage());
                    write.writeInt(npc.getExpDrop());
                    write.writeInt(npc.getDropTable());
                    write.writeFloat(npc.getRandomRegionMoveGenerator().getProbabilityStill());
                    write.writeFloat(npc.getRandomRegionMoveGenerator().getProbabilityWalkStart());
                    write.writeString(npc.getDefaultSpawnLocation().getWorldName());
                    write.writeInt(npc.getDefaultSpawnLocation().getX());
                    write.writeInt(npc.getDefaultSpawnLocation().getY());
                    write.writeShort(npc.getDefaultSpawnLocation().getZ());
                }

                /*******************************************************************
                 * IF THE NPC IS HOSTILE TOWARDS THE PLAYER, MAKE SURE TO SEND THEM
                 * THE ATTACK INTERACTION. NO REASON TO OPEN PLAYER BANK OR ENTITY
                 * STORE IF ENTITY HATES THE PLAYER.
                 ******************************************************************/
                write.writeByte(FirstInteraction.getByte(npc.getFirstInteractionBasedOnAlignment(packetReceiver)));
                write.writeShort(npc.getShopIdBasedOnAlignment(packetReceiver));
                write.writeBoolean(npc.getBankKeeperStatusBasedOnAlignment(packetReceiver));

                write.writeByte(npc.getAlignmentByPlayer(packetReceiver).getEntityAlignmentByte());
                write.writeByte(npc.getFaction());

                write.writeByte(npc.getFacingDirection().getDirectionByte());
                write.writeFloat(npc.getMoveSpeed());
                write.writeInt(npc.getMaxHealth());
                write.writeInt(npc.getCurrentHealth());

                write.writeByte(appearance.getHairTexture());
                write.writeByte(appearance.getHelmTexture());
                write.writeByte(appearance.getChestTexture());
                write.writeByte(appearance.getPantsTexture());
                write.writeByte(appearance.getShoesTexture());
                write.writeInt(appearance.getHairColor());
                write.writeInt(appearance.getEyeColor());
                write.writeInt(appearance.getSkinColor());
                write.writeInt(appearance.getGlovesColor());
                write.writeByte(appearance.getLeftHandTexture());
                write.writeByte(appearance.getRightHandTexture());

                write.writeInt(npc.getScriptId());
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
                write.writeInt(appearance.getHairColor());
                write.writeInt(appearance.getEyeColor());
                write.writeInt(appearance.getSkinColor());
                write.writeInt(appearance.getGlovesColor());
                write.writeByte(appearance.getLeftHandTexture());
                write.writeByte(appearance.getRightHandTexture());
                break;
        }

        if (entityToSpawn.getEntityType() != EntityType.PLAYER) return;
        println(getClass(), "===[ SPAWN OUT -> " + this.packetReceiver.getName() + " ]================================", false, PRINT_DEBUG);
        println(getClass(), "entityType: " + (entityToSpawn.equals(packetReceiver) ? EntityType.CLIENT_PLAYER : entityToSpawn.getEntityType()), false, PRINT_DEBUG);
        println(getClass(), "entityId: " + movingEntity.getServerEntityId(), false, PRINT_DEBUG);
        println(getClass(), "entityName: " + movingEntity.getName(), false, PRINT_DEBUG);
        println(getClass(), "MapName: " + movingEntity.getFutureWorldLocation().getWorldName(), false, PRINT_DEBUG);
        println(getClass(), "tileX: " + movingEntity.getFutureWorldLocation().getX(), false, PRINT_DEBUG);
        println(getClass(), "tileY: " + movingEntity.getFutureWorldLocation().getY(), false, PRINT_DEBUG);
        println(getClass(), "tileZ: " + movingEntity.getFutureWorldLocation().getZ(), false, PRINT_DEBUG);
        println(getClass(), "directional Byte: " + movingEntity.getFacingDirection().getDirectionByte(), false, PRINT_DEBUG);
        println(getClass(), "move speed: " + movingEntity.getMoveSpeed(), false, PRINT_DEBUG);
        println(getClass(), "MaxHP: " + movingEntity.getMaxHealth(), false, PRINT_DEBUG);
        println(getClass(), "CurrentHp: " + movingEntity.getCurrentHealth(), false, PRINT_DEBUG);

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
        println(getClass(), "LeftHand: " + appearance.getLeftHandTexture(), false, PRINT_DEBUG);
        println(getClass(), "RightHand: " + appearance.getRightHandTexture(), false, PRINT_DEBUG);
    }

    private void spawnStationaryEntity(GameOutputStream write) {
        write.writeShort(entityToSpawn.getServerEntityId());
        write.writeByte(entityToSpawn.getEntityType().getEntityTypeByte());
        write.writeString(entityToSpawn.getName());
        write.writeInt(entityToSpawn.getCurrentWorldLocation().getX());
        write.writeInt(entityToSpawn.getCurrentWorldLocation().getY());
        write.writeShort(entityToSpawn.getCurrentWorldLocation().getZ());
        write.writeByte(entityToSpawn.getAppearance().getMonsterBodyTexture());
    }

    private void spawnItemStackDrop(GameOutputStream write) {
        ItemStackDrop itemStackDrop = (ItemStackDrop) entityToSpawn;
        write.writeShort(itemStackDrop.getServerEntityId());
        write.writeByte(itemStackDrop.getEntityType().getEntityTypeByte());
        write.writeString(itemStackDrop.getName());
        write.writeInt(itemStackDrop.getCurrentWorldLocation().getX());
        write.writeInt(itemStackDrop.getCurrentWorldLocation().getY());
        write.writeShort(itemStackDrop.getCurrentWorldLocation().getZ());

        if (authenticatedUser.isAdmin() || authenticatedUser.isContentDeveloper()) {
            write.writeBoolean(itemStackDrop.isSpawnedFromMonster());
            write.writeInt(itemStackDrop.getItemStack().getItemId());
            write.writeInt(itemStackDrop.getItemStack().getAmount());
            write.writeInt(itemStackDrop.getRespawnTimeMin());
            write.writeInt(itemStackDrop.getRespawnTimeMax());
        }

        write.writeByte(itemStackDrop.getAppearance().getMonsterBodyTexture());
    }
}
