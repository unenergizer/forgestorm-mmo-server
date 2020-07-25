package com.forgestorm.server.command;

import com.forgestorm.server.command.listeners.*;
import com.forgestorm.server.game.ManagerStart;
import lombok.Getter;

import static com.forgestorm.server.util.Log.println;

@Getter
public class CommandManager implements ManagerStart {

    private final CommandProcessor commandProcessor = new CommandProcessor();
    private final ConsoleCommandManager consoleCommandManager = new ConsoleCommandManager(this);

    @Override
    public void start() {
        println(getClass(), "Registering commands...");
        commandProcessor.addListener(new EntityCommands());
        commandProcessor.addListener(new InventoryCommands());
        commandProcessor.addListener(new PlayerCommands());
        commandProcessor.addListener(new ServerCommands());
        commandProcessor.addListener(new MapCommands());
        commandProcessor.addListener(new MessageCommands());

        consoleCommandManager.start();
    }

    public void exit() {
        consoleCommandManager.stop();
    }
}
