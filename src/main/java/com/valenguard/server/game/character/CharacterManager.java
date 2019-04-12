package com.valenguard.server.game.character;

import com.valenguard.server.Server;
import com.valenguard.server.database.sql.GamePlayerCharacterSQL;
import com.valenguard.server.game.PlayerConstants;
import com.valenguard.server.game.ScreenType;
import com.valenguard.server.game.world.entity.Appearance;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.network.game.PlayerSessionData;
import com.valenguard.server.network.game.packet.out.InitScreenPacketOut;
import com.valenguard.server.network.game.shared.ClientHandler;

import static com.valenguard.server.util.Log.println;

public class CharacterManager {

    public boolean isNameUnique(String name) {
        // TODO: check to make sure the players chosen name is unique
        return true;
    }

    public void createCharacter(Player player, String characterName, byte factionId, byte bodyId, byte headId, byte colorId) {

        // Set
        player.setName(characterName);
        player.setFaction(factionId);
        player.setCurrentHealth(PlayerConstants.BASE_HP);
        player.setFacingDirection(PlayerConstants.SPAWN_FACING_DIRECTION);
        player.setCurrentMapLocation(new Location(PlayerConstants.START_SPAWN_LOCATION));
        player.setFutureMapLocation(new Location(PlayerConstants.START_SPAWN_LOCATION));

        short[] initialPlayerTextureIds = new short[4];
        initialPlayerTextureIds[Appearance.BODY] = bodyId;
        initialPlayerTextureIds[Appearance.HEAD] = headId;
        initialPlayerTextureIds[Appearance.ARMOR] = -1;
        initialPlayerTextureIds[Appearance.HELM] = -1;
        player.setAppearance(new Appearance(player, colorId, initialPlayerTextureIds));

        // TODO: Insert into SQL and then load player defaults!
        new GamePlayerCharacterSQL().firstTimeSaveSQL(player);

        // TODO: Send player back to character select screen and show them all characters (including the one just made)
    }

    public void deleteCharacter(Player player, int characterId) {
        // TODO: Soft delete the players character / place deleted flag on character, but do NOT remove entry (for recovery purposes)
    }

    public void characterLogin(Player player, int characterId) {
        // TODO: Get new character ID and load player game

        PlayerSessionData playerSessionData = null;//new PlayerSessionData(tempID, username, clientHandler)
        Server.getInstance().getGameManager().getPlayerProcessor().queuePlayerJoinServer(playerSessionData); // TODO: Redo!
    }

    public void characterLogout(Player player) {

        // TODO: Save player login information and send the character back to menu
    }

    public void playerLogin(PlayerSessionData playerSessionData) {
        println(getClass(), "Player has logged in!");

        Server.getInstance().getNetworkManager().getOutStreamManager().addClient(playerSessionData.getClientHandler());

        // TODO: Send player to the character select screen
        new InitScreenPacketOut(playerSessionData.getClientHandler(), ScreenType.CHARACTER_SELECT).sendPacket();

        // TODO: Send player all their characters

    }

    public void playerLogout(Player player) {
        ClientHandler clientHandler = player.getClientHandler();
        if (clientHandler.isPlayerQuitProcessed()) return; // Check to make sure we only remove the player once
        clientHandler.setPlayerQuitProcessed(true);
    }
}
