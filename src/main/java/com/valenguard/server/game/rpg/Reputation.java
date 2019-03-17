package com.valenguard.server.game.rpg;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.entity.EntityType;
import com.valenguard.server.game.entity.NPC;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.packet.out.AiEntityDataUpdatePacketOut;
import lombok.Getter;

public class Reputation {

    private Player player;

    public Reputation(Player player) {
        this.player = player;
    }

    @Getter
    private short[] reputationData = new short[ValenguardMain.getInstance().getFactionManager().getNumberOfFactions()];

    private short getReputations(byte faction) {
        return reputationData[faction];
    }

    public EntityAlignment getAlignment(byte faction) {
        return ReputationTypes.getReputationType(getReputations(faction)).getEntityAlignment();
    }

    public void addReputation(byte faction, short amount) {

        EntityAlignment previousAlignment = getAlignment(faction);
        reputationData[faction] = (short) Math.max(reputationData[faction] - amount, -7500);

        if (previousAlignment != getAlignment(faction)) {
            updateEntitiesForFaction(faction);
        }

        byte[] enemyTypes = ValenguardMain.getInstance().getFactionManager().getFactionEnemies(faction);
        for (byte index = 0; index < reputationData.length; index++) {
            if (index == faction) continue;
            for (byte enemyType : enemyTypes) {
                if (enemyType != index) continue;

                // Removing reputation from the faction.
                previousAlignment = getAlignment(index);
                reputationData[index] = (short) Math.min(reputationData[index] + amount, 7500);
                if (previousAlignment != getAlignment(index)) {
                    updateEntitiesForFaction(index);
                }
            }
        }
    }

    private void updateEntitiesForFaction(byte faction) {
        player.getGameMap().getAiEntityController().getEntities()
                .stream()
                .filter(aiEntity -> aiEntity.getEntityType() == EntityType.NPC && ((NPC) aiEntity).getFaction() == faction)
                .forEach(aiEntity -> new AiEntityDataUpdatePacketOut(player, aiEntity, AiEntityDataUpdatePacketOut.ALIGNMENT_INDEX).sendPacket());
    }
}
