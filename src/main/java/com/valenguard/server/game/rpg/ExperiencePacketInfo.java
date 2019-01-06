package com.valenguard.server.game.rpg;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ExperiencePacketInfo {
    private byte skillOpcode;
    private int experienceGained;
}
