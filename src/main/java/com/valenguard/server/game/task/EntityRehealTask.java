package com.valenguard.server.game.task;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.entity.AiEntity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.maps.GameMap;
import com.valenguard.server.network.packet.out.EntityHealPacketOut;

public class EntityRehealTask {

    private static final long REHEAL_INTERVAL = 120;
    private static final int REHEAL_AMOUNT = 1;

    public void tickEntityReheal(long numberOfTicksPassed) {
        if (numberOfTicksPassed % REHEAL_INTERVAL == 0) {
            for (GameMap gameMap : ValenguardMain.getInstance().getGameManager().getGameMaps().values()) {
                // Reheal players
                for (Player player : gameMap.getPlayerList()) {
                    if (player.getTargetEntity() != null) continue; // Don't reheal entities with combat targets.
                    if (player.getCurrentHealth() < player.getMaxHealth()) {
                        player.setCurrentHealth(player.getCurrentHealth() + REHEAL_AMOUNT);
                        gameMap.forAllPlayers(anyPlayer ->
                                new EntityHealPacketOut(anyPlayer, player, REHEAL_AMOUNT).sendPacket());
                    }
                }

                // Reheal Entities
                for (AiEntity aiEntity : gameMap.getAiEntityMap().values()) {
                    if (aiEntity.getTargetEntity() != null) continue; // Don't reheal entities with combat targets.
                    if (aiEntity.getCurrentHealth() < aiEntity.getMaxHealth()) {
                        aiEntity.setCurrentHealth(aiEntity.getCurrentHealth() + REHEAL_AMOUNT);
                        gameMap.forAllPlayers(anyPlayer ->
                                new EntityHealPacketOut(anyPlayer, aiEntity, REHEAL_AMOUNT).sendPacket());
                    }
                }
            }
        }
    }

}
