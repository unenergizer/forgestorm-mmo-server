package com.valenguard.server.game.entity;

public class ItemStackDrop extends Entity {

    private int timeTillPublicDrop = 60;

    public boolean doItemStackDropTick() {
        timeTillPublicDrop--;
        return timeTillPublicDrop <= 0;
    }

}
