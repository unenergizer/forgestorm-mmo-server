package com.valenguard.server;

import com.valenguard.server.commands.CommandProcessor;
import com.valenguard.server.commands.ConsoleCommandManager;
import com.valenguard.server.maps.MapManager;
import com.valenguard.server.network.ServerConnection;
import com.valenguard.server.network.packet.in.PlayerMove;
import com.valenguard.server.network.packet.in.PingIn;
import com.valenguard.server.network.packet.out.OutputStreamManager;
import com.valenguard.server.util.ConsoleLogger;
import lombok.Getter;

@Getter
public class ValenguardMain {

    // This is a commit test from Joseph

    private static ValenguardMain instance = null;
    private MapManager mapManager;
    private CommandProcessor commandProcessor;
    private ServerLoop serverLoop;
    private OutputStreamManager outStreamManager;

    private ValenguardMain() {}

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
        System.out.println(ConsoleLogger.SERVER.toString() + "Booting Valenguard Server!");
        ValenguardMain.getInstance().start();
    }

    /**
     * Starts all server processes.
     */
    private void start() {

        mapManager = new MapManager();

        // register commands
        registerCommands();

        // start serverConnection code
        initializeNetwork();

        // start server loop
        serverLoop = new ServerLoop();
        serverLoop.start();

        outStreamManager = new OutputStreamManager();
        new Thread(outStreamManager, "OutputStream Thread").start();

//        // TODO: MOVE OR REMOVE....
//        Scanner scanner = new Scanner(System.in);
//        String input;
//        while (true) {
//            input = scanner.nextLine();
//            if (input.equalsIgnoreCase("/stop")) break;
//        }
//        scanner.close();
//        serverConnection.close();
    }

    /**
     * Initializes the network. Starts listening for connection
     * and registers network event listeners.
     */
    private void initializeNetwork() {
        System.out.println(ConsoleLogger.NETWORK.toString() + "Initializing network...");
        ServerConnection.getInstance().openServer((eventBus) -> {
            eventBus.registerListener(new PlayerMove());
            eventBus.registerListener(new PingIn());
        });
    }

    /**
     * Register server commands.
     */
    private void registerCommands() {
        System.out.println(ConsoleLogger.SERVER.toString() + "Registering commands.");
        commandProcessor = new CommandProcessor();

        // console command manager
        new ConsoleCommandManager().start();
    }

    /**
     * Stops server operations and terminates the program.
     */
    public void stop() {
        System.out.println(ConsoleLogger.SERVER.toString() + "ServerConnection shutdown initialized!");

        //TODO: Stop network functions and shut down nicely.
        ServerConnection.getInstance().close();

        System.out.println(ConsoleLogger.SERVER.toString() + "ServerConnection shutdown complete!");
    }
}
