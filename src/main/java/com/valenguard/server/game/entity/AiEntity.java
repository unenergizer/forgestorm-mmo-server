package com.valenguard.server.game.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiEntity extends MovingEntity {

    private int aiEntityDataID = -1;
    private int expDrop = 0;
    private Integer dropTable = 0;

    private RandomRegionMoveGenerator randomRegionMoveGenerator;

    public void setMovementInfo(float probabilityStill, float probabilityWalkStart, int bounds1x, int bounds1y, int bounds2x, int bounds2y) {
        randomRegionMoveGenerator = new RandomRegionMoveGenerator(this, probabilityStill, probabilityWalkStart, bounds1x, bounds1y, bounds2x, bounds2y);
    }
}
