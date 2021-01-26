package com.forgestorm.server.game;

import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.server.game.world.maps.MoveDirection;

public class PlayerConstants {

    /**
     * How fast our player can move around the map.
     */
    public static final float DEFAULT_MOVE_SPEED = 2F;

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
    public static final Location START_SPAWN_LOCATION = new Location("game_start", 0, -1);

    /**
     * Player death spawn location.
     */
    public static final Location RESPAWN_LOCATION = new Location("game_start", 0, -1);

    /**
     * Player Starting health points
     */
    public static final int BASE_HP = 60;

    /**
     * Player starting base armor
     */
    public static final int BASE_ARMOR = 0;

    /**
     * Player Starting Damage
     */
    public static final int BASE_DAMAGE = 10;

    /**
     * PLAYER STARTING GEAR
     */
    public static final int STARTER_GEAR_SWORD_ID = 4;
    public static final int STARTER_GEAR_CHEST_ID = 2;
    public static final int STARTER_GEAR_PANTS_ID = 23;
    public static final int STARTER_GEAR_SHOES_ID = 7;
}
