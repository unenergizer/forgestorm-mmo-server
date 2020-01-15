package com.valenguard.server.game.character;

import com.valenguard.server.Server;
import com.valenguard.server.database.sql.GamePlayerCharacterSQL;
import com.valenguard.server.game.PlayerConstants;
import com.valenguard.server.game.UserInterfaceType;
import com.valenguard.server.game.world.entity.Appearance;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.network.game.PlayerSessionData;
import com.valenguard.server.network.game.packet.out.CharacterMenuLoadPacketOut;
import com.valenguard.server.network.game.packet.out.InitClientPrivilegePacketOut;
import com.valenguard.server.network.game.packet.out.InitScreenPacketOut;
import com.valenguard.server.network.game.shared.ClientHandler;
import com.valenguard.server.util.color.EyeColorList;
import com.valenguard.server.util.color.HairColorList;
import com.valenguard.server.util.color.SkinColorList;
import com.valenguard.server.util.libgdx.Color;

import java.util.List;

import static com.valenguard.server.util.Log.println;

public class CharacterManager {

    private static final boolean PRINT_DEBUG = false;

    public boolean isNameUnique(String name) {
        // TODO: check to make sure the players chosen name is unique
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
        player.setCurrentMapLocation(new Location(PlayerConstants.START_SPAWN_LOCATION));
        player.setFutureMapLocation(new Location(PlayerConstants.START_SPAWN_LOCATION));

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

        // Insert into SQL and then load player defaults!
        new GamePlayerCharacterSQL().firstTimeSaveSQL(player);

        // Send player back to character select screen and show them all characters (including the one just made)
        sendToCharacterScreen(clientHandler);
    }

    public void deleteCharacter(ClientHandler clientHandler, byte characterId) {
        Player player = clientHandler.getLoadedPlayers().get(characterId);
        new GamePlayerCharacterSQL().softDelete(player);
    }

    public void characterLogin(ClientHandler clientHandler, byte characterId) {
        if (clientHandler.getPlayer().isLoggedInGameWorld()) return;
        clientHandler.setCurrentPlayerId(characterId);
        Server.getInstance().getGameManager().getPlayerProcessor().queuePlayerEnterGameWorld(clientHandler);
    }

    public void characterLogout(ClientHandler clientHandler) {
        println(getClass(), "Character Logout Initialized..");
        Server.getInstance().getGameManager().getPlayerProcessor().queuePlayerQuitGameWorld(clientHandler);

//        sendToCharacterScreen(clientHandler);
    }

    public void clientConnect(PlayerSessionData playerSessionData) {
        ClientHandler clientHandler = playerSessionData.getClientHandler();

        Server.getInstance().getNetworkManager().getOutStreamManager().addClient(clientHandler);

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
                Server.getInstance().getGameManager().getPlayerProcessor().queuePlayerQuitGameWorld(clientHandler);
            }
        }

        Server.getInstance().getNetworkManager().getOutStreamManager().removeClient(clientHandler);
        clientHandler.closeConnection();
    }

    public void sendToCharacterScreen(ClientHandler clientHandler) {
        println(getClass(), "Sending client to the character screen.");

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
