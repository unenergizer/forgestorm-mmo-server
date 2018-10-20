package com.valenguard.server.maps.data;

import com.valenguard.server.entity.Entity;
import com.valenguard.server.entity.Player;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class TmxMap {

    private final String mapName;
    private final int mapWidth;
    private final int mapHeight;
    private final Tile map[][];
    private final List<Player> playerList = new ArrayList<>();
    private List<Entity> entityList;

    public TmxMap(String mapName, int mapWidth, int mapHeight, Tile[][] map, List<Entity> entityList) {
        this.mapName = mapName;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.map = map;
        this.entityList = entityList;
    }

    /**
     * Adds this player to the list of players on this map.
     *
     * @param player The player we will add to this list of players.
     * @throws RuntimeException Tried to add a player that is already on this map.
     */
    public synchronized void addPlayer(Player player) {
        // Make sure we are not adding the player to a map they are already on.
        if (playerList.contains(player)) {
            throw new RuntimeException("Tried to add player to a map they are already on.");
        }

        // Add the player to the list.
        playerList.add(player);

        System.out.println("Added " + player.getClientHandler().getSocket().getInetAddress() + " to map " + mapName + ".");
        System.out.println("Map " + mapName + " now has " + playerList.size() + " players.");
    }

    /**
     * Removes this player from the list of players on this map.
     *
     * @param player The player to remove from this map.
     * @throws RuntimeException Tried to remove a player that is not on this map.
     */
    public synchronized void removePlayer(Player player) {
        // Make sure the player is actually on this map.
        if (!playerList.contains(player)) {
            throw new RuntimeException("Tried to remove a player from a map they are not on.");
        }

        // Remove the player from the list.
        playerList.remove(player);

        System.out.println("Removed " + player.getClientHandler().getSocket().getInetAddress() + " from map " + mapName + ".");
        System.out.println("Map " + mapName + " now has " + playerList.size() + " players.");
    }

    /**
     * Test to see if the tile/coordinate can be walked on.
     *
     * @param x The X grid coordinate a entity is attempting to move to.
     * @param y The Y grid coordinate a entity is attempting to move to.
     * @return True if the tile/coordinate is walkable. False otherwise.
     */
    public boolean isTraversable(int x, int y) {
        if (isOutOfBounds(x, y)) return false;
        return map[x][y].isTraversable();
    }

    /**
     * This is a test to make sure the entity does not go outside the map.
     *
     * @param x The X grid coordinate a entity is attempting to move to.
     * @param y The Y grid coordinate a entity is attempting to move to.
     * @return True if entity is attempting to move outside the map. False otherwise.
     */
    public boolean isOutOfBounds(int x, int y) {
        return x < 0 || x >= mapWidth || y < 0 || y >= mapHeight;
    }

    /**
     * Retrieves a tile by the location passed in. It is assumed that the location
     * is not out of bounds before being passed.
     *
     * @param location the location on the map.
     * @return The tile associated with the location.
     */
    public Tile getTileByLocation(Location location) {
        return map[location.getX()][location.getY()];
    }
}