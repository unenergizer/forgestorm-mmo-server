package com.valenguard.server.network.shared;

public class Opcodes {

    /**
     * SHARED
     */
    public static final byte PING = 0x01;

    /**
     * SERVER -> CLIENT
     */
    public static final byte INIT_PLAYER_CLIENT = 0x00;
    public static final byte ENTITY_SPAWN = 0x02;
    public static final byte ENTITY_DESPAWN = 0x03;
    public static final byte ENTITY_MOVE_UPDATE = 0x05;
    public static final byte ENTITY_CHANGE_MAP = 0x06;

    /**
     * CLIENT -> SERVER
     */
    public static final byte MOVE_REQUEST = 0x04;

}
