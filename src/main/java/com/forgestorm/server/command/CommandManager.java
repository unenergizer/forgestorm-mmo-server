package com.forgestorm.server.command;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.command.listeners.*;
import com.forgestorm.server.game.ManagerStart;
import com.forgestorm.server.game.world.entity.Player;
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
        commandProcessor.addListener(new InventoryCommands(this));
        commandProcessor.addListener(new PlayerCommands(this));
        commandProcessor.addListener(new ServerCommands(this));
        commandProcessor.addListener(new WorldCommands());
        commandProcessor.addListener(new MessageCommands());

        consoleCommandManager.start();
    }

    public void exit() {
        consoleCommandManager.stop();
    }

    public Player getPlayer(CommandSource commandSource, String playerName) {
        Player player = ServerMain.getInstance().getGameManager().findPlayer(playerName);

        if (player == null) {
            commandSource.sendMessage("[RED]The player [YELLOW]" + playerName + "[RED] could not be found. Check spelling.");
            return null;
        }

        return player;
    }
}
