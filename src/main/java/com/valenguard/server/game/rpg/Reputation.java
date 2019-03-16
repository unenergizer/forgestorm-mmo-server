package com.valenguard.server.game.rpg;

import com.valenguard.server.ValenguardMain;
import lombok.Getter;

public class Reputation {
    @Getter
    private short[] reputationData = new short[ValenguardMain.getInstance().getFactionManager().getNumberOfFactions()];

    private short getReputations(byte faction) {
        return reputationData[faction];
    }

    public EntityAlignment getAlignment(byte faction) {
        return ReputationTypes.getReputationType(getReputations(faction)).getEntityAlignment();
    }

    public void addReputation(byte faction, short amount) {

        reputationData[faction] = (short) Math.min(reputationData[faction] + amount, 7500);

        byte[] enemyTypes = ValenguardMain.getInstance().getFactionManager().getFactionEnimies(faction);
        for (byte index = 0; index < reputationData.length; index++) {
            if (index == faction) continue;
            for (byte enemyType : enemyTypes) {
                if (enemyType != index) continue;

                // Removing reputation from the faction.
                reputationData[index] = (short) Math.max(reputationData[index] + amount, -7500);

            }
        }
    }
}
