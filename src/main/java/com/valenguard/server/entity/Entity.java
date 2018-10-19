package com.valenguard.server.entity;

import com.valenguard.server.maps.data.Location;
import com.valenguard.server.maps.data.TmxMap;
import com.valenguard.server.network.shared.ServerConstants;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Entity {

    private int entityID;
    private int health;
    private String name = "";
    private int level;

    private Location location;
    private Location futureLocation;
    private float moveSpeed;
    private int countDownMovementTicks;

    boolean isMoving = false;

    public Entity(int entityID, Location location, float moveSpeed) {
        this.entityID = entityID;
        this.location = location;
        this.moveSpeed = moveSpeed;
    }

    public void setupMovement(Location futureLocation) {
        this.futureLocation = futureLocation;
        isMoving = true;
        System.out.println("MOVE SPEED: " + moveSpeed);
        moveSpeed = 1.0f;
        countDownMovementTicks = (int) (ServerConstants.TICKS_PER_SECOND * (1.0f / moveSpeed));
        System.out.println("TICK SETUP: " + countDownMovementTicks);
    }

    public void resetMovement() {
        // Update with new location
        location = futureLocation;

        // Clear movement data
        futureLocation = null;
        isMoving = false;
        countDownMovementTicks = -1;
    }

    public void processMovement() {
        countDownMovementTicks--;
    }

    public TmxMap getMapData() {
        return location.getMapData();
    }
}
