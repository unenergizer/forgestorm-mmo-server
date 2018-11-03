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
    public TmxMap getMapData() {
        return ValenguardMain.getInstance().getMapManager().getMapData(mapName);
    }

    /**
     * Wrapper method to see if this current currentMapLocation is traversable.
     *
     * @return True if this tile can be walked on, false otherwise.
     */
    public boolean isTraversable() {
        return getMapData().isTraversable(x, y);
    }

    /**
     * Wrapper method to check if this currentMapLocation is going out of bounds of the map.
     *
     * @return True if the currentMapLocation is out of bounds, false otherwise.
     */
    public boolean isOutOfBounds() {
        return getMapData().isOutOfBounds(x, y);
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
