package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.Server;
import com.valenguard.server.database.AuthenticatedUser;
import com.valenguard.server.database.sql.GameWorldMonsterSQL;
import com.valenguard.server.database.sql.GameWorldNpcSQL;
import com.valenguard.server.game.rpg.Attributes;
import com.valenguard.server.game.rpg.EntityAlignment;
import com.valenguard.server.game.world.entity.*;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.game.world.maps.MoveDirection;
import com.valenguard.server.game.world.maps.Warp;
import com.valenguard.server.network.game.shared.*;
import lombok.AllArgsConstructor;

import static com.valenguard.server.util.Log.println;

@Opcode(getOpcode = Opcodes.ADMIN_EDITOR_ENTITY)
public class AdminEditorEntityPacketIn implements PacketListener<AdminEditorEntityPacketIn.EntityEditorPacketIn> {

    private static final boolean PRINT_DEBUG = true;

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {

        EntityType entityType = EntityType.getEntityType(clientHandler.readByte());

        // Editor data
        boolean spawn = clientHandler.readBoolean();
        boolean save = clientHandler.readBoolean();

        // Basic data
        short entityID = clientHandler.readShort();
        String name = clientHandler.readString();
        String faction = clientHandler.readString();
        int health = clientHandler.readInt();
        int damage = clientHandler.readInt();
        int expDrop = clientHandler.readInt();
        int dropTable = clientHandler.readInt();
        float walkSpeed = clientHandler.readFloat();
        float probStop = clientHandler.readFloat();
        float probWalk = clientHandler.readFloat();
        short shopId = clientHandler.readShort();
        boolean bankKeeper = clientHandler.readBoolean();

        // World data
        String worldName = clientHandler.readString();
        short worldX = clientHandler.readShort();
        short worldY = clientHandler.readShort();

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

        // Appearance
        if (entityType == EntityType.NPC) {
            hairTexture = clientHandler.readByte();
            helmTexture = clientHandler.readByte();
            chestTexture = clientHandler.readByte();
            pantsTexture = clientHandler.readByte();
            shoesTexture = clientHandler.readByte();
            hairColor = clientHandler.readInt();
            eyesColor = clientHandler.readInt();
            skinColor = clientHandler.readInt();
            glovesColor = clientHandler.readInt();
        } else if (entityType == EntityType.MONSTER) {
            monsterBodyTexture = clientHandler.readByte();
        }

        println(PRINT_DEBUG);
        println(getClass(), "=============== NPC EDITOR PACKET IN =============", false, PRINT_DEBUG);
        println(getClass(), "Spawn: " + spawn, false, PRINT_DEBUG);
        println(getClass(), "Save: " + save, false, PRINT_DEBUG);
        println(getClass(), "EntityType: " + entityType, false, PRINT_DEBUG);
        println(getClass(), "EntityID: " + entityID, false, PRINT_DEBUG);
        println(getClass(), "Name: " + name, false, PRINT_DEBUG);
        println(getClass(), "Faction: " + faction, false, PRINT_DEBUG);
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
        println(getClass(), " *** Appearance Data ***", false, PRINT_DEBUG);
        if (entityType == EntityType.MONSTER) {
            println(getClass(), "MonsterBodyTexture: " + monsterBodyTexture, false, PRINT_DEBUG);
        } else if (entityType == EntityType.NPC) {
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

        return new EntityEditorPacketIn(
                entityType,
                spawn,
                save,
                entityID,
                name,
                faction,
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
                monsterBodyTexture,
                hairTexture,
                helmTexture,
                chestTexture,
                pantsTexture,
                shoesTexture,
                hairColor,
                eyesColor,
                skinColor,
                glovesColor
        );
    }

    @Override
    public boolean sanitizePacket(EntityEditorPacketIn packetData) {
        AuthenticatedUser authenticatedUser = packetData.getClientHandler().getAuthenticatedUser();
        boolean isAdmin = authenticatedUser.isAdmin();

        if (!isAdmin) {
            println(getClass(), "Non admin player attempted to create a NPC! XF Account name: " + authenticatedUser.getXfAccountName(), true);
        } else {
            println(getClass(), "Spawning NPC made by NPC Editor");
        }

        return isAdmin;
    }

    @Override
    public void onEvent(EntityEditorPacketIn packetData) {
        Player player = packetData.getClientHandler().getPlayer();
        AiEntity aiEntity = null;

        if (packetData.entityID != -1) {
            if (packetData.entityType == EntityType.NPC) {
                aiEntity = (NPC) packetData.getClientHandler().getPlayer().getGameMap().getAiEntityController().getEntity(packetData.entityID);
            } else if (packetData.entityType == EntityType.MONSTER) {
                aiEntity = (Monster) packetData.getClientHandler().getPlayer().getGameMap().getAiEntityController().getEntity(packetData.entityID);
            }
        } else {
            if (packetData.entityType == EntityType.NPC) {
                aiEntity = new NPC();
            } else if (packetData.entityType == EntityType.MONSTER) {
                aiEntity = new Monster();
            }
        }

        aiEntity.setName(packetData.name);

        if (aiEntity.getEntityType() == EntityType.NPC) {
            ((NPC) aiEntity).setFaction(Server.getInstance().getFactionManager().getFactionByName(packetData.faction));
        } else if (aiEntity.getEntityType() == EntityType.MONSTER) {
            ((Monster) aiEntity).setAlignment(EntityAlignment.FRIENDLY);
        }
        aiEntity.setEntityType(aiEntity.getEntityType());
        aiEntity.setCurrentHealth(packetData.health);
        aiEntity.setMaxHealth(packetData.health);
        aiEntity.setExpDrop(packetData.expDrop);
        aiEntity.setDropTable(packetData.dropTable);
        aiEntity.setMoveSpeed(packetData.walkSpeed);
        aiEntity.setBankKeeper(packetData.bankKeeper);

        aiEntity.setMovementInfo(packetData.probStop, packetData.probWalk, 0, 0, 96, 54);
        Location location = new Location(packetData.worldName, packetData.worldX, packetData.worldY);
        aiEntity.setDefaultSpawnLocation(location);
        aiEntity.setSpawnWarp(new Warp(location, MoveDirection.SOUTH));
        aiEntity.gameMapRegister(aiEntity.getSpawnWarp());

        // Setup appearance
        Appearance appearance = new Appearance(aiEntity);
        aiEntity.setAppearance(appearance);

        if (aiEntity.getEntityType() == EntityType.MONSTER) {
            appearance.setMonsterBodyTexture(packetData.monsterBodyTexture);
        } else if (aiEntity.getEntityType() == EntityType.NPC) {
            appearance.setHairTexture(packetData.hairTexture);
            appearance.setHelmTexture(packetData.helmTexture);
            appearance.setChestTexture(packetData.chestTexture);
            appearance.setPantsTexture(packetData.pantsTexture);
            appearance.setShoesTexture(packetData.shoesTexture);
            appearance.setHairColor(packetData.hairColor);
            appearance.setEyeColor(packetData.eyesColor);
            appearance.setSkinColor(packetData.skinColor);
            appearance.setGlovesColor(packetData.glovesColor);
        }

        // Setup basic attributes.
        Attributes attributes = new Attributes();
        attributes.setDamage(packetData.damage);
        aiEntity.setAttributes(attributes);

        if (packetData.shopId != -1) aiEntity.setShopId(packetData.shopId);

        if (packetData.save && packetData.entityID != -1) {
            if (aiEntity.getEntityType() == EntityType.NPC) {
                new GameWorldNpcSQL().saveSQL((NPC) aiEntity);
            } else if (aiEntity.getEntityType() == EntityType.MONSTER) {
                new GameWorldMonsterSQL().saveSQL((Monster) aiEntity);
            }
            aiEntity.setInstantRespawn(true);
            aiEntity.killAiEntity(null);
        } else if (packetData.save) {
            if (aiEntity.getEntityType() == EntityType.NPC) {
                new GameWorldNpcSQL().firstTimeSaveSQL((NPC) aiEntity);
            } else if (aiEntity.getEntityType() == EntityType.MONSTER) {
                new GameWorldMonsterSQL().firstTimeSaveSQL((Monster) aiEntity);
            }
            player.getGameMap().getAiEntityController().queueEntitySpawn(aiEntity);
        } else {
            if (packetData.spawn) player.getGameMap().getAiEntityController().queueEntitySpawn(aiEntity);
        }
    }

    @AllArgsConstructor
    class EntityEditorPacketIn extends PacketData {

        private EntityType entityType;

        // Editor data
        private boolean spawn;
        private boolean save;

        // Basic data
        private short entityID;
        private String name;
        private String faction;
        private int health;
        private int damage;
        private int expDrop;
        private int dropTable;
        private float walkSpeed;
        private float probStop;
        private float probWalk;
        private short shopId;
        private boolean bankKeeper;

        // World data
        private String worldName;
        private short worldX;
        private short worldY;

        // Appearance
        private byte monsterBodyTexture;
        private byte hairTexture;
        private byte helmTexture;
        private byte chestTexture;
        private byte pantsTexture;
        private byte shoesTexture;
        private int hairColor;
        private int eyesColor;
        private int skinColor;
        private int glovesColor;
    }
}
