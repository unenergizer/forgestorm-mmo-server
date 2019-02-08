package com.valenguard.server;

import com.valenguard.server.commands.CommandProcessor;
import com.valenguard.server.commands.ConsoleCommandManager;
import com.valenguard.server.commands.listeners.InventoryCommands;
import com.valenguard.server.commands.listeners.TicksPerSecondCommand;
import com.valenguard.server.game.GameManager;
import com.valenguard.server.game.inventory.ItemManager;
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
    private PingManager pingManager;
    private GameManager gameManager;
    private CommandProcessor commandProcessor;
    private GameLoop gameLoop;
    private OutputStreamManager outStreamManager;
    private ItemManager itemManager;

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

        itemManager = new ItemManager();

        gameManager = new GameManager();
        getGameManager().init();

        pingManager = new PingManager();

        registerCommands();
        initializeNetwork();

        gameLoop = new GameLoop();
        gameLoop.start();

        outStreamManager = new OutputStreamManager();
    }

    public void stop() {
        Log.println(getClass(), "ServerConnection shutdown initialized!");
        ServerConnection.getInstance().close();
        Log.println(getClass(), "ServerConnection shutdown complete!");
    }

    private void registerCommands() {
        Log.println(getClass(), "Registering commands.");
        commandProcessor = new CommandProcessor();
        commandProcessor.addListener(new TicksPerSecondCommand());
        commandProcessor.addListener(new InventoryCommands());
        new ConsoleCommandManager().start();
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
        });
    }
}
