package com.forgestorm.server.game.world.entity;

import com.forgestorm.server.game.rpg.EntityAlignment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Monster extends AiEntity {
    private EntityAlignment alignment;
}
