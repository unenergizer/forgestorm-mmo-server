package com.forgestorm.server.game.abilities;

import com.forgestorm.server.game.world.entity.MovingEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class WaitingAbility {
    private MovingEntity targetEntity;
}
