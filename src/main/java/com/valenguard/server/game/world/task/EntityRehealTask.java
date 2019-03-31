package com.valenguard.server.game.world.task;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.entity.AiEntity;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.maps.GameMap;
import com.valenguard.server.network.game.packet.out.EntityHealPacketOut;

import static com.valenguard.server.util.Log.println;

public class EntityRehealTask implements AbstractTask {

    private static final boolean DEBUG_PRINT = false;
    private static final long REHEAL_INTERVAL = 120;
    private static final int REHEAL_AMOUNT = 1;

    @Override
    public void tick(long ticksPassed) {
        if (ticksPassed % REHEAL_INTERVAL == 0) {

            println(getClass(), "Healing entities!", false, DEBUG_PRINT);

            for (GameMap gameMap : Server.getInstance().getGameManager().getGameMapProcessor().getGameMaps().values()) {
                // Reheal players
                for (Player player : gameMap.getPlayerController().getPlayerList()) {
                    if (player.isInCombat()) continue; // Don't reheal entities in combat.
                    if (player.getCurrentHealth() < player.getMaxHealth()) {
                        println(getClass(), "Healing player: " + player.getName(), false, DEBUG_PRINT);
                        player.setCurrentHealth(player.getCurrentHealth() + REHEAL_AMOUNT);
                        gameMap.getPlayerController().forAllPlayers(anyPlayer ->
                                new EntityHealPacketOut(anyPlayer, player, REHEAL_AMOUNT).sendPacket());
                    }
                }

                // Reheal Entities
                for (AiEntity aiEntity : gameMap.getAiEntityController().getEntities()) {
                    if (aiEntity.isInCombat()) continue; // Don't reheal entities in combat.
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
