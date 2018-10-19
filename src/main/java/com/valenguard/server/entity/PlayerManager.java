package com.valenguard.server.entity;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.maps.data.Location;
import com.valenguard.server.maps.data.TmxMap;
import com.valenguard.server.network.packet.out.EntitySpawnPacket;
import com.valenguard.server.network.packet.out.InitClientPacket;
import com.valenguard.server.network.packet.out.PingOut;
import com.valenguard.server.network.shared.ClientHandler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    private int lastFakeID = 0; //Temporary player ID. In the future this ID will come from the database.

    private static PlayerManager instance;

    private Map<ClientHandler, Integer> mappedPlayerIds = new ConcurrentHashMap<>();

    private PlayerManager() {
    }

    /**
     * Gets the main instance of this class.
     *
     * @return A singleton instance of this class.
     */
    public static PlayerManager getInstance() {
        if (instance == null) instance = new PlayerManager();
        return instance;
    }

    /**
     * Player has just logged in for the first time. Lets get everyone updated.
     *
     * @param clientHandler The client handle of the player.
     */
    public void onPlayerConnect(ClientHandler clientHandler) {
        //TODO: GET LAST LOGIN INFO FROM DATABASE, UNLESS PLAYER IS TRUE "NEW PLAYER."
        TmxMap tmxMap = ValenguardMain.getInstance().getMapManager().getMapData(NewPlayerConstants.STARTING_MAP);

        // Below we create a starting location for a new player.
        // The Y cord is subtracted from the height of the map.
        // The reason for this is because on the Tiled Editor
        // the Y cord is reversed.  This just makes our job
        // easier if we want to quickly grab a cord from the
        // Tiled Editor without doing the subtraction ourselves.
        Location location = new Location(NewPlayerConstants.STARTING_MAP,
                NewPlayerConstants.STARTING_X_CORD,
                tmxMap.getMapHeight() - NewPlayerConstants.STARTING_Y_CORD);

        Player player = new Player(lastFakeID, location, 2, clientHandler);
        // TODO SET THE FEST OF THE PLAYERS INFORMATION

        EntityManager.getInstance().addEntity(Player.class, (short) lastFakeID, player);
        mappedPlayerIds.put(clientHandler, lastFakeID);

        System.out.println("Writing out initialization information to the player.");
        System.out.println("eID: " + lastFakeID + ", X: " + player.getLocation().getX() + ", Y: " + player.getLocation().getY());

        // Sending out basic information about the client.
        new InitClientPacket(player, true, lastFakeID, location).sendPacket();

        // Sending the player information about themselves.
        new EntitySpawnPacket(player, player);

        // Start sending ping packets
        new PingOut(player).sendPacket();

        // TODO CHANGE HOW THE MAPS ARE STORING INFORMATION ABOUT THE PLAYER AS FROM THE ENTITY MANAGER!!

        // Let's update everyone because a player has joined the map.
        updateMapWithNewPlayer(player, tmxMap);

        // Add the player to the map they are on.
        tmxMap.addPlayer(player);

        //TODO: This should be a ID from the database. Until then, increment the fake ID for the next client connection.
        lastFakeID++;
    }

    /**
     * Player is disconnecting. Lets clean up after them.
     *
     * @param clientHandler The client connection that refers to a specific player.
     */
    public void onPlayerDisconnect(ClientHandler clientHandler) {
        Player player = getPlayer(clientHandler);

        // Let's update everyone because a player has left the map.
        updateMapWithPlayerLeave(player);
        EntityManager.getInstance().removeEntity(Player.class, mappedPlayerIds.get(clientHandler));
        mappedPlayerIds.remove(clientHandler);
    }

    /**
     * Helper method to get a entity via their client handle.
     *
     * @param clientHandler The client handle we will use to find a entity.
     * @return The entity associated with this client handle.
     */
    public Player getPlayer(ClientHandler clientHandler) {
        Player player = EntityManager.getInstance().getEntity(Player.class, mappedPlayerIds.get(clientHandler));
        if (player == null) {
            throw new RuntimeException("Player not found using this ClientHandler.");
        }
        return player;
    }

    /**
     * Handles switching maps for the player passed and spawns them at
     * the spawnLocation on the new map.
     *
     * @param player        The player who is switching maps.
     * @param spawnLocation The new spawn location on the new map.
     */
    public void playerSwitchMap(Player player, Location spawnLocation) {

        // TODO: CHANGE THIS. MAP SWITCHING WAS REMOVED TEMP. D:<

        // Tell the client to switch maps.
        //new PlayerMapChange(player, spawnLocation).sendPacket();

        // Let's update everyone because a player has joined the map.
        updateMapWithNewPlayer(player, spawnLocation.getMapData());

        // Let's update everyone because a player has left the map.
        updateMapWithPlayerLeave(player);

        // Setting the new map for the player
        spawnLocation.getMapData().addPlayer(player);

        // Setting the new location of the player the server.
        player.setLocation(spawnLocation);
    }

    /**
     * Sends out packet information to all the players on the map telling
     * them that a new player has joined and tells the player who joined
     * about all other players on the map.
     *
     * @param playerWhoJoined The player who just joined the map.
     * @param mapToUpdate     The map that the player has just joined.
     */
    private void updateMapWithNewPlayer(Player playerWhoJoined, TmxMap mapToUpdate) {
        mapToUpdate.getPlayerList().forEach(playerOnMap -> {

            // TODO: COME BACK AND ADD THE SPAWN PACKETS FOR ENTITIES

            // Send all players on the map info of the new player.
            // new EntitySpawnPacket(playerOnMap, playerWhoJoined);

            // Send the new player info about all players already on the map.
            //  new EntitySpawnPacket(playerWhoJoined, playerOnMap).sendPacket();
        });
    }

    /**
     * Sends out a packet to everyone indicating that the player has left the map.
     *
     * @param player The player who left the map
     */
    private void updateMapWithPlayerLeave(Player player) {
        // TODO: ADD SOME METHOD TO REMOVE PLAYERS FROM MAPS. THIS WAS REMOVED ! D:<

        player.getLocation().getMapData().getPlayerList().forEach(playerOnMap -> {

            if (player.equals(playerOnMap)) return;

            // Let all the players on the map know about the exit
            //new EntityExitMap(playerOnMap, player).sendPacket();
        });
        // Remove the player from the map they are currently in.
        player.getMapData().removePlayer(player);
    }
}
