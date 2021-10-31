package com.forgestorm.server.game.rpg;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.world.entity.EntityType;
import com.forgestorm.server.game.world.entity.NPC;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.packet.out.AiEntityDataUpdatePacketOutOut;
import lombok.Getter;

import static java.util.Objects.requireNonNull;

public class Reputation {

    private final Player player;

    public Reputation(final Player player) {
        this.player = player;
    }

    @Getter
    private final short[] reputationData = new short[ServerMain.getInstance().getFactionManager().getNumberOfFactions()];

    private short getReputations(byte faction) {
        return reputationData[faction];
    }

    public EntityAlignment getAlignment(byte faction) {
        return requireNonNull(ReputationTypes.getReputationType(getReputations(faction))).getEntityAlignment();
    }

    public void addReputation(byte faction, short amount) {

        EntityAlignment previousAlignment = getAlignment(faction);
        reputationData[faction] = (short) Math.max(reputationData[faction] - amount, -7500);

        if (previousAlignment != getAlignment(faction)) {
            updateEntitiesForFaction(faction);
        }

        byte[] enemyTypes = ServerMain.getInstance().getFactionManager().getFactionEnemies(faction);
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
        player.getGameWorld().getAiEntityController().getEntities()
                .stream()
                .filter(aiEntity -> aiEntity.getEntityType() == EntityType.NPC && ((NPC) aiEntity).getFaction() == faction)
                .forEach(aiEntity -> new AiEntityDataUpdatePacketOutOut(player, aiEntity, AiEntityDataUpdatePacketOutOut.ALIGNMENT_INDEX).sendPacket());
    }
}
