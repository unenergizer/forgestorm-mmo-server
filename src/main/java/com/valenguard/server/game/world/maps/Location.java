package com.valenguard.server.game.world.maps;

import com.valenguard.server.ServerMain;
import com.valenguard.server.game.world.entity.Entity;
import lombok.Getter;
import lombok.Setter;

import static com.google.common.base.Preconditions.checkArgument;

@SuppressWarnings("unused")
@Getter
@Setter
public class Location {

    private String mapName;
    private short x;
    private short y;

    public Location(String mapName, short x, short y) {
        this.mapName = mapName;
        this.x = x;
        this.y = y;
    }

    public Location(Location location) {
        this.mapName = location.mapName;
        this.x = location.x;
        this.y = location.y;
    }

    public GameMap getGameMap() {
        return ServerMain.getInstance().getGameManager().getGameMapProcessor().getGameMap(mapName);
    }

    public Location add(Location location) {
        checkArgument(location.getMapName().equals(mapName),
                "Can't add locations. " + location.getMapName() + " doesn't equal " + mapName + ".");
        return new Location(mapName, (short) (this.x + location.getX()), (short) (this.y + location.getY()));
    }

    public void set(Location location) {
        this.mapName = location.mapName;
        this.x = location.x;
        this.y = location.y;
    }

    public Location add(short x, short y) {
        this.x = (short) (this.x + x);
        this.y = (short) (this.y + y);
        return this;
    }

    public boolean isWithinDistance(Entity entity, short distance) {
        return isWithinDistance(entity.getCurrentMapLocation(), distance);
    }

    public boolean isWithinDistance(Location otherLocation, short distance) {
        return getDistanceAway(otherLocation) <= distance;
    }

    public short getDistanceAway(Location otherLocation) {
        int diffX = otherLocation.getX() - x;
        int diffY = otherLocation.getY() - y;

        double realDifference = Math.sqrt((double) (diffX * diffX + diffY * diffY));
        return (short) Math.floor(realDifference);
    }

    public MoveDirection getMoveDirectionFromLocation(Location targetLocation) {
        if (targetLocation.getX() > x) {
            return MoveDirection.EAST;
        } else if (targetLocation.getX() < x) {
            return MoveDirection.WEST;
        } else if (targetLocation.getY() > y) {
            return MoveDirection.NORTH;
        } else if (targetLocation.getY() < y) {
            return MoveDirection.SOUTH;
        }
        return MoveDirection.NONE;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Location)) return false;
        Location otherLocation = (Location) obj;

        if (!otherLocation.getMapName().equals(mapName)) return false;
        if (otherLocation.getX() != x) return false;
        if (otherLocation.getY() != y) return false;
        return true;
    }

    @Override
    public String toString() {
        return "[" + mapName + "] -> [" + x + ", " + y + "]";
    }
}
