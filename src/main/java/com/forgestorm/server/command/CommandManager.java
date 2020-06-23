package com.forgestorm.server.command;

import com.forgestorm.server.command.listeners.*;
import lombok.Getter;

import static com.forgestorm.server.util.Log.println;

@Getter
public class CommandManager {

    private final CommandProcessor commandProcessor = new CommandProcessor();
    private final ConsoleCommandManager consoleCommandManager = new ConsoleCommandManager(this);

    public void start() {
        println(getClass(), "Registering commands...");
        commandProcessor.addListener(new EntityCommands());
        commandProcessor.addListener(new InventoryCommands());
        commandProcessor.addListener(new PlayerCommands());
        commandProcessor.addListener(new ServerCommands());
        commandProcessor.addListener(new MapCommands());

        consoleCommandManager.start();
    }

    public void exit() {
        consoleCommandManager.stop();
    }
}
