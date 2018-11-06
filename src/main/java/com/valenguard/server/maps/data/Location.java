package com.valenguard.server.maps.data;

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

    /**
     * Helper method to quickly get the map data for this currentMapLocation object.
     *
     * @return The map data that relates to this currentMapLocation object.
     */
    public TmxMap getTmxMap() {
        return ValenguardMain.getInstance().getMapManager().getTmxMap(mapName);
    }

    /**
     * Adds two locations coordinates together.
     *
     * @param location The currentMapLocation to add to this.
     * @return A new currentMapLocation with added X and Y coordinates.
     */
    public Location add(Location location) {
        if (!location.getMapName().equals(mapName))
            throw new RuntimeException("Can't add locations. " + location.getMapName() + " doesn't equal " + mapName + ".");
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
