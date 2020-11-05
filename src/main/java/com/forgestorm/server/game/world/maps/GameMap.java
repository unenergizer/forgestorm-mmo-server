package com.forgestorm.server.game.world.maps;

import com.forgestorm.server.game.PlayerConstants;
import com.forgestorm.server.game.world.tile.TileImage;
import com.forgestorm.server.game.world.tile.properties.TilePropertyTypes;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class GameMap {

    private final String mapName;
    private final int mapWidth, mapHeight;

    private final PlayerController playerController = new PlayerController(this);
    private final AiEntityController aiEntityController = new AiEntityController(this);
    private final StationaryEntityController stationaryEntityController = new StationaryEntityController(this);
    private final ItemStackDropEntityController itemStackDropEntityController = new ItemStackDropEntityController(this);
    private Map<Integer, TileImage[]> layers;

    private final Map<Integer, Warp> tileWarps = new HashMap<Integer, Warp>();

    public void addTileWarp(short x, short y, Warp warp) {
        tileWarps.put((x << 16) | (y & 0xFFFF), warp);
    }

    public Warp getWarp(short x, short y) {
        if (tileWarps.containsKey((x << 16) | (y & 0xFFFF))) {
            return tileWarps.get((x << 16) | (y & 0xFFFF));
        }
        return null;
    }

    public GameMap(String mapName, int mapWidth, int mapHeight, Map<Integer, TileImage[]> layers) {
        this.mapName = mapName;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.layers = layers;
    }

    public boolean isMovable(Location location) {
        return !isOutOfBounds(location) && isTraversable(location);
    }

    public boolean isTraversable(Location location) {
        if (isOutOfBounds(location)) return false;
        GameMap gameMap = location.getGameMap();
        for (TileImage[] layer : gameMap.getLayers().values()) {
            TileImage tileImage = layer[location.getX() + location.getY() * gameMap.getMapWidth()];
            if (tileImage.containsProperty(TilePropertyTypes.COLLISION_BLOCK)) return false;
        }

        return true;
    }

    private boolean isOutOfBounds(Location location) {
        short x = location.getX();
        short y = location.getY();
        return x < 0 || x >= location.getGameMap().getMapWidth() || y < 0 || y >= location.getGameMap().getMapHeight();
    }

    public boolean isOutOfBounds(short x, short y) {
        return x < 0 || x >= mapWidth || y < 0 || y >= mapHeight;
    }

    public Warp getWarpFromLocation(Location location) {
        return getWarp(location.getX(), location.getY());
    }

    public boolean locationHasWarp(Location location) {
        return getWarp(location.getX(), location.getY()) != null;
    }

    public TileImage getTileByLocation(Location location) {
//        checkArgument(!isOutOfBounds(location));
//        return location.getGameMap().getMap()[location.getX()][location.getY()];
        return location.getGameMap().getLayers().get(0)[location.getX() + location.getY() * location.getGameMap().getMapWidth()];
    }

    public Location getLocation(MoveDirection direction) {
        if (direction == MoveDirection.SOUTH) return new Location(mapName, (short) 0, (short) -1);
        if (direction == MoveDirection.NORTH) return new Location(mapName, (short) 0, (short) 1);
        if (direction == MoveDirection.WEST) return new Location(mapName, (short) -1, (short) 0);
        if (direction == MoveDirection.EAST) return new Location(mapName, (short) 1, (short) 0);
        if (direction == MoveDirection.NONE) return new Location(mapName, (short) 0, (short) 0);
        throw new RuntimeException("Tried to get a location, but direction could not be determined. MapName: " + mapName + ", MoveDirection: " + direction);
    }

    public boolean isGraveYardMap() {
        return mapName.equals(PlayerConstants.RESPAWN_LOCATION.getMapName());
    }
}
