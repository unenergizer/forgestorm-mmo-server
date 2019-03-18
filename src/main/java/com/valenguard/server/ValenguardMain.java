package com.valenguard.server;

import com.valenguard.server.commands.CommandProcessor;
import com.valenguard.server.commands.ConsoleCommandManager;
import com.valenguard.server.commands.listeners.InventoryCommands;
import com.valenguard.server.commands.listeners.PlayerCommands;
import com.valenguard.server.commands.listeners.TicksPerSecondCommand;
import com.valenguard.server.game.GameManager;
import com.valenguard.server.game.entity.AiEntityDataManager;
import com.valenguard.server.game.entity.AiEntityRespawnTimer;
import com.valenguard.server.game.inventory.DropTableManager;
import com.valenguard.server.game.inventory.ItemStackManager;
import com.valenguard.server.game.inventory.TradeManager;
import com.valenguard.server.game.mysql.DatabaseConnection;
import com.valenguard.server.game.mysql.DatabaseSettingsLoader;
import com.valenguard.server.game.rpg.EntityShopManager;
import com.valenguard.server.game.rpg.FactionManager;
import com.valenguard.server.network.PingManager;
import com.valenguard.server.network.ServerConnection;
import com.valenguard.server.network.packet.in.*;
import com.valenguard.server.network.packet.out.OutputStreamManager;
import com.valenguard.server.network.shared.NetworkSettingsLoader;
import com.valenguard.server.util.Log;
import lombok.Getter;

@Getter
public class ValenguardMain {

    private static ValenguardMain instance = null;

    // Framework
    private GameLoop gameLoop;
    private OutputStreamManager outStreamManager;
    private CommandProcessor commandProcessor;

    // System
    private PingManager pingManager;
    private TradeManager tradeManager;
    private AiEntityRespawnTimer aiEntityRespawnTimer;
    private GameManager gameManager;

    // Data Loaders
    private FactionManager factionManager;
    private AiEntityDataManager aiEntityDataManager;
    private ItemStackManager itemStackManager;
    private DropTableManager dropTableManager;
    private EntityShopManager entityShopManager;

    private ValenguardMain() {
    }

    public static ValenguardMain getInstance() {
        if (instance == null) instance = new ValenguardMain();
        return instance;
    }

    public static void main(String[] args) {
        ValenguardMain.getInstance().start();
    }

    private void start() {
        Log.println(getClass(), "Booting Valenguard Server!");

        // Boot data loaders
        factionManager = new FactionManager();
        aiEntityDataManager = new AiEntityDataManager();
        itemStackManager = new ItemStackManager();
        dropTableManager = new DropTableManager();
        entityShopManager = new EntityShopManager();

        // Start systems
        pingManager = new PingManager();
        tradeManager = new TradeManager();
        aiEntityRespawnTimer = new AiEntityRespawnTimer();
        gameManager = new GameManager();
        getGameManager().init();

        // Framework
        registerCommands();
        initializeDatabase();
        initializeNetwork();
        gameLoop = new GameLoop();
        gameLoop.start();
    }

    public void stop() {
        Log.println(getClass(), "ServerConnection shutdown initialized!");
        ServerConnection.getInstance().close();
        DatabaseConnection.getInstance().close();
        Log.println(getClass(), "ServerConnection shutdown complete!");
    }

    private void registerCommands() {
        Log.println(getClass(), "Registering commands.");
        commandProcessor = new CommandProcessor();
        commandProcessor.addListener(new TicksPerSecondCommand());
        commandProcessor.addListener(new InventoryCommands());
        commandProcessor.addListener(new PlayerCommands());
        new ConsoleCommandManager().start();
    }

    private void initializeDatabase() {
        Log.println(getClass(), "Initializing database...");
        DatabaseSettingsLoader databaseSettingsLoader = new DatabaseSettingsLoader();
        DatabaseConnection.getInstance().openDatabase(databaseSettingsLoader.loadNetworkSettings());
    }

    private void initializeNetwork() {
        Log.println(getClass(), "Initializing network...");
        NetworkSettingsLoader networkSettingsLoader = new NetworkSettingsLoader();
        ServerConnection.getInstance().openServer(networkSettingsLoader.loadNetworkSettings(), (eventBus) -> {
            eventBus.registerListener(new PlayerMovePacketIn());
            eventBus.registerListener(new PingPacketIn());
            eventBus.registerListener(new ChatMessagePacketIn());
            eventBus.registerListener(new PlayerAppearancePacketIn());
            eventBus.registerListener(new InventoryPacketIn());
            eventBus.registerListener(new ClickActionPacketIn());
            eventBus.registerListener(new PlayerTradePacketIn());
            eventBus.registerListener(new ShopPacketIn());
        });
        outStreamManager = new OutputStreamManager();
    }
}
