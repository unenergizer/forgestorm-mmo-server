package com.valenguard.server.game.rpg.skills;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SkillNodeData {
    private String name;
    private int numberOfUsages;
    private int dropTableId;
    private int experience;
}
