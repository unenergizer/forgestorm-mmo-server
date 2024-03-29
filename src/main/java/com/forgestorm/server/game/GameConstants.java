package com.forgestorm.server.game;

import com.forgestorm.server.util.ServerTimeUtil;

public class GameConstants {
    public static final int TICKS_PER_SECOND = 20;

    public static final String MAP_FILE_EXTENSION_TYPE = ".json";
    public static final int MAP_SAVE_INTERVAL_IN_MINUTES = ServerTimeUtil.getMinutes(5);
    public static final int TILE_SIZE = 16;
    public static final int CHUNK_SIZE = 16;

    // NOTE: THIS SHOULD PROBABLY ALWAYS BE BIGGER THAN
    // THE RADIUS ON THE CLIENT SIDE
    public static final int CHUNK_RADIUS = 2;
    public static final int MAX_TILE_SEND = 4 * 4;

    public static final short HUMAN_MAX_HEADS = 79;
    public static final short HUMAN_MAX_BODIES = 59;

    public static final short START_ATTACK_RADIUS = 6;
    public static final short QUIT_ATTACK_RADIUS = 8;

    public static final int GENERAL_RESPAWN_TIME = 60;

    public static final int PLAYERS_TO_PROCESS = 50;

    public static final short MAX_GROUND_ITEMS = 3500;
    public static final short MAX_AI_ENTITIES = 5000;
    public static final short MAX_STATIONARY_ENTITIES = 75;

    public static final short MAX_SHOP_DISTANCE = 5;
    public static final short MAX_BANK_DISTANCE = 5;
    public static final short MAX_INTERACT_DISTANCE = 5;
    public static final byte MAX_CHAT_LENGTH = 127; // Max chat length is 0x7F.

    public static final String WELCOME_MESSAGE = MessageText.SERVER + "Welcome to RetroMMO!";
    public static final String WELCOME_MESSAGE_2 = MessageText.SERVER + "[RED]GAME IS IN ALPHA STATE, EXPECT BUGS! ;)";
}
