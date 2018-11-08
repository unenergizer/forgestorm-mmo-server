package com.valenguard.server.game;

class NewPlayerConstants {

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
    public static final int STARTING_X_CORD = 32;

    /**
     * This is the first spawn point of the player. This coordinate is based on the coordinate system in Tiled Editor.
     * This Tiled Editor coordinate gets translated later to in-game coordinates.
     */
    public static final int STARTING_Y_CORD = 38;
}
