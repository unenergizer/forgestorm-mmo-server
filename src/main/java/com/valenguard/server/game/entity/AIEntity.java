package com.valenguard.server.game.entity;

import lombok.Getter;

public class AIEntity extends MovingEntity {

    @Getter
    private RandomRegionMoveGenerator randomRegionMoveGenerator;

    public void setDefaultMovement() {
        randomRegionMoveGenerator = new RandomRegionMoveGenerator(this, 0.3f, 0.5f, 0, 0, 49, 49);
    }

    public void setMovementBounds(int bounds1x, int bounds1y, int bounds2x, int bounds2y) {
        randomRegionMoveGenerator = new RandomRegionMoveGenerator(this, 0.3f, 0.5f, bounds1x, bounds1y, bounds2x, bounds2y);
    }

    public void setMovementInfo(float probabilityStill, float probabilityWalkStart, int bounds1x, int bounds1y, int bounds2x, int bounds2y) {
        randomRegionMoveGenerator = new RandomRegionMoveGenerator(this, probabilityStill, probabilityWalkStart, bounds1x, bounds1y, bounds2x, bounds2y);
    }
}
