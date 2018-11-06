package com.valenguard.server.entity;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.maps.data.Location;
import com.valenguard.server.maps.data.TmxMap;
import com.valenguard.server.maps.data.Warp;
import com.valenguard.server.network.PingManager;
import com.valenguard.server.network.packet.out.EntityDespawnPacket;
import com.valenguard.server.network.packet.out.EntitySpawnPacket;
import com.valenguard.server.network.packet.out.InitClientPacket;
import com.valenguard.server.network.packet.out.PingOut;
import com.valenguard.server.network.shared.ClientHandler;
import com.valenguard.server.network.shared.ServerConstants;
import lombok.Getter;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class PlayerManager {

    private static final float DEFAULT_MOVE_SPEED = .4f; //TODO MOVE TO SOME DEFAULT CLIENT INFO CLASS OR LOAD FROM FILE/DB
    private short serverEntityID = 0; //Temporary player ID. In the future this ID will come from the database.
    private static PlayerManager instance;
    private final Map<ClientHandler, Short> mappedPlayerIds = new ConcurrentHashMap<>();
    @Getter
    private PingManager pingManager = new PingManager();

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

    public Collection<ClientHandler> getClientHandles() {
        return mappedPlayerIds.keySet();
    }

    /**
     * Player has just logged in for the first time. Lets get everyone updated.
     *
     * @param clientHandler The client handle of the player.
     */
    public void onPlayerConnect(Player player, ClientHandler clientHandler) {
        //TODO: GET LAST LOGIN INFO FROM DATABASE, UNLESS PLAYER IS TRUE "NEW PLAYER."
        TmxMap tmxMap = ValenguardMain.getInstance().getMapManager().getTmxMap(NewPlayerConstants.STARTING_MAP);

        // Below we create a starting currentMapLocation for a new player.
        // The Y cord is subtracted from the height of the map.
        // The reason for this is because on the Tiled Editor
        // the Y cord is reversed.  This just makes our job
        // easier if we want to quickly grab a cord from the
        // Tiled Editor without doing the subtraction ourselves.
        Location location = new Location(NewPlayerConstants.STARTING_MAP,
                NewPlayerConstants.STARTING_X_CORD,
                tmxMap.getMapHeight() - NewPlayerConstants.STARTING_Y_CORD);

        player.setServerEntityId(serverEntityID);
        player.setCurrentMapLocation(location);
        player.setFutureMapLocation(location);
        player.setMoveSpeed(DEFAULT_MOVE_SPEED);
        player.setClientHandler(clientHandler);

        // TODO SET THE FEST OF THE PLAYERS INFORMATION

        player.setFacingDirection(MoveDirection.DOWN);

        EntityManager.getInstance().addEntity(serverEntityID, player);
        mappedPlayerIds.put(clientHandler, serverEntityID);

        // Sending out basic information about the client.
        new InitClientPacket(player, true, serverEntityID, location).sendPacket();

        // Spawning the player for themselves.
        new EntitySpawnPacket(player, player).sendPacket();

        // Start sending ping packets
        // TODO: Determine if we should start this here or wait until everyone else's ping happens
        new PingOut(player).sendPacket();

        // TODO CHANGE HOW THE MAPS ARE STORING INFORMATION ABOUT THE PLAYER AS FROM THE ENTITY MANAGER!!

        // Let's update everyone because a player has joined the map.
        updateMapWithNewPlayer(player, tmxMap);

        //TODO: This should be a ID from the database. Until then, increment the fake ID for the next client connection.
        serverEntityID++;
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
        EntityManager.getInstance().removeEntity(mappedPlayerIds.get(clientHandler));
        mappedPlayerIds.remove(clientHandler);
    }

    /**
     * Helper method to get a entity via their client handle.
     *
     * @param clientHandler The client handle we will use to find a entity.
     * @return The entity associated with this client handle.
     */
    public Player getPlayer(ClientHandler clientHandler) {
        Player player = (Player) EntityManager.getInstance().getEntity(mappedPlayerIds.get(clientHandler));
        if (player == null) {
            throw new RuntimeException("Player not found using this ClientHandler.");
        }
        return player;
    }

    /**
     * Handles switching maps for the player passed and spawns them at
     * the spawnLocation on the new map.
     */
    public void playerSwitchMap(Player player, Warp warp) {

        // Despawn on current map
        updateMapWithPlayerLeave(player);

        System.out.println();
        System.out.println("[WARP] Name: " + warp.getMapName());
        System.out.println("[WARP] x: " + warp.getToX());
        System.out.println("[WARP] y: " + warp.getToY());

        Location warpDestination = new Location(warp.getMapName(), warp.getToX(), warp.getToY());
        player.setCurrentMapLocation(warpDestination);
        player.setFutureMapLocation(warpDestination);
        player.setFacingDirection(warp.getMoveDirection());
        player.setRealX(player.getFutureMapLocation().getX() * ServerConstants.TILE_SIZE);
        player.setRealY(player.getFutureMapLocation().getY() * ServerConstants.TILE_SIZE);

        // Spawn on target map
        updateMapWithNewPlayer(player, warpDestination.getTmxMap());
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
        // Add the player to the map they are on.
        mapToUpdate.addPlayer(playerWhoJoined);

        mapToUpdate.getPlayerList().forEach(playerOnMap -> {

            // Send all players on the map info of the new player.
            new EntitySpawnPacket(playerOnMap, playerWhoJoined).sendPacket();

            // Send the new player info about all players already on the map.
            new EntitySpawnPacket(playerWhoJoined, playerOnMap).sendPacket();
        });
    }

    /**
     * Sends out a packet to everyone indicating that the player has left the map.
     *
     * @param despawnTarget The player who left the map
     */
    private void updateMapWithPlayerLeave(Player despawnTarget) {
        despawnTarget.getTmxMap().removePlayer(despawnTarget);
        sendToAllButPlayer(despawnTarget, clientHandler ->
                new EntityDespawnPacket(clientHandler.getPlayer(), despawnTarget).sendPacket());
    }

    public void sendToAllButPlayer(Player player, Consumer<ClientHandler> callback) {
        player.getTmxMap().getPlayerList().forEach(playerOnMap -> {
            if (player.equals(playerOnMap)) return;
            callback.accept(playerOnMap.getClientHandler());
        });
    }
}
