package com.forgestorm.server.game.world.task;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.world.entity.AiEntity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.GameWorld;
import com.forgestorm.server.network.game.packet.out.EntityHealPacketOut;

import static com.forgestorm.server.util.Log.println;

public class EntityRehealTask implements AbstractTask {

    private static final boolean DEBUG_PRINT = false;
    private static final long REHEAL_INTERVAL = 120;
    private static final int REHEAL_AMOUNT = 1;

    @Override
    public void tick(long ticksPassed) {
        if (ticksPassed % REHEAL_INTERVAL == 0) {

            println(getClass(), "Healing entities!", false, DEBUG_PRINT);

            for (GameWorld gameWorld : ServerMain.getInstance().getGameManager().getGameWorldProcessor().getGameMaps().values()) {
                // Reheal players
                for (Player player : gameWorld.getPlayerController().getPlayerList()) {
                    if (player.isInCombat()) continue; // Don't reheal entities in active.
                    if (player.getCurrentHealth() < player.getMaxHealth()) {
                        println(getClass(), "Healing player: " + player.getName(), false, DEBUG_PRINT);
                        player.setCurrentHealth(player.getCurrentHealth() + REHEAL_AMOUNT);
                        gameWorld.getPlayerController().forAllPlayers(anyPlayer ->
                                new EntityHealPacketOut(anyPlayer, player, REHEAL_AMOUNT).sendPacket());
                    }
                }

                // Reheal Entities
                for (AiEntity aiEntity : gameWorld.getAiEntityController().getEntities()) {
                    if (aiEntity.isInCombat()) continue; // Don't reheal entities in active.
                    if (aiEntity.getCurrentHealth() < aiEntity.getMaxHealth()) {
                        aiEntity.setCurrentHealth(aiEntity.getCurrentHealth() + REHEAL_AMOUNT);
                        gameWorld.getPlayerController().forAllPlayers(anyPlayer ->
                                new EntityHealPacketOut(anyPlayer, aiEntity, REHEAL_AMOUNT).sendPacket());
                    }
                }
            }
        }
    }
}
