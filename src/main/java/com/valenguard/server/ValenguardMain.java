package com.valenguard.server;

import com.valenguard.server.commands.CommandProcessor;
import com.valenguard.server.commands.ConsoleCommandManager;
import com.valenguard.server.game.GameManager;
import com.valenguard.server.network.PingManager;
import com.valenguard.server.network.ServerConnection;
import com.valenguard.server.network.packet.in.PingIn;
import com.valenguard.server.network.packet.in.PlayerMove;
import com.valenguard.server.network.packet.out.OutputStreamManager;
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
        Log.println(getClass(),"Booting Valenguard Server!");

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
        Log.println(getClass(),"ServerConnection shutdown initialized!");
        ServerConnection.getInstance().close();
        Log.println(getClass(),"ServerConnection shutdown complete!");
    }

    private void registerCommands() {
        Log.println(getClass(),"Registering commands.");
        commandProcessor = new CommandProcessor();
        new ConsoleCommandManager().start();
    }

    private void initializeNetwork() {
        Log.println(getClass(),"Initializing network...");
        ServerConnection.getInstance().openServer((eventBus) -> {
            eventBus.registerListener(new PlayerMove());
            eventBus.registerListener(new PingIn());
        });
    }
}
