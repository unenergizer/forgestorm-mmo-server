package com.valenguard.server;

import com.valenguard.server.command.CommandManager;
import com.valenguard.server.database.DatabaseManager;
import com.valenguard.server.game.GameLoop;
import com.valenguard.server.game.GameManager;
import com.valenguard.server.game.rpg.EntityShopManager;
import com.valenguard.server.game.rpg.FactionManager;
import com.valenguard.server.game.world.entity.AiEntityDataManager;
import com.valenguard.server.game.world.item.DropTableManager;
import com.valenguard.server.game.world.item.ItemStackManager;
import com.valenguard.server.game.world.item.trade.TradeManager;
import com.valenguard.server.network.NetworkManager;
import com.valenguard.server.util.Log;
import lombok.Getter;

@Getter
public class Server {

    private static Server instance = null;

    // Framework
    private final GameLoop gameLoop = new GameLoop();
    private final CommandManager commandManager = new CommandManager();
    private final DatabaseManager databaseManager = new DatabaseManager();
    private final NetworkManager networkManager = new NetworkManager();

    // System
    private final TradeManager tradeManager = new TradeManager();
    private final GameManager gameManager = new GameManager();

    // Data Loaders
    private final FactionManager factionManager = new FactionManager();
    private final AiEntityDataManager aiEntityDataManager = new AiEntityDataManager();
    private final ItemStackManager itemStackManager = new ItemStackManager();
    private final DropTableManager dropTableManager = new DropTableManager();
    private final EntityShopManager entityShopManager = new EntityShopManager();

    private Server() {
    }

    public static void main(String[] args) {
        Server.getInstance().startServer();
    }

    public static Server getInstance() {
        if (instance == null) instance = new Server();
        return instance;
    }

    private void startServer() {
        Log.println(getClass(), "Starting Server!");

        // Boot io loaders
        aiEntityDataManager.start();
        itemStackManager.start();
        dropTableManager.start();

        // Start systems
        gameManager.start();
        commandManager.start();
        databaseManager.start();
        networkManager.start();
        gameLoop.start();
    }

    public void exitServer() {
        Log.println(getClass(), "Stopping Server!");

        // TODO: Implement ExecutorService to manage threads and shut them down in order.

        getGameManager().exit();
        commandManager.exit();
        networkManager.exit();
        databaseManager.exit();

        System.exit(0);
    }
}
