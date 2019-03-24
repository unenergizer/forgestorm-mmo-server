package com.valenguard.server.game;

import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.game.world.maps.MoveDirection;

public class PlayerConstants {

    /**
     * How fast our packetReceiver can move around the map.
     */
    static final float DEFAULT_MOVE_SPEED = .4f;

    /**
     * The faction that the player begins as. This will probably be changed.
     */
    public static final String STARTING_FACTION = "THE_EMPIRE";

    /**
     * The facing direction that the player starts out as when they first join the server.
     */
    public static final MoveDirection STARTING_FACING_DIRECTION = MoveDirection.SOUTH;

    /**
     * This is the map new players with join on first login.
     */
    @SuppressWarnings("SpellCheckingInspection")
    public static final String STARTING_MAP = "maintown";

    /**
     * This is the first spawn point of the packetReceiver. This coordinate is based on the coordinate system in Tiled Editor.
     * This Tiled Editor coordinate gets translated later to in-game coordinates.
     */
    public static final short STARTING_X_CORD = 43;

    /**
     * This is the first spawn point of the packetReceiver. This coordinate is based on the coordinate system in Tiled Editor.
     * This Tiled Editor coordinate gets translated later to in-game coordinates.
     */
    public static final short STARTING_Y_CORD = 24;

    /**
     *
     */
    public static final Location START_SPAWN_LOCATION = new Location(STARTING_MAP, STARTING_X_CORD, STARTING_Y_CORD);

    /**
     * This is the first respawn point of the packetReceiver. This coordinate is based on the coordinate system in Tiled Editor.
     * This Tiled Editor coordinate gets translated later to in-game coordinates.
     */
    public static final short RESPAWN_X_CORD = 42;

    /**
     * This is the first respawn point of the packetReceiver. This coordinate is based on the coordinate system in Tiled Editor.
     * This Tiled Editor coordinate gets translated later to in-game coordinates.
     */
    public static final short RESPAWN_Y_CORD = 15;

    /**
     * Player Starting health points
     */
    public static final int BASE_HP = 1000;

    /**
     * Player starting base armor
     */
    static final int BASE_ARMOR = 2;

    /**
     * Player Starting Damage
     */
    static final int BASE_DAMAGE = 10;
}
