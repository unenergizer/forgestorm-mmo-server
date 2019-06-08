package com.valenguard.server.game.world.entity;

import com.valenguard.server.game.world.maps.GameMap;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.game.world.maps.MoveDirection;
import lombok.Getter;
import lombok.Setter;

import java.util.Random;

@Getter
public class RandomRegionMoveGenerator {

    private final AiEntity aiEntity;

    private final float probabilityStill;
    private final float probabilityWalkStart;

    private final int regionStartX;
    private final int regionStartY;
    private final int regionEndX;
    private final int regionEndY;

    RandomRegionMoveGenerator(AiEntity aiEntity, float probabilityStill, float probabilityWalkStart,
                              int regionStartX, int regionStartY, int regionEndX, int regionEndY) {
        this.aiEntity = aiEntity;
        this.probabilityStill = probabilityStill;
        this.probabilityWalkStart = probabilityWalkStart;
        this.regionStartX = Math.min(regionStartX, regionEndX);
        this.regionEndX = Math.max(regionStartX, regionEndX);
        this.regionStartY = Math.min(regionStartY, regionEndY);
        this.regionEndY = Math.max(regionStartY, regionEndY);
    }

    private int tickCount = 0;

    @Setter
    private boolean alreadyDeterminedMove = false;

    public MoveDirection generateMoveDirection(boolean isMoving) {
        if (alreadyDeterminedMove) {
            if (!isMoving) {

                tickCount++;

                // Let the entity try again.
                if (tickCount > 20) alreadyDeterminedMove = false;
            }
            return MoveDirection.NONE;
        }

        MoveDirection moveDirection;

        if (isMoving) {
            float result = new Random().nextFloat();

            // If probabilityStill == 0.1 then there is a 10% chance that this is true.
            if (result <= probabilityStill) {

                // Stop entity from moving.
                moveDirection = MoveDirection.NONE;
            } else {
                moveDirection = getMoveDirection();
            }

        } else {
            float result = new Random().nextFloat();

            if (result <= probabilityWalkStart) {
                moveDirection = getMoveDirection();
            } else {
                moveDirection = MoveDirection.NONE;
            }
        }

        alreadyDeterminedMove = true;
        tickCount = 0;
        return moveDirection;
    }

    private MoveDirection getMoveDirection() {
        MoveDirection moveDirection;

        GameMap gameMap = aiEntity.getGameMap();

        // Generates a number between 0-3
        MoveDirection possibleMoveDirection = MoveDirection.getDirection((byte) new Random().nextInt(4));
        Location attemptLocation = new Location(gameMap.getLocation(possibleMoveDirection)).add(aiEntity.getCurrentMapLocation());

        if (!gameMap.isMovable(attemptLocation)) {
            moveDirection = MoveDirection.NONE;
        } else {
            // Making sure the entity does not move outside the region.
            if (attemptLocation.getX() >= regionStartX && attemptLocation.getY() >= regionStartY && attemptLocation.getX() <= regionEndX && attemptLocation.getY() <= regionEndY) {
                moveDirection = possibleMoveDirection;
            } else {
                moveDirection = MoveDirection.NONE;
            }
        }
        return moveDirection;
    }
}
