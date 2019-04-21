package com.valenguard.server.game.abilities;

import com.valenguard.server.game.world.entity.MovingEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class WaitingAbility {
    private MovingEntity targetEntity;
}
