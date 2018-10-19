package com.valenguard.server.util;

/**
 * Provides the server console with formatted text.
 */
public enum ConsoleLogger {
    CHAT,
    NETWORK,
    ERROR,
    INFO,
    SERVER;

    @Override
    public String toString() {
        return "[" + super.toString() + "] ";
    }
}
