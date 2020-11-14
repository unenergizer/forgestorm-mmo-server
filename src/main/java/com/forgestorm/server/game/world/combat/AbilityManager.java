package com.forgestorm.server.game.world.combat;

import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.GameManager;
import com.forgestorm.server.game.world.entity.AiEntity;
import com.forgestorm.server.game.world.entity.MovingEntity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.GameWorld;

public class AbilityManager {

    private final GameManager gameManager;

    public AbilityManager(final GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void tick(long ticksPassed) {
        if (ticksPassed % GameConstants.TICKS_PER_SECOND == 0) {
            for (GameWorld gameWorld : gameManager.getGameWorldProcessor().getGameWorlds().values()) {
                for (AiEntity aiEntity : gameWorld.getAiEntityController().getEntities()) {
                    processEntity(aiEntity);
                }
                for (Player player : gameWorld.getPlayerController().getPlayerList()) {
                    processEntity(player);
                }
            }
        }
    }

    private void processEntity(MovingEntity movingEntity) {
        for (AbstractAbility ability : movingEntity.getAbstractAbilityList()) {
            ability.processCooldownTime();
            switch (ability.getAbilityType()) {
                case ACTIVE:
                    if (ability.isReady() && ability.isActive()) {
                        ability.doAbilityActions(movingEntity);
                        ability.resetAbility();
                    }
                    break;
                case PASSIVE:
                    ability.doAbilityActions(movingEntity);
                    break;
            }
        }
    }

}
