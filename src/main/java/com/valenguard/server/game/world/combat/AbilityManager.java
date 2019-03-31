package com.valenguard.server.game.world.combat;

import com.valenguard.server.game.GameManager;
import com.valenguard.server.game.world.entity.AiEntity;
import com.valenguard.server.game.world.entity.MovingEntity;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.maps.GameMap;

public class AbilityManager {

    private final GameManager gameManager;

    public AbilityManager(final GameManager gameManager) {
        this.gameManager = gameManager;
    }

    public void tick(long ticksPassed) {
        if (ticksPassed % 20 == 0) {
            for (GameMap gameMap : gameManager.getGameMapProcessor().getGameMaps().values()) {
                for (AiEntity aiEntity : gameMap.getAiEntityController().getEntities()) {
                    processEntity(aiEntity);
                }
                for (Player player : gameMap.getPlayerController().getPlayerList()) {
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
