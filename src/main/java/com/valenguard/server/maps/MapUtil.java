package com.valenguard.server.maps;


import com.valenguard.server.maps.data.Location;
import com.valenguard.server.maps.data.Tile;
import com.valenguard.server.maps.data.TmxMap;
import com.valenguard.server.maps.data.Warp;

public class MapUtil {


    public static boolean isTraversable(Location location) {
        if (isOutOfBounds(location)) return false;
        return location.getTmxMap().getMap()[location.getX()][location.getY()].isTraversable();
    }

    public static boolean isOutOfBounds(Location location) {
        int x = location.getX();
        int y = location.getY();
        TmxMap tmxMap = location.getTmxMap();
        return x < 0 || x >= tmxMap.getMapWidth() || y < 0 || y >= tmxMap.getMapHeight();
    }

    public static Warp getWarpFromLocation(Location location) {
        return location.getTmxMap().getMap()[location.getX()][location.getY()].getWarp();
    }

    public static boolean locationHasWarp(Location location) {
        System.out.println("[locationHasWarp] Name: " + location.getMapName());
        System.out.println("[locationHasWarp] x: " + location.getX());
        System.out.println("[locationHasWarp] y: " + location.getY());
        return getTileByLocation(location).getWarp() != null;
    }

    public static Tile getTileByLocation(Location location) {
        if (isOutOfBounds(location)) return null;
        return location.getTmxMap().getMap()[location.getX()][location.getY()];
    }
}
