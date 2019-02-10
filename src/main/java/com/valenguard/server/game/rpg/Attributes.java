package com.valenguard.server.game.rpg;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Attributes {
    // max health
    private int health = 0; // current health
    private int armor = 0;
    private int damage = 0;
}
