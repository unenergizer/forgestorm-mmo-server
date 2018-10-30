package com.valenguard.server.maps.data;

import com.valenguard.server.entity.Entity;
import com.valenguard.server.entity.MoveDirection;

@SuppressWarnings({"unused", "WeakerAccess"})
public class MoveUtil {

    public static MoveDirection getMoveDirection(Location currentLocation, Location futureLocation) {
        return getMoveDirection(currentLocation.getX(), currentLocation.getY(),
                futureLocation.getX(), futureLocation.getY());
    }

    public static MoveDirection getMoveDirection(int currentX, int currentY, int futureX, int futureY) {
        if (currentX > futureX) return MoveDirection.LEFT;
        else if (currentX < futureX) return MoveDirection.RIGHT;
        else if (currentY > futureY) return MoveDirection.DOWN;
        else if (currentY < futureY) return MoveDirection.UP;
        return MoveDirection.NONE;
    }

    public static Location getLocation(TmxMap tmxMap, MoveDirection direction) {
        if (direction == MoveDirection.DOWN) return new Location(tmxMap.getMapName(), 0, -1);
        if (direction == MoveDirection.UP) return new Location(tmxMap.getMapName(), 0, 1);
        if (direction == MoveDirection.LEFT) return new Location(tmxMap.getMapName(), -1, 0);
        if (direction == MoveDirection.RIGHT) return new Location(tmxMap.getMapName(), 1, 0);
        return null;
    }

    public static boolean isEntityMoving(Entity entity) {
        return entity.getCurrentMapLocation().getX() != entity.getFutureMapLocation().getX() || entity.getCurrentMapLocation().getY() != entity.getFutureMapLocation().getY();
    }
}
