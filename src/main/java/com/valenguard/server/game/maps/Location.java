package com.valenguard.server.game.maps;

import com.valenguard.server.ValenguardMain;
import lombok.Getter;
import lombok.Setter;

import static com.google.common.base.Preconditions.checkArgument;

@SuppressWarnings("unused")
@Getter
@Setter
public class Location {

    private String mapName;
    private int x;
    private int y;

    public Location(String mapName, int x, int y) {
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
        return ValenguardMain.getInstance().getGameManager().getGameMap(mapName);
    }

    public Location add(Location location) {
        checkArgument(location.getMapName().equals(mapName),
                "Can't add locations. " + location.getMapName() + " doesn't equal " + mapName + ".");
        return new Location(mapName, this.x + location.getX(), this.y + location.getY());
    }

    public void set(Location location) {
        this.mapName = location.mapName;
        this.x = location.x;
        this.y = location.y;
    }

    public void add(int x, int y) {
        this.x = this.x + x;
        this.y = this.y + y;
    }

    public boolean isWithinDistance(Location location, int distance) {
        return (x + distance == location.getX() || x - distance == location.getX() || x == location.getX())
                && (y + distance == location.getY() || y - distance == location.getY() || y == location.getY());
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
