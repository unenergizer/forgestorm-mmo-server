package com.valenguard.server.game.task;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.entity.AiEntity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.maps.GameMap;
import com.valenguard.server.network.game.packet.out.EntityHealPacketOut;

import static com.valenguard.server.util.Log.println;

public class EntityRehealTask {

    private static final boolean DEBUG_PRINT = false;

    private static final long REHEAL_INTERVAL = 120;
    private static final int REHEAL_AMOUNT = 1;

    public void tickEntityReheal(long numberOfTicksPassed) {
        if (numberOfTicksPassed % REHEAL_INTERVAL == 0) {

            println(getClass(), "Rehealing entities!", false, DEBUG_PRINT);

            for (GameMap gameMap : ValenguardMain.getInstance().getGameManager().getGameMaps().values()) {
                // Reheal players
                for (Player player : gameMap.getPlayerController().getPlayerList()) {
                    if (player.getTargetEntity() != null) continue; // Don't reheal entities with combat targets.
                    if (player.getCurrentHealth() < player.getMaxHealth()) {
                        println(getClass(), "Rehealing player: " + player.getName(), false, DEBUG_PRINT);
                        player.setCurrentHealth(player.getCurrentHealth() + REHEAL_AMOUNT);
                        gameMap.getPlayerController().forAllPlayers(anyPlayer ->
                                new EntityHealPacketOut(anyPlayer, player, REHEAL_AMOUNT).sendPacket());
                    }
                }

                // Reheal Entities
                for (AiEntity aiEntity : gameMap.getAiEntityController().getEntities()) {
                    if (aiEntity.getTargetEntity() != null) continue; // Don't reheal entities with combat targets.
                    if (aiEntity.getCurrentHealth() < aiEntity.getMaxHealth()) {
                        aiEntity.setCurrentHealth(aiEntity.getCurrentHealth() + REHEAL_AMOUNT);
                        gameMap.getPlayerController().forAllPlayers(anyPlayer ->
                                new EntityHealPacketOut(anyPlayer, aiEntity, REHEAL_AMOUNT).sendPacket());
                    }
                }
            }
        }
    }

}
