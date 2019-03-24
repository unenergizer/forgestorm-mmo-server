package com.valenguard.server.command;

import com.valenguard.server.command.listeners.InventoryCommands;
import com.valenguard.server.command.listeners.PlayerCommands;
import com.valenguard.server.command.listeners.TicksPerSecondCommand;
import lombok.Getter;

import static com.valenguard.server.util.Log.println;

@Getter
public class CommandManager {

    private final CommandProcessor commandProcessor = new CommandProcessor();

    public void start() {
        println(getClass(), "Registering commands...");
        commandProcessor.addListener(new TicksPerSecondCommand());
        commandProcessor.addListener(new InventoryCommands());
        commandProcessor.addListener(new PlayerCommands());

        new ConsoleCommandManager(this).start();
    }
}
