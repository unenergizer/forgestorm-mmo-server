package com.valenguard.server.game;

public class NewPlayerConstants {

    /**
     * How fast our player can move around the map.
     */
    public static final float DEFAULT_MOVE_SPEED = .4f;

    /**
     * This is the map new players with join on first login.
     */
    @SuppressWarnings("SpellCheckingInspection")
    public static final String STARTING_MAP = "maintown";

    /**
     * This is the first spawn point of the player. This coordinate is based on the coordinate system in Tiled Editor.
     * This Tiled Editor coordinate gets translated later to in-game coordinates.
     */
    public static final int STARTING_X_CORD = 43;

    /**
     * This is the first spawn point of the player. This coordinate is based on the coordinate system in Tiled Editor.
     * This Tiled Editor coordinate gets translated later to in-game coordinates.
     */
    public static final int STARTING_Y_CORD = 30;

    /**
     * This is the first respawn point of the player. This coordinate is based on the coordinate system in Tiled Editor.
     * This Tiled Editor coordinate gets translated later to in-game coordinates.
     */
    public static final int RESPAWN_X_CORD = 42;

    /**
     * This is the first respawn point of the player. This coordinate is based on the coordinate system in Tiled Editor.
     * This Tiled Editor coordinate gets translated later to in-game coordinates.
     */
    public static final int RESPAWN_Y_CORD = 15;

    public static final int BASE_HP = 10;
    public static final int BASE_ARMOR = 2;
    public static final int BASE_DAMAGE = 1;
}
