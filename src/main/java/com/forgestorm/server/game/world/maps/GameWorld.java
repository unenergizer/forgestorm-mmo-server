package com.forgestorm.server.game.world.maps;

import com.forgestorm.server.game.PlayerConstants;
import com.forgestorm.server.game.world.tile.TileImage;
import com.forgestorm.server.game.world.tile.properties.TilePropertyTypes;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class GameWorld {

    private final String worldName;
    private final int widthInChunks, heightInChunks;

    private final PlayerController playerController = new PlayerController(this);
    private final AiEntityController aiEntityController = new AiEntityController(this);
    private final StationaryEntityController stationaryEntityController = new StationaryEntityController(this);
    private final ItemStackDropEntityController itemStackDropEntityController = new ItemStackDropEntityController(this);
    private Map<Integer, TileImage[]> layers;

    private final Map<Integer, Warp> tileWarps = new HashMap<Integer, Warp>();


    // TODO: THIS IS THE NEW ONE. DOES NOT HAVE "Map<Integer, TileImage[]> layers" SOMETHING WILL PROBABLY BREAK HERE....
    public GameWorld(String worldName, int widthInChunks, int heightInChunks) {
        this.worldName = worldName;
        this.widthInChunks = widthInChunks;
        this.heightInChunks = heightInChunks;
    }

    // TODO: THIS IS THE OLD ONE. SOMETHING WILL PROBABLY BREAK HERE....
    public GameWorld(String worldName, int widthInChunks, int heightInChunks, Map<Integer, TileImage[]> layers) {
        this.worldName = worldName;
        this.widthInChunks = widthInChunks;
        this.heightInChunks = heightInChunks;
        this.layers = layers;
    }

    public void addTileWarp(int x, int y, Warp warp) {
        tileWarps.put((x << 16) | (y & 0xFFFF), warp);
    }

    public Warp getWarp(int x, int y) {
        if (tileWarps.containsKey((x << 16) | (y & 0xFFFF))) {
            return tileWarps.get((x << 16) | (y & 0xFFFF));
        }
        return null;
    }

    public boolean isMovable(Location location) {
        return !isOutOfBounds(location) && isTraversable(location);
    }

    public boolean isTraversable(Location location) {
        if (isOutOfBounds(location)) return false;
        GameWorld gameWorld = location.getGameWorld();
        for (TileImage[] layer : gameWorld.getLayers().values()) {
            TileImage tileImage = layer[location.getX() + location.getY() * gameWorld.getWidthInChunks()];
            if (tileImage.containsProperty(TilePropertyTypes.COLLISION_BLOCK)) return false;
        }

        return true;
    }

    private boolean isOutOfBounds(Location location) {
        int x = location.getX();
        int y = location.getY();
        return x < 0 || x >= location.getGameWorld().getWidthInChunks() || y < 0 || y >= location.getGameWorld().getHeightInChunks();
    }

    public boolean isOutOfBounds(int x, int y) {
        return x < 0 || x >= widthInChunks || y < 0 || y >= heightInChunks;
    }

    public Warp getWarpFromLocation(Location location) {
        return getWarp(location.getX(), location.getY());
    }

    public boolean locationHasWarp(Location location) {
        return getWarp(location.getX(), location.getY()) != null;
    }

    public TileImage getTileByLocation(Location location) {
//        checkArgument(!isOutOfBounds(location));
//        return location.getGameWorld().getWorld()[location.getX()][location.getY()];
        return location.getGameWorld().getLayers().get(0)[location.getX() + location.getY() * location.getGameWorld().getWidthInChunks()];
    }

    public Location getLocation(MoveDirection direction) {
        if (direction == MoveDirection.SOUTH) return new Location(worldName, 0, -1);
        if (direction == MoveDirection.NORTH) return new Location(worldName, 0, 1);
        if (direction == MoveDirection.WEST) return new Location(worldName, -1, 0);
        if (direction == MoveDirection.EAST) return new Location(worldName, 1, 0);
        if (direction == MoveDirection.NONE) return new Location(worldName, 0, 0);
        throw new RuntimeException("Tried to get a location, but direction could not be determined. WorldName: " + worldName + ", MoveDirection: " + direction);
    }

    public boolean isGraveYardWorld() {
        return worldName.equals(PlayerConstants.RESPAWN_LOCATION.getWorldName());
    }
}
