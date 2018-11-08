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
    private ServerLoop serverLoop;
    private OutputStreamManager outStreamManager;

    private ValenguardMain() {
    }

    /**
     * Singleton implementation of the main class.
     *
     * @return a static instance of this class.
     */
    public static ValenguardMain getInstance() {
        if (instance == null) instance = new ValenguardMain();
        return instance;
    }

    public static void main(String[] args) {
        ValenguardMain.getInstance().start();
    }

    /**
     * Starts all server processes.
     */
    private void start() {
        Log.println(getClass(),"Booting Valenguard Server!");
        gameManager = new GameManager();
        getGameManager().init();

        pingManager = new PingManager();

        // Register commands
        registerCommands();

        // start serverConnection code
        initializeNetwork();

        // start server loop
        serverLoop = new ServerLoop();
        serverLoop.start();

        outStreamManager = new OutputStreamManager();
        new Thread(outStreamManager, "OutputStream Thread").start();
    }

    /**
     * Stops server operations and terminates the program.
     */
    public void stop() {
        Log.println(getClass(),"ServerConnection shutdown initialized!");

        //TODO: Stop network functions and shut down nicely.
        ServerConnection.getInstance().close();

        Log.println(getClass(),"ServerConnection shutdown complete!");
    }

    /**
     * Initializes the network. Starts listening for connection
     * and registers network event listeners.
     */
    private void initializeNetwork() {
        Log.println(getClass(),"Initializing network...");
        ServerConnection.getInstance().openServer((eventBus) -> {
            eventBus.registerListener(new PlayerMove());
            eventBus.registerListener(new PingIn());
        });
    }

    /**
     * Register server commands.
     */
    private void registerCommands() {
        Log.println(getClass(),"Registering commands.");
        commandProcessor = new CommandProcessor();

        // console command manager
        new ConsoleCommandManager().start();
    }
}
