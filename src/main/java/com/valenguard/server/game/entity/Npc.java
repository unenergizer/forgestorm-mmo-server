package com.valenguard.server.game.entity;

import lombok.Getter;

public class Npc extends MovingEntity {

    // TODO: Scrips, timers, tasks, questing, etc

    @Getter
    private RandomRegionMoveGenerator randomRegionMoveGenerator;

    public Npc() {
        randomRegionMoveGenerator = new RandomRegionMoveGenerator(this, 0.3f, 0.5f, 0, 0, 49, 49);
    }

    public Npc(int bounds1x, int bounds1y, int bounds2x, int bounds2y) {
        randomRegionMoveGenerator = new RandomRegionMoveGenerator(this, 0.3f, 0.5f, bounds1x, bounds1y, bounds2x, bounds2y);
    }
}
