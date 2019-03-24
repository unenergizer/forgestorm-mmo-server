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
    private GameLoop gameLoop = new GameLoop();
    private CommandManager commandManager = new CommandManager();
    private DatabaseManager databaseManager = new DatabaseManager();
    private NetworkManager networkManager = new NetworkManager();

    // System
    private TradeManager tradeManager = new TradeManager();
    private GameManager gameManager = new GameManager();

    // Data Loaders
    private FactionManager factionManager = new FactionManager();
    private AiEntityDataManager aiEntityDataManager = new AiEntityDataManager();
    private ItemStackManager itemStackManager = new ItemStackManager();
    private DropTableManager dropTableManager = new DropTableManager();
    private EntityShopManager entityShopManager = new EntityShopManager();

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
        networkManager.exit();
        databaseManager.exit();
    }
}
