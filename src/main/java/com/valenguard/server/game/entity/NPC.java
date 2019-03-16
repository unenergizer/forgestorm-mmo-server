package com.valenguard.server.game.entity;

import com.valenguard.server.game.rpg.EntityAlignment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NPC extends AiEntity {

    private byte faction;

    public EntityAlignment getAlignmentByPlayer(Player player) {
        return player.getReputation().getAlignment(faction);
    }

    // TODO: Scrips, timers, tasks, questing, etc

}
