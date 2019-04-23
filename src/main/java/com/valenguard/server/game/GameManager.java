package com.valenguard.server.game;

import com.valenguard.server.game.world.combat.AbilityManager;
import com.valenguard.server.game.world.entity.AiEntity;
import com.valenguard.server.game.world.entity.Entity;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.entity.PlayerProcessor;
import com.valenguard.server.game.world.maps.GameMap;
import com.valenguard.server.game.world.maps.GameMapProcessor;
import com.valenguard.server.network.game.shared.ClientHandler;
import lombok.Getter;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Getter
public class GameManager {

    private final AbilityManager abilityManager = new AbilityManager(this);
    private final PlayerProcessor playerProcessor = new PlayerProcessor(this);
    private final GameMapProcessor gameMapProcessor = new GameMapProcessor();

    public void start() {
        gameMapProcessor.loadAllMaps();
    }

    void tickWorld(long ticksPassed) {
        // WARNING: Maintain tick order!
        abilityManager.tick(ticksPassed);
        gameMapProcessor.spawnEntities();
        gameMapProcessor.getGameMaps().values().forEach(gameMap -> gameMap.getStationaryEntityController().tick());
        gameMapProcessor.getGameMaps().values().forEach(gameMap -> gameMap.getAiEntityController().tick());
        gameMapProcessor.getGameMaps().values().forEach(gameMap -> gameMap.getItemStackDropEntityController().tick());
        gameMapProcessor.getGameMaps().values().forEach(gameMap -> gameMap.getPlayerController().tickPlayer());
        gameMapProcessor.getGameMaps().values().forEach(gameMap -> gameMap.getPlayerController().sendPlayersPacket());
        gameMapProcessor.getGameMaps().values().forEach(gameMap -> gameMap.getPlayerController().tickPlayerShuffle(ticksPassed));
        playerProcessor.processPlayerQuit();
        playerProcessor.processPlayerJoinGameWorld();
    }

    public void sendToAllButPlayer(Player player, Consumer<ClientHandler> callback) {
        player.getGameMap().getPlayerController().getPlayerList().forEach(playerOnMap -> {
            if (player.equals(playerOnMap)) return;
            callback.accept(playerOnMap.getClientHandler());
        });
    }

    public void forAllPlayers(Consumer<Player> callback) {
        gameMapProcessor.getGameMaps().values().forEach(gameMap -> gameMap.getPlayerController().getPlayerList().forEach(callback));
    }

    public void forAllPlayersFiltered(Consumer<Player> callback, Predicate<Player> predicate) {
        gameMapProcessor.getGameMaps().values().forEach(gameMap -> gameMap.getPlayerController().getPlayerList().stream().filter(predicate).forEach(callback));
    }

    public void forAllAiEntitiesFiltered(Consumer<AiEntity> callback, Predicate<AiEntity> predicate) {
        gameMapProcessor.getGameMaps().values().forEach(gameMap -> gameMap.getAiEntityController().getEntities().stream().filter(predicate).forEach(callback));
    }

    public Player findPlayer(short playerId) {
        for (GameMap gameMap : gameMapProcessor.getGameMaps().values()) {
            for (Player player : gameMap.getPlayerController().getPlayerList()) {
                if (player.getServerEntityId() == playerId) {
                    return player;
                }
            }
        }
        return null;
    }

    public Player findPlayer(String username) {
        for (GameMap gameMap : gameMapProcessor.getGameMaps().values()) {
            for (Player player : gameMap.getPlayerController().getPlayerList()) {
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
        for (GameMap gameMap : getGameMapProcessor().getGameMaps().values()) {
            for (Player player : gameMap.getPlayerController().getPlayerList()) {
                kickPlayer(player);
            }
        }
    }
}
