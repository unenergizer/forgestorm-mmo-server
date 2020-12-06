package com.forgestorm.server.game.world.maps;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.world.entity.Entity;
import lombok.Getter;
import lombok.Setter;

import static com.google.common.base.Preconditions.checkArgument;

@SuppressWarnings("unused")
@Getter
@Setter
public class Location {

    private String worldName;
    private int x;
    private int y;

    public Location(String worldName, int x, int y) {
        this.worldName = worldName;
        this.x = x;
        this.y = y;
    }

    public Location(Location location) {
        this.worldName = location.worldName;
        this.x = location.x;
        this.y = location.y;
    }

    public GameWorld getGameWorld() {
        return ServerMain.getInstance().getGameManager().getGameWorldProcessor().getGameWorld(worldName);
    }

    public WorldChunk getLocationChunk() {
        return getGameWorld().findChunk(x, y);
    }

    public Location add(Location location) {
        checkArgument(location.getWorldName().equals(worldName),
                "Can't add locations. " + location.getWorldName() + " doesn't equal " + worldName + ".");
        return new Location(worldName, (this.x + location.getX()), (this.y + location.getY()));
    }

    public void set(Location location) {
        this.worldName = location.worldName;
        this.x = location.x;
        this.y = location.y;
    }

    public Location add(int x, int y) {
        this.x = this.x + x;
        this.y = this.y + y;
        return this;
    }

    public boolean isWithinDistance(Entity entity, int distance) {
        return isWithinDistance(entity.getCurrentWorldLocation(), distance);
    }

    public boolean isWithinDistance(Location otherLocation, int distance) {
        return getDistanceAway(otherLocation) <= distance;
    }

    public int getDistanceAway(Location otherLocation) {
        int diffX = otherLocation.getX() - x;
        int diffY = otherLocation.getY() - y;

        double realDifference = Math.sqrt(diffX * diffX + diffY * diffY);
        return (int) Math.floor(realDifference);
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

        if (!otherLocation.getWorldName().equals(worldName)) return false;
        if (otherLocation.getX() != x) return false;
        if (otherLocation.getY() != y) return false;
        return true;
    }

    @Override
    public String toString() {
        return "[" + worldName + "] -> [" + x + ", " + y + "]";
    }
}
