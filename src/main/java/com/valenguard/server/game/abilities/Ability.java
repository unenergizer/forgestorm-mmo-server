package com.valenguard.server.game.abilities;

import lombok.Data;

@Data
public class Ability {
    /* REQUIRED */
    private short abilityId;
    private String name;
    private AbilityType abilityType;

    private Short abilityAnimation;

    private Integer cooldown;

    /* FLAGS */
    private Integer damageMin;
    private Integer damageMax;

    private Integer distanceMin;
    private Integer distanceMax;
}
