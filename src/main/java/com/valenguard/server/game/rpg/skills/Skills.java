package com.valenguard.server.game.rpg.skills;

import com.valenguard.server.game.world.entity.Player;

public class Skills {

    public final Skill MINING;
    public final Skill MELEE;

    public Skills(Player player) {
        MINING = new Skill(player, SkillOpcodes.MINING);
        MELEE = new Skill(player, SkillOpcodes.MELEE);
    }
}
