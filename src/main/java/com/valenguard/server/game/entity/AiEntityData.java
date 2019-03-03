package com.valenguard.server.game.entity;

import com.valenguard.server.game.rpg.EntityAlignment;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class AiEntityData {

    private final int entityDataID;

    private String name;
    private EntityType entityType;
    private EntityAlignment entityAlignment;
    private Integer colorID;
    private short atlasBodyID;
    private Integer atlasHeadID;
    private int health;
    private int damage;
    private int expDrop;
    private int dropTable;
    private float walkSpeed;
    private float probabilityStill;
    private float probabilityWalkStart;

    public AiEntityData(final int entityDataID) {
        this.entityDataID = entityDataID;
    }
}
