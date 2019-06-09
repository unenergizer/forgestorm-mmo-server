package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.Server;
import com.valenguard.server.database.AuthenticatedUser;
import com.valenguard.server.database.sql.GameWorldNpcSQL;
import com.valenguard.server.game.rpg.Attributes;
import com.valenguard.server.game.world.entity.Appearance;
import com.valenguard.server.game.world.entity.EntityType;
import com.valenguard.server.game.world.entity.NPC;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.game.world.maps.MoveDirection;
import com.valenguard.server.game.world.maps.Warp;
import com.valenguard.server.network.game.shared.*;
import lombok.AllArgsConstructor;

import static com.valenguard.server.util.Log.println;

@Opcode(getOpcode = Opcodes.ADMIN_EDITOR_NPC)
public class AdminEditorNPCPacketIn implements PacketListener<AdminEditorNPCPacketIn.NpcDataIn> {

    private static final boolean PRINT_DEBUG = true;

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        // Editor data
        boolean spawn = clientHandler.readBoolean();
        boolean save = clientHandler.readBoolean();

        // Basic data
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

        // Appearance
        byte hairTexture = clientHandler.readByte();
        byte helmTexture = clientHandler.readByte();
        byte chestTexture = clientHandler.readByte();
        byte pantsTexture = clientHandler.readByte();
        byte shoesTexture = clientHandler.readByte();
        int hairColor = clientHandler.readInt();
        int eyesColor = clientHandler.readInt();
        int skinColor = clientHandler.readInt();
        int glovesColor = clientHandler.readInt();

        println(PRINT_DEBUG);
        println(getClass(), "=============== NPC EDITOR PACKET IN =============", false, PRINT_DEBUG);
        println(getClass(), "Spawn: " + spawn, false, PRINT_DEBUG);
        println(getClass(), "Save: " + save, false, PRINT_DEBUG);
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
        println(getClass(), "WorldName: " + worldName, false, PRINT_DEBUG);
        println(getClass(), "World X: " + worldX, false, PRINT_DEBUG);
        println(getClass(), "World Y: " + worldY, false, PRINT_DEBUG);
        println(getClass(), "HairTexture: " + hairTexture, false, PRINT_DEBUG);
        println(getClass(), "HelmTexture: " + helmTexture, false, PRINT_DEBUG);
        println(getClass(), "ChestTexture: " + chestTexture, false, PRINT_DEBUG);
        println(getClass(), "PantsTexture: " + pantsTexture, false, PRINT_DEBUG);
        println(getClass(), "ShoesTexture: " + shoesTexture, false, PRINT_DEBUG);
        println(getClass(), "HairColor: " + hairColor, false, PRINT_DEBUG);
        println(getClass(), "EyesColor: " + eyesColor, false, PRINT_DEBUG);
        println(getClass(), "SkinColor: " + skinColor, false, PRINT_DEBUG);
        println(getClass(), "GlovesColor: " + glovesColor, false, PRINT_DEBUG);

        return new NpcDataIn(
                spawn,
                save,
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
    public boolean sanitizePacket(NpcDataIn packetData) {
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
    public void onEvent(NpcDataIn packetData) {
        Player player = packetData.getClientHandler().getPlayer();
        NPC npc = new NPC();

        npc.setName(packetData.name);
        npc.setFaction(Server.getInstance().getFactionManager().getFactionByName(packetData.faction));
        npc.setEntityType(EntityType.NPC);
        npc.setCurrentHealth(packetData.health);
        npc.setMaxHealth(packetData.health);
        npc.setExpDrop(packetData.expDrop);
        npc.setDropTable(packetData.dropTable);
        npc.setMoveSpeed(packetData.walkSpeed);
        npc.setBankKeeper(packetData.bankKeeper);

        npc.setMovementInfo(packetData.probStop, packetData.probWalk, 0, 0, 96, 54);
        npc.setSpawnWarp(new Warp(new Location(packetData.worldName, packetData.worldX, packetData.worldY), MoveDirection.SOUTH));
        npc.gameMapRegister(npc.getSpawnWarp());

        // Setup appearance
        Appearance appearance = new Appearance(npc);
        npc.setAppearance(appearance);
        appearance.setHairTexture(packetData.hairTexture);
        appearance.setHelmTexture(packetData.helmTexture);
        appearance.setChestTexture(packetData.chestTexture);
        appearance.setPantsTexture(packetData.pantsTexture);
        appearance.setShoesTexture(packetData.shoesTexture);
        appearance.setHairColor(packetData.hairColor);
        appearance.setEyeColor(packetData.eyesColor);
        appearance.setSkinColor(packetData.skinColor);
        appearance.setGlovesColor(packetData.glovesColor);

        // Setup basic attributes.
        Attributes attributes = new Attributes();
        attributes.setDamage(packetData.damage);
        npc.setAttributes(attributes);

        if (packetData.shopId != -1) npc.setShopId(packetData.shopId);

        player.getGameMap().getAiEntityController().queueEntitySpawn(npc);

        if (packetData.save) {
            // Do a MySQL save
            new GameWorldNpcSQL().firstTimeSaveSQL(npc);
        }
    }

    @AllArgsConstructor
    class NpcDataIn extends PacketData {
        // Editor data
        private boolean spawn;
        private boolean save;

        // Basic data
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
