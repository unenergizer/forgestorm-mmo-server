package com.valenguard.server.game.maps;

import lombok.Getter;
import lombok.Setter;

import static com.google.common.base.Preconditions.checkArgument;

public class GameMap {

    @Getter
    private final String mapName;
    @Getter
    private final int mapWidth, mapHeight;
    @Getter
    private final Tile map[][];

    @Getter
    private final PlayerController playerController = new PlayerController(this);

    @Getter
    private final AiEntityController aiEntityController = new AiEntityController(this);
    @Getter
    private final StationaryEntityController stationaryEntityController = new StationaryEntityController(this);
    @Getter
    private final ItemStackDropEntityController itemStackDropEntityController = new ItemStackDropEntityController(this);

    // TODO: generate the id from a pool of free ids
    @Getter
    @Setter
    private short lastItemStackDrop = 0;

    public GameMap(String mapName, int mapWidth, int mapHeight, Tile[][] map) {
        this.mapName = mapName;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.map = map;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isMovable(Location location) {
        return !isOutOfBounds(location) && isTraversable(location);
    }

    private boolean isTraversable(Location location) {
        if (isOutOfBounds(location)) return false;
        return location.getGameMap().getMap()[location.getX()][location.getY()].isTraversable();
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
        return location.getGameMap().getMap()[location.getX()][location.getY()].getWarp();
    }

    public boolean locationHasWarp(Location location) {
        return getTileByLocation(location).getWarp() != null;
    }

    private Tile getTileByLocation(Location location) {
        checkArgument(!isOutOfBounds(location));
        return location.getGameMap().getMap()[location.getX()][location.getY()];
    }

    public Location getLocation(MoveDirection direction) {
        if (direction == MoveDirection.SOUTH) return new Location(mapName, (short) 0, (short) -1);
        if (direction == MoveDirection.NORTH) return new Location(mapName, (short) 0, (short) 1);
        if (direction == MoveDirection.WEST) return new Location(mapName, (short) -1, (short) 0);
        if (direction == MoveDirection.EAST) return new Location(mapName, (short) 1, (short) 0);
        if (direction == MoveDirection.NONE) return new Location(mapName, (short) 0, (short) 0);
        throw new RuntimeException("Tried to get a location, but direction could not be determined. MapName: " + mapName + ", MoveDirection: " + direction);
    }
}
