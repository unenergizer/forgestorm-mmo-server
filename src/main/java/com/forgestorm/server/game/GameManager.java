package com.forgestorm.server.game;

import com.forgestorm.server.game.world.combat.AbilityManager;
import com.forgestorm.server.game.world.entity.AiEntity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.entity.PlayerProcessor;
import com.forgestorm.server.game.world.maps.GameWorld;
import com.forgestorm.server.game.world.maps.GameWorldProcessor;
import com.forgestorm.server.game.world.task.AbstractTask;
import com.forgestorm.server.network.game.shared.ClientHandler;
import lombok.Getter;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
public class GameManager implements AbstractTask, ManagerStart {

    private final AbilityManager abilityManager = new AbilityManager(this);
    private final PlayerProcessor playerProcessor = new PlayerProcessor(this);
    private final GameWorldProcessor gameWorldProcessor = new GameWorldProcessor();

    @Override
    public void start() {
        gameWorldProcessor.loadAllMaps();
        gameWorldProcessor.getEntitiesFromDatabase();
    }

    @Override
    public void tick(long ticksPassed) {
        // WARNING: Maintain tick order!
        abilityManager.tick(ticksPassed);
        gameWorldProcessor.spawnEntities();
        gameWorldProcessor.getGameMaps().values().forEach(gameMap -> gameMap.getStationaryEntityController().tick());
        gameWorldProcessor.getGameMaps().values().forEach(gameMap -> gameMap.getAiEntityController().tick());
        gameWorldProcessor.getGameMaps().values().forEach(gameMap -> gameMap.getItemStackDropEntityController().tick());
        gameWorldProcessor.getGameMaps().values().forEach(gameMap -> gameMap.getPlayerController().tickPlayerQuit());
        gameWorldProcessor.getGameMaps().values().forEach(gameMap -> gameMap.getPlayerController().tickPlayerJoin());
        gameWorldProcessor.getGameMaps().values().forEach(gameMap -> gameMap.getPlayerController().tickPlayerShuffle(ticksPassed));
        playerProcessor.processPlayerQuit();
        playerProcessor.processPlayerJoinGameWorld();
    }

    public void sendToAllButPlayer(Player player, Consumer<ClientHandler> callback) {
        player.getGameMap().getPlayerController().getPlayerList().forEach(playerOnMap -> {
            if (player.equals(playerOnMap)) return;
            callback.accept(playerOnMap.getClientHandler());
        });
    }

    public void sendToAll(Player player, Consumer<ClientHandler> callback) {
        player.getGameMap().getPlayerController().getPlayerList().forEach(playerOnMap -> {
            callback.accept(playerOnMap.getClientHandler());
        });
    }

    public void forAllPlayers(Consumer<Player> callback) {
        gameWorldProcessor.getGameMaps().values().forEach(gameMap -> gameMap.getPlayerController().getPlayerList().forEach(callback));
    }

    public void forAllPlayersFiltered(Consumer<Player> callback, Predicate<Player> predicate) {
        gameWorldProcessor.getGameMaps().values().forEach(gameMap -> gameMap.getPlayerController().getPlayerList().stream().filter(predicate).forEach(callback));
    }

    public void forAllAiEntitiesFiltered(Consumer<AiEntity> callback, Predicate<AiEntity> predicate) {
        gameWorldProcessor.getGameMaps().values().forEach(gameMap -> gameMap.getAiEntityController().getEntities().stream().filter(predicate).forEach(callback));
    }

    public Player findPlayer(short playerId) {
        for (GameWorld gameWorld : gameWorldProcessor.getGameMaps().values()) {
            for (Player player : gameWorld.getPlayerController().getPlayerList()) {
                if (player.getServerEntityId() == playerId) {
                    return player;
                }
            }
        }
        return null;
    }

    public Player findPlayer(String username) {
        for (GameWorld gameWorld : gameWorldProcessor.getGameMaps().values()) {
            for (Player player : gameWorld.getPlayerController().getPlayerList()) {
                if (player.getName().toLowerCase().equals(username.toLowerCase())) {
                    return player;
                }
            }
        }
        return null;
    }

    public void kickPlayer(String username) {
        kickPlayer(findPlayer(username));
    }

    public void kickPlayer(Player player) {
        playerProcessor.queuePlayerQuitGameWorld(player.getClientHandler());
    }

    public void exit() {
        // Kick all players
        for (GameWorld gameWorld : getGameWorldProcessor().getGameMaps().values()) {
            for (Player player : gameWorld.getPlayerController().getPlayerList()) {
                kickPlayer(player);
            }
        }
    }
}
