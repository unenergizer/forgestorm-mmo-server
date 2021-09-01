package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.database.AuthenticatedUser;
import com.forgestorm.server.database.sql.GameWorldItemStackDropSQL;
import com.forgestorm.server.database.sql.GameWorldMonsterSQL;
import com.forgestorm.server.database.sql.GameWorldNpcSQL;
import com.forgestorm.server.game.rpg.Attributes;
import com.forgestorm.server.game.rpg.EntityAlignment;
import com.forgestorm.server.game.world.entity.*;
import com.forgestorm.server.game.world.item.ItemStack;
import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.server.game.world.maps.MoveDirection;
import com.forgestorm.server.game.world.maps.Warp;
import com.forgestorm.server.network.game.shared.*;
import lombok.AllArgsConstructor;

import static com.forgestorm.server.util.Log.println;

@Opcode(getOpcode = Opcodes.ADMIN_EDITOR_ENTITY)
public class AdminEditorEntityPacketIn implements PacketListener<AdminEditorEntityPacketIn.EntityEditorPacketIn> {

    private static final boolean PRINT_DEBUG = false;

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {

        EntityType entityType = EntityType.getEntityType(clientHandler.readByte());

        // Editor data
        boolean spawn = clientHandler.readBoolean();
        boolean save = clientHandler.readBoolean();
        boolean delete = clientHandler.readBoolean();

        // World data
        String worldName = clientHandler.readString();
        int worldX = clientHandler.readInt();
        int worldY = clientHandler.readInt();
        short worldZ = clientHandler.readShort();

        // Basic data
        short entityID = clientHandler.readShort();
        String name = null;
        FirstInteraction firstInteraction = null;

        String faction = null;
        EntityAlignment entityAlignment = null;

        int health = 0;
        int damage = 0;
        int expDrop = 0;
        int dropTable = 0;
        float walkSpeed = 0;
        float probStop = 0;
        float probWalk = 0;
        short shopId = -1;
        boolean bankKeeper = false;

        // Appearance
        byte monsterBodyTexture = -1;
        byte hairTexture = -1;
        byte helmTexture = -1;
        byte chestTexture = -1;
        byte pantsTexture = -1;
        byte shoesTexture = -1;
        int hairColor = -1;
        int eyesColor = -1;
        int skinColor = -1;
        int glovesColor = -1;

        // ItemStackDrop
        int itemStackId = 0;
        int amount = 0;
        int respawnTimeMin = 0;
        int respawnTimeMax = 0;

        switch (entityType) {
            case NPC:
                name = clientHandler.readString();
                firstInteraction = FirstInteraction.getFirstInteraction(clientHandler.readByte());
                faction = clientHandler.readString();
                health = clientHandler.readInt();
                damage = clientHandler.readInt();
                expDrop = clientHandler.readInt();
                dropTable = clientHandler.readInt();
                walkSpeed = clientHandler.readFloat();
                probStop = clientHandler.readFloat();
                probWalk = clientHandler.readFloat();
                shopId = clientHandler.readShort();
                bankKeeper = clientHandler.readBoolean();

                hairTexture = clientHandler.readByte();
                helmTexture = clientHandler.readByte();
                chestTexture = clientHandler.readByte();
                pantsTexture = clientHandler.readByte();
                shoesTexture = clientHandler.readByte();
                hairColor = clientHandler.readInt();
                eyesColor = clientHandler.readInt();
                skinColor = clientHandler.readInt();
                glovesColor = clientHandler.readInt();
                break;
            case MONSTER:
                name = clientHandler.readString();
                firstInteraction = FirstInteraction.getFirstInteraction(clientHandler.readByte());
                entityAlignment = EntityAlignment.getEntityAlignment(clientHandler.readByte());
                health = clientHandler.readInt();
                damage = clientHandler.readInt();
                expDrop = clientHandler.readInt();
                dropTable = clientHandler.readInt();
                walkSpeed = clientHandler.readFloat();
                probStop = clientHandler.readFloat();
                probWalk = clientHandler.readFloat();
                shopId = clientHandler.readShort();
                bankKeeper = clientHandler.readBoolean();

                monsterBodyTexture = clientHandler.readByte();
                break;
            case ITEM_STACK:
                itemStackId = clientHandler.readInt();
                amount = clientHandler.readInt();
                respawnTimeMin = clientHandler.readInt();
                respawnTimeMax = clientHandler.readInt();
                break;
        }


        println(PRINT_DEBUG);
        println(getClass(), "=============== ENTITY EDITOR PACKET IN =============", false, PRINT_DEBUG);
        println(getClass(), "Spawn: " + spawn, false, PRINT_DEBUG);
        println(getClass(), "Save: " + save, false, PRINT_DEBUG);
        println(getClass(), "EntityType: " + entityType, false, PRINT_DEBUG);
        println(getClass(), "EntityID: " + entityID, false, PRINT_DEBUG);
        println(getClass(), "Name: " + name, false, PRINT_DEBUG);
        println(getClass(), "FirstInteraction: " + firstInteraction, false, PRINT_DEBUG);
        if (faction != null) println(getClass(), "Faction: " + faction, false, PRINT_DEBUG);
        if (entityAlignment != null) println(getClass(), "Alignment: " + entityAlignment, false, PRINT_DEBUG);
        println(getClass(), "Health: " + health, false, PRINT_DEBUG);
        println(getClass(), "Damage: " + damage, false, PRINT_DEBUG);
        println(getClass(), "ExpDrop: " + expDrop, false, PRINT_DEBUG);
        println(getClass(), "DropTable: " + dropTable, false, PRINT_DEBUG);
        println(getClass(), "WalkSpeed: " + walkSpeed, false, PRINT_DEBUG);
        println(getClass(), "ProbStop: " + probStop, false, PRINT_DEBUG);
        println(getClass(), "ProbWalk: " + probWalk, false, PRINT_DEBUG);
        println(getClass(), "ShopID: " + shopId, false, PRINT_DEBUG);
        println(getClass(), "BankKeeper: " + bankKeeper, false, PRINT_DEBUG);
        println(getClass(), "Spawn: " + worldName + " X: " + worldX + ", Y: " + worldY, false, PRINT_DEBUG);
        if (entityType == EntityType.MONSTER) {
            println(getClass(), " *** Appearance Data ***", false, PRINT_DEBUG);
            println(getClass(), "MonsterBodyTexture: " + monsterBodyTexture, false, PRINT_DEBUG);
        } else if (entityType == EntityType.NPC) {
            println(getClass(), " *** Appearance Data ***", false, PRINT_DEBUG);
            println(getClass(), "HairTexture: " + hairTexture, false, PRINT_DEBUG);
            println(getClass(), "HelmTexture: " + helmTexture, false, PRINT_DEBUG);
            println(getClass(), "ChestTexture: " + chestTexture, false, PRINT_DEBUG);
            println(getClass(), "PantsTexture: " + pantsTexture, false, PRINT_DEBUG);
            println(getClass(), "ShoesTexture: " + shoesTexture, false, PRINT_DEBUG);
            println(getClass(), "HairColor: " + hairColor, false, PRINT_DEBUG);
            println(getClass(), "EyesColor: " + eyesColor, false, PRINT_DEBUG);
            println(getClass(), "SkinColor: " + skinColor, false, PRINT_DEBUG);
            println(getClass(), "GlovesColor: " + glovesColor, false, PRINT_DEBUG);
        }
        println(getClass(), "ItemStackID: " + itemStackId, false, PRINT_DEBUG);
        println(getClass(), "Stack Size: " + amount, false, PRINT_DEBUG);
        println(getClass(), "MinRespawnTime: " + respawnTimeMin, false, PRINT_DEBUG);
        println(getClass(), "MaxRespawnTime: " + respawnTimeMax, false, PRINT_DEBUG);

        return new EntityEditorPacketIn(
                entityType,
                spawn,
                save,
                delete,
                entityID,
                name,
                firstInteraction,
                faction,
                entityAlignment,
                health,
                damage,
                expDrop,
                dropTable,
                walkSpeed,
                probStop,
                probWalk,
                shopId,
                bankKeeper,
                worldName,
                worldX,
                worldY,
                worldZ,
                monsterBodyTexture,
                hairTexture,
                helmTexture,
                chestTexture,
                pantsTexture,
                shoesTexture,
                hairColor,
                eyesColor,
                skinColor,
                glovesColor,
                itemStackId,
                amount,
                respawnTimeMin,
                respawnTimeMax
        );
    }

    @Override
    public boolean sanitizePacket(EntityEditorPacketIn packetData) {
        AuthenticatedUser authenticatedUser = packetData.getClientHandler().getAuthenticatedUser();
        boolean isAllowed = authenticatedUser.isAdmin() || authenticatedUser.isContentDeveloper();

        if (!isAllowed) {
            println(getClass(), "Non admin player attempted to create a NPC! XF Account name: " + authenticatedUser.getXfAccountName(), true);
        }

        return isAllowed;
    }

    @Override
    public void onEvent(EntityEditorPacketIn packetData) {
        Player player = packetData.getClientHandler().getPlayer();
        Entity entity = null;

        if (packetData.entityID != -1) {
            if (packetData.entityType == EntityType.NPC) {
                entity = packetData.getClientHandler().getPlayer().getGameWorld().getAiEntityController().getEntity(packetData.entityID);
            } else if (packetData.entityType == EntityType.MONSTER) {
                entity = packetData.getClientHandler().getPlayer().getGameWorld().getAiEntityController().getEntity(packetData.entityID);
            } else if (packetData.entityType == EntityType.ITEM_STACK) {
                entity = packetData.getClientHandler().getPlayer().getGameWorld().getItemStackDropEntityController().getEntity(packetData.entityID);
            }
        } else {
            if (packetData.entityType == EntityType.NPC) {
                entity = new NPC();
                entity.setEntityType(EntityType.NPC);
            } else if (packetData.entityType == EntityType.MONSTER) {
                entity = new Monster();
                entity.setEntityType(EntityType.MONSTER);
            } else if (packetData.entityType == EntityType.ITEM_STACK) {
                entity = new ItemStackDrop();
                entity.setEntityType(EntityType.ITEM_STACK);
            }
        }

        // World Data
        Location spawnLocation = new Location(packetData.worldName, packetData.worldX, packetData.worldY, packetData.worldZ);

        // Appearance
        Appearance appearance = new Appearance(entity);
        entity.setAppearance(appearance);

        // Setup basic attributes.
        Attributes attributes = new Attributes();
        attributes.setDamage(packetData.damage);

        switch (packetData.entityType) {
            case NPC:
                NPC npc = (NPC) entity;
                npc.setName(packetData.name);
                npc.setFirstInteraction(packetData.firstInteraction);
                npc.setFaction(ServerMain.getInstance().getFactionManager().getFactionByName(packetData.faction));
                npc.setCurrentHealth(packetData.health);
                npc.setMaxHealth(packetData.health);
                npc.setExpDrop(packetData.expDrop);
                npc.setDropTable(packetData.dropTable);
                npc.setMoveSpeed(packetData.walkSpeed);
                npc.setBankKeeper(packetData.bankKeeper);

                // World Data
                npc.setRegionLocations(0, 0, 96, 54); // TODO: Get from client
                npc.setMovementInfo(packetData.probStop, packetData.probWalk);
                npc.setDefaultSpawnLocation(spawnLocation);
                npc.gameWorldRegister(new Warp(spawnLocation, MoveDirection.SOUTH));

                // Appearance
                appearance.setHairTexture(packetData.hairTexture);
                appearance.setHelmTexture(packetData.helmTexture);
                appearance.setChestTexture(packetData.chestTexture);
                appearance.setPantsTexture(packetData.pantsTexture);
                appearance.setShoesTexture(packetData.shoesTexture);
                appearance.setHairColor(packetData.hairColor);
                appearance.setEyeColor(packetData.eyesColor);
                appearance.setSkinColor(packetData.skinColor);
                appearance.setGlovesColor(packetData.glovesColor);

                // Combat
                npc.setAttributes(attributes);

                // Shop
                if (packetData.shopId != -1) npc.setShopId(packetData.shopId);
                break;
            case MONSTER:
                Monster monster = (Monster) entity;
                monster.setName(packetData.name);
                monster.setFirstInteraction(packetData.firstInteraction);
                monster.setAlignment(packetData.entityAlignment);
                monster.setCurrentHealth(packetData.health);
                monster.setMaxHealth(packetData.health);
                monster.setExpDrop(packetData.expDrop);
                monster.setDropTable(packetData.dropTable);
                monster.setMoveSpeed(packetData.walkSpeed);
                monster.setBankKeeper(packetData.bankKeeper);

                // World Data
                monster.setRegionLocations(0, 0, 96, 54); // TODO: Get from client
                monster.setMovementInfo(packetData.probStop, packetData.probWalk);
                monster.setDefaultSpawnLocation(spawnLocation);
                monster.gameWorldRegister(new Warp(spawnLocation, MoveDirection.SOUTH));

                // Appearance
                appearance.setMonsterBodyTexture(packetData.monsterBodyTexture);

                // Combat
                monster.setAttributes(attributes);

                // Shop
                if (packetData.shopId != -1) monster.setShopId(packetData.shopId);
                break;
            case ITEM_STACK:
                ItemStackDrop itemStackDrop = (ItemStackDrop) entity;
                itemStackDrop.setCurrentWorldLocation(spawnLocation);
                ItemStack itemStack = ServerMain.getInstance().getItemStackManager().makeItemStack(packetData.itemStackId, packetData.amount);
                itemStackDrop.setItemStack(itemStack);
                itemStackDrop.setSpawnedForAll(true);
                itemStackDrop.setSpawnedFromMonster(false);
                itemStackDrop.setRespawnTimeMin(packetData.respawnTimeMin);
                itemStackDrop.setRespawnTimeMax(packetData.respawnTimeMax);
                appearance.setMonsterBodyTexture((byte) itemStack.getItemId());
                break;
        }

        // Database
        if (packetData.save && packetData.entityID != -1) {
            // Updating entity in database
            if (entity.getEntityType() == EntityType.NPC) {
                println(getClass(), "---> Updating NPC in database", false, PRINT_DEBUG);
                NPC npc = (NPC) entity;
                new GameWorldNpcSQL().saveSQL(npc);
                npc.setInstantRespawn(true);
                npc.killAiEntity(null);
            } else if (entity.getEntityType() == EntityType.MONSTER) {
                println(getClass(), "---> Updating Monster in database", false, PRINT_DEBUG);
                Monster monster = (Monster) entity;
                new GameWorldMonsterSQL().saveSQL(monster);
                monster.setInstantRespawn(true);
                monster.killAiEntity(null);
            } else if (entity.getEntityType() == EntityType.ITEM_STACK) {
                println(getClass(), "---> Updating ItemStackDrop in database", false, PRINT_DEBUG);
                ItemStackDrop itemStackDrop = (ItemStackDrop) entity;
                new GameWorldItemStackDropSQL().saveSQL(itemStackDrop);
                itemStackDrop.removeItemStackDrop();
                ServerMain.getInstance().getGameManager().getGameWorldProcessor().loadItemStackDrop(itemStackDrop.getGameWorld());
            }
        } else if (packetData.save) {
            // Saving new entity
            if (entity.getEntityType() == EntityType.NPC) {
                println(getClass(), "---> Saving new NPC to database", false, PRINT_DEBUG);
                new GameWorldNpcSQL().firstTimeSaveSQL((NPC) entity);
            } else if (entity.getEntityType() == EntityType.MONSTER) {
                println(getClass(), "---> Saving new Monster to database", false, PRINT_DEBUG);
                new GameWorldMonsterSQL().firstTimeSaveSQL((Monster) entity);
            } else if (entity.getEntityType() == EntityType.ITEM_STACK) {
                println(getClass(), "---> Saving new ItemStackDrop to database", false, PRINT_DEBUG);
                new GameWorldItemStackDropSQL().firstTimeSaveSQL((ItemStackDrop) entity);
            }
        } else if (packetData.delete && packetData.entityID != -1) {

            // Remove entity from map
            if (entity instanceof AiEntity) ((AiEntity) entity).removeAiEntity();
            if (entity instanceof ItemStackDrop) ((ItemStackDrop) entity).removeItemStackDrop();

            if (entity.getEntityType() == EntityType.NPC) {
                println(getClass(), "---> Deleting NPC from database", false, PRINT_DEBUG);
                new GameWorldNpcSQL().deleteSQL((NPC) entity);
            } else if (entity.getEntityType() == EntityType.MONSTER) {
                println(getClass(), "---> Deleting Monster from database", false, PRINT_DEBUG);
                new GameWorldMonsterSQL().deleteSQL((Monster) entity);
            } else if (entity.getEntityType() == EntityType.ITEM_STACK) {
                println(getClass(), "---> Deleting ItemStackDrop from database", false, PRINT_DEBUG);
                new GameWorldItemStackDropSQL().deleteSQL((ItemStackDrop) entity);
            }
        } else {
            if (packetData.spawn) {
                if (entity instanceof AiEntity) {
                    player.getGameWorld().getAiEntityController().queueEntitySpawn((AiEntity) entity);
                } else if (entity instanceof ItemStackDrop) {
                    player.getGameWorld().getItemStackDropEntityController().queueEntitySpawn((ItemStackDrop) entity);
                }
            }
        }
    }

    @AllArgsConstructor
    static class EntityEditorPacketIn extends PacketData {

        private final EntityType entityType;

        // Editor data
        private final boolean spawn;
        private final boolean save;
        private final boolean delete;

        // Basic data
        private final short entityID;
        private final String name;
        private final FirstInteraction firstInteraction;
        private final String faction;
        private final EntityAlignment entityAlignment;
        private final int health;
        private final int damage;
        private final int expDrop;
        private final int dropTable;
        private final float walkSpeed;
        private final float probStop;
        private final float probWalk;
        private final short shopId;
        private final boolean bankKeeper;

        // World data
        private final String worldName;
        private final int worldX;
        private final int worldY;
        private final short worldZ;

        // Appearance
        private final byte monsterBodyTexture;
        private final byte hairTexture;
        private final byte helmTexture;
        private final byte chestTexture;
        private final byte pantsTexture;
        private final byte shoesTexture;
        private final int hairColor;
        private final int eyesColor;
        private final int skinColor;
        private final int glovesColor;

        // ItemStackDrop
        private final int itemStackId;
        private final int amount;
        private final int respawnTimeMin;
        private final int respawnTimeMax;
    }
}
