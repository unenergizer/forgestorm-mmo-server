package com.valenguard.server.game;

import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.game.world.maps.MoveDirection;

public class PlayerConstants {

    /**
     * How fast our packetReceiver can move around the map.
     */
    public static final float DEFAULT_MOVE_SPEED = .4f;

    /**
     * The faction that the player begins as. This will probably be changed.
     */
    public static final String STARTING_FACTION = "THE_EMPIRE";

    /**
     * The facing direction that the player starts out as when they first join the server.
     */
    public static final MoveDirection SPAWN_FACING_DIRECTION = MoveDirection.SOUTH;

    /**
     * First player join spawn location.
     */
    public static final Location START_SPAWN_LOCATION = new Location("game_start", (short) 39, (short) 5);

    /**
     * Player death spawn location.
     */
    public static final Location RESPAWN_LOCATION = new Location("game_start", (short) 39, (short) 5);

    /**
     * Player Starting health points
     */
    public static final int BASE_HP = 1000;

    /**
     * Player starting base armor
     */
    public static final int BASE_ARMOR = 2;

    /**
     * Player Starting Damage
     */
    public static final int BASE_DAMAGE = 10;
}
