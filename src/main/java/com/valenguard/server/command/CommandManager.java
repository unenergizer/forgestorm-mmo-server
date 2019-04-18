package com.valenguard.server.command;

import com.valenguard.server.command.listeners.*;
import lombok.Getter;

import static com.valenguard.server.util.Log.println;

@Getter
public class CommandManager {

    private final CommandProcessor commandProcessor = new CommandProcessor();
    private final ConsoleCommandManager consoleCommandManager = new ConsoleCommandManager(this);

    public void start() {
        println(getClass(), "Registering commands...");
        commandProcessor.addListener(new TicksPerSecondCommand());
        commandProcessor.addListener(new InventoryCommands());
        commandProcessor.addListener(new PlayerCommands());
        commandProcessor.addListener(new ShutdownCommand());
        commandProcessor.addListener(new PlayersOnlineCommand());

        consoleCommandManager.start();
    }

    public void exit() {
        consoleCommandManager.stop();
    }
}
