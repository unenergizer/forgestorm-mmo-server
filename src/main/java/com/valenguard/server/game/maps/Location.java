package com.valenguard.server.game.maps;

import com.google.common.base.Preconditions;
import com.valenguard.server.ValenguardMain;
import lombok.Getter;
import lombok.Setter;

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
         Preconditions.checkArgument(location.getMapName().equals(mapName),
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
}
