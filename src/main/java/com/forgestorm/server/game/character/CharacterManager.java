package com.forgestorm.server.game.character;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.database.sql.GamePlayerCharacterSQL;
import com.forgestorm.server.database.sql.GamePlayerInventorySQL;
import com.forgestorm.server.game.PlayerConstants;
import com.forgestorm.server.game.UserInterfaceType;
import com.forgestorm.server.game.world.entity.Appearance;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.shared.game.world.item.ItemStack;
import com.forgestorm.server.game.world.item.ItemStackManager;
import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.server.network.game.PlayerSessionData;
import com.forgestorm.server.network.game.packet.out.CharacterMenuLoadPacketOut;
import com.forgestorm.server.network.game.packet.out.InitClientPrivilegePacketOut;
import com.forgestorm.server.network.game.packet.out.InitScreenPacketOut;
import com.forgestorm.server.network.game.packet.out.PingPacketOut;
import com.forgestorm.server.network.game.shared.ClientHandler;
import com.forgestorm.shared.util.color.EyeColorList;
import com.forgestorm.shared.util.color.HairColorList;
import com.forgestorm.shared.util.color.SkinColorList;
import com.forgestorm.server.util.libgdx.Color;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.forgestorm.server.util.Log.println;

public class CharacterManager {

    private static final boolean PRINT_DEBUG = false;

    private final List<String> blackListedNames = new ArrayList<>();

    public CharacterManager() {
        // TODO: Load blacklist of names from database
        blackListedNames.add("admin");
        blackListedNames.add("administrator");
        blackListedNames.add("mod");
        blackListedNames.add("moderator");
        blackListedNames.add("staff");
        blackListedNames.add("owner");
        // TODO: Add more unaccepted names...
    }

    public boolean isNameBlacklisted(String name) {
        for (String s : blackListedNames) {
            if (s.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public boolean isNameUnique(String name) {
        // Check to make sure the players chosen name is unique
        try (Connection connection = ServerMain.getInstance().getDatabaseManager().getHikariDataSource().getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("select * from game_player_characters where name = ?");
            preparedStatement.setString(1, name);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return true;
    }

    public void createCharacter(ClientHandler clientHandler, String characterName, byte hairTexture, byte hairColor, byte eyeColor, byte skinColor) {
        Player player = clientHandler.getPlayer();

        println(getClass(), "Name: " + characterName, false, PRINT_DEBUG);
        println(getClass(), "HairTexture: " + hairTexture, false, PRINT_DEBUG);
        println(getClass(), "HairColor: " + hairColor + " (Ordinal)", false, PRINT_DEBUG);
        println(getClass(), "EyeColor: " + eyeColor + " (Ordinal)", false, PRINT_DEBUG);
        println(getClass(), "SkinColor: " + skinColor + " (Ordinal)", false, PRINT_DEBUG);

        int hairValue = Color.rgba8888(HairColorList.getColorFromOrdinal(hairColor));
        int eyeValue = Color.rgba8888(EyeColorList.getColorFromOrdinal(eyeColor));
        int skinValue = Color.rgba8888(SkinColorList.getColorFromOrdinal(skinColor));

        println(getClass(), "HairColor: " + hairValue + " (int)", false, PRINT_DEBUG);
        println(getClass(), "EyeColor: " + eyeValue + " (int)", false, PRINT_DEBUG);
        println(getClass(), "SkinColor: " + skinValue + " (int)", false, PRINT_DEBUG);

        // Set
        player.setName(characterName);
        player.setFaction((byte) 0); // TODO: Fill in from client
        player.setCurrentHealth(PlayerConstants.BASE_HP);
        player.setFacingDirection(PlayerConstants.SPAWN_FACING_DIRECTION);
        player.setCurrentWorldLocation(new Location(PlayerConstants.START_SPAWN_LOCATION));
        player.setFutureWorldLocation(new Location(PlayerConstants.START_SPAWN_LOCATION));

        player.setCharacterClass(CharacterClasses.FIGHTER);
        player.setCharacterGender(CharacterGenders.MALE);
        player.setCharacterRace(CharacterRaces.HUMAN);

        // TODO: Get bodyID and headID from client!
        Appearance appearance = new Appearance(player);
        appearance.setHairTexture(hairTexture);
        appearance.setHairColor(hairValue);
        appearance.setEyeColor(eyeValue);
        appearance.setSkinColor(skinValue);
        player.setAppearance(appearance);

        // Give the player beginner items
        ItemStackManager itemStackManager = ServerMain.getInstance().getItemStackManager();

        // Default Armor and Weapons
        ItemStack sword = itemStackManager.makeItemStack(PlayerConstants.STARTER_GEAR_SWORD_ID, 1);
        ItemStack chest = itemStackManager.makeItemStack(PlayerConstants.STARTER_GEAR_CHEST_ID, 1);
        ItemStack pants = itemStackManager.makeItemStack(PlayerConstants.STARTER_GEAR_PANTS_ID, 1);
        ItemStack shoes = itemStackManager.makeItemStack(PlayerConstants.STARTER_GEAR_SHOES_ID, 1);
        player.getPlayerEquipment().setItemStack((byte) 10, sword, false);
        player.getPlayerEquipment().setItemStack((byte) 1, chest, false);
        player.getPlayerEquipment().setItemStack((byte) 2, pants, false);
        player.getPlayerEquipment().setItemStack((byte) 3, shoes, false);

        // HotBar Spell Setup
        ItemStack meleeSpell = itemStackManager.makeItemStack(25, 1);
        ItemStack rangedSpell = itemStackManager.makeItemStack(26, 1);
        player.getPlayerHotBar().setItemStack((byte) 0, meleeSpell, false);
        player.getPlayerHotBar().setItemStack((byte) 1, rangedSpell, false);

        // Insert into SQL and then load player defaults!
        new GamePlayerCharacterSQL().firstTimeSaveSQL(player); // Goes first, sets player.databaseId (returned from first time save)
        new GamePlayerInventorySQL().firstTimeSaveSQL(player); // Goes second, needs player.getDatabaseId();

        // Send player back to character select screen and show them all characters (including the one just made)
        sendToCharacterScreen(clientHandler);
    }

    public void deleteCharacter(ClientHandler clientHandler, byte characterId) {
        Player player = clientHandler.getLoadedPlayers().get(characterId);
        new GamePlayerCharacterSQL().softDelete(player);
    }

    public void characterLogin(ClientHandler clientHandler, byte characterId) {
        if (clientHandler.getPlayer().isLoggedInGameWorld()) return;
        println(getClass(), "Sending " + clientHandler.getAuthenticatedUser().getXfAccountName() + " into the game world.");
        clientHandler.setCurrentPlayerId(characterId);
        ServerMain.getInstance().getGameManager().getPlayerProcessor().queuePlayerEnterGameWorld(clientHandler);
    }

    public void characterLogout(ClientHandler clientHandler) {
        println(getClass(), "Character Logout Initialized..");
        ServerMain.getInstance().getGameManager().getPlayerProcessor().queuePlayerQuitGameWorld(clientHandler);

//        sendToCharacterScreen(clientHandler);
    }

    public void clientConnect(PlayerSessionData playerSessionData) {
        ClientHandler clientHandler = playerSessionData.getClientHandler();

        ServerMain.getInstance().getNetworkManager().getOutStreamManager().addClient(clientHandler);
        new PingPacketOut(clientHandler).sendPacket();

        // Tell the client its privileges
        new InitClientPrivilegePacketOut(clientHandler).sendPacket();

        // Send player all their characters
        sendToCharacterScreen(playerSessionData.getClientHandler());
    }

    public void clientDisconnect(ClientHandler clientHandler) {
        clientHandler.setPlayerQuitServer(true);

        // Make sure we save player data!
        for (Player player : clientHandler.getLoadedPlayers().values()) {
            if (player.isLoggedInGameWorld()) {
                ServerMain.getInstance().getGameManager().getPlayerProcessor().queuePlayerQuitGameWorld(clientHandler);
            }
        }

        ServerMain.getInstance().getNetworkManager().getOutStreamManager().removeClient(clientHandler);
        clientHandler.closeConnection();
    }

    public void sendToCharacterScreen(ClientHandler clientHandler) {
        println(getClass(), "Sending " + clientHandler.getAuthenticatedUser().getXfAccountName() + " to the character screen.");

        // Used when logging out of a character and going to the character screen.
        clientHandler.getLoadedPlayers().clear();
        clientHandler.setCurrentPlayerId(null);

        // Send player to the character select screen
        new InitScreenPacketOut(clientHandler, UserInterfaceType.CHARACTER_SELECT).sendPacket();

        // Load all basic character information
        List<CharacterDataOut> characterDataOutList = new GamePlayerCharacterSQL().searchCharacters(clientHandler.getAuthenticatedUser().getDatabaseUserId());

        // Generate a player object for all characters in the database
        clientHandler.loadAllPlayers(characterDataOutList);

        // Send the player all their characters
        new CharacterMenuLoadPacketOut(clientHandler).sendPacket();
    }
}
