package com.valenguard.server.game.entity;

import com.valenguard.server.game.rpg.EntityAlignment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Monster extends AiEntity {
    private EntityAlignment alignment;
}
