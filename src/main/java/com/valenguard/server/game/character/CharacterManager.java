package com.valenguard.server.game.character;

import com.valenguard.server.Server;
import com.valenguard.server.database.sql.GamePlayerCharacterSQL;
import com.valenguard.server.game.PlayerConstants;
import com.valenguard.server.game.ScreenType;
import com.valenguard.server.game.world.entity.Appearance;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.network.game.PlayerSessionData;
import com.valenguard.server.network.game.packet.out.CharacterMenuLoadPacketOut;
import com.valenguard.server.network.game.packet.out.InitClientPrivilegePacketOut;
import com.valenguard.server.network.game.packet.out.InitScreenPacketOut;
import com.valenguard.server.network.game.shared.ClientHandler;
import com.valenguard.server.util.ColorList;

import java.util.List;

import static com.valenguard.server.util.Log.println;

public class CharacterManager {

    private static final boolean PRINT_DEBUG = true;

    public boolean isNameUnique(String name) {
        // TODO: check to make sure the players chosen name is unique
        return true;
    }

    public void createCharacter(ClientHandler clientHandler, CharacterClasses characterClass, CharacterGenders characterGender, CharacterRaces characterRace, ColorList characterColor, String characterName) {
        Player player = clientHandler.getPlayer();

        println(getClass(), "Class: " + characterClass.name(), false, PRINT_DEBUG);
        println(getClass(), "Gender: " + characterGender.name(), false, PRINT_DEBUG);
        println(getClass(), "Race: " + characterRace.name(), false, PRINT_DEBUG);
        println(getClass(), "Color: " + characterColor.name(), false, PRINT_DEBUG);
        println(getClass(), "Name: " + characterName, false, PRINT_DEBUG);

        // Set
        player.setName(characterName);
        player.setFaction((byte) 0); // TODO: Fill in from client
        player.setCurrentHealth(PlayerConstants.BASE_HP);
        player.setFacingDirection(PlayerConstants.SPAWN_FACING_DIRECTION);
        player.setCurrentMapLocation(new Location(PlayerConstants.START_SPAWN_LOCATION));
        player.setFutureMapLocation(new Location(PlayerConstants.START_SPAWN_LOCATION));

        player.setCharacterClass(characterClass);
        player.setCharacterGender(characterGender);
        player.setCharacterRace(characterRace);

        // TODO: Get bodyID and headID from client!
        Appearance appearance = new Appearance(player);
        appearance.setHairTexture((byte) 0);
        appearance.setHairColor(-11501569);
        appearance.setEyeColor(-2004671);
        appearance.setSkinColor(-3872513);
        player.setAppearance(appearance);

        // Insert into SQL and then load player defaults!
        new GamePlayerCharacterSQL().firstTimeSaveSQL(player);

        // Send player back to character select screen and show them all characters (including the one just made)
        sendToCharacterScreen(clientHandler);
    }

    public void deleteCharacter(Player player, int characterId) {
        // TODO: Soft delete the players character / place deleted flag on character, but do NOT remove entry (for recovery purposes)
    }

    public void characterLogin(ClientHandler clientHandler, byte characterId) {
        if (clientHandler.getPlayer().isLoggedInGameWorld()) return;
        clientHandler.setCurrentPlayerId(characterId);
        Server.getInstance().getGameManager().getPlayerProcessor().queuePlayerEnterGameWorld(clientHandler);
    }

    public void characterLogout(ClientHandler clientHandler) {
        Server.getInstance().getGameManager().getPlayerProcessor().queuePlayerQuitGameWorld(clientHandler);

        sendToCharacterScreen(clientHandler);
    }

    public void clientConnect(PlayerSessionData playerSessionData) {
        ClientHandler clientHandler = playerSessionData.getClientHandler();

        Server.getInstance().getNetworkManager().getOutStreamManager().addClient(clientHandler);

        // Tell the client its privileges
        new InitClientPrivilegePacketOut(clientHandler, clientHandler.getAuthenticatedUser().isAdmin()).sendPacket();

        // Send player all their characters
        sendToCharacterScreen(playerSessionData.getClientHandler());
    }

    public void clientDisconnect(ClientHandler clientHandler) {
        // Make sure we save player data!
        for (Player player : clientHandler.getLoadedPlayers().values()) {
            if (player.isLoggedInGameWorld()) {
                Server.getInstance().getGameManager().getPlayerProcessor().queuePlayerQuitGameWorld(clientHandler);
            }
        }

        Server.getInstance().getNetworkManager().getOutStreamManager().removeClient(clientHandler);
        clientHandler.closeConnection();
    }

    private void sendToCharacterScreen(ClientHandler clientHandler) {
        // Send player to the character select screen
        new InitScreenPacketOut(clientHandler, ScreenType.CHARACTER_SELECT).sendPacket();

        // Load all basic character information
        List<CharacterDataOut> characterDataOutList = new GamePlayerCharacterSQL().searchCharacters(clientHandler.getAuthenticatedUser().getDatabaseUserId());

        // Generate a player object for all characters in the database
        clientHandler.loadAllPlayers(characterDataOutList);

        // Send the player all their characters
        new CharacterMenuLoadPacketOut(clientHandler).sendPacket();
    }
}
