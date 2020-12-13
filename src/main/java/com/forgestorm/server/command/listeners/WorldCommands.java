package com.forgestorm.server.command.listeners;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.command.*;
import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.game.world.maps.GameWorld;
import com.forgestorm.server.game.world.maps.GameWorldProcessor;
import com.forgestorm.server.game.world.maps.WorldCreator;
import com.forgestorm.server.io.todo.FileManager;
import com.forgestorm.server.network.game.packet.out.ChatMessagePacketOut;

import java.io.File;

public class WorldCommands {

    @Command(base = "create", argLenReq = 3)
    @CommandArguments(missing = "<worldName> <width> <height>")
    public void createWorld(CommandSource commandSource, String[] args) {
        GameWorldProcessor gameWorldProcessor = ServerMain.getInstance().getGameManager().getGameWorldProcessor();
        String worldName = args[0];
        int width = Integer.parseInt(args[1]);
        int height = Integer.parseInt(args[2]);

        // Check to make sure the world name doesn't already exist
        if (gameWorldProcessor.doesGameWorldExist(worldName)) {
            new ChatMessagePacketOut(commandSource.getPlayer(), ChatChannelType.GENERAL, "[RED]World name already been used. Try a different name").sendPacket();
            return;
        }

        // TODO: VERY UGLY CODE. WILL REWRITE SOME DAY
        WorldCreator worldCreator = new WorldCreator();
        worldCreator.createWorld(worldName, width, height);
        FileManager fileManager = ServerMain.getInstance().getFileManager();
        File file = new File(fileManager.getWorldDirectory() + File.separator + worldName);
        fileManager.loadGameWorldData(file);
        GameWorld gameWorld = fileManager.getGameWorldData(file).getGameWorld();
        gameWorldProcessor.loadWorld(gameWorld);

        new ChatMessagePacketOut(commandSource.getPlayer(), ChatChannelType.GENERAL, "[RED] THIS NEEDS TO BE REDONE! PLEASE LOOK IT UP AND FIX... ").sendPacket();
    }

    @Command(base = "saveworld")
    public void saveWorld(CommandSource commandSource) {
        GameWorldProcessor gameWorldProcessor = ServerMain.getInstance().getGameManager().getGameWorldProcessor();

        for (GameWorld gameWorld : gameWorldProcessor.getGameWorlds().values()) {
            gameWorld.saveChunks(true);
        }

        commandSource.sendMessage("[GREEN]All " + gameWorldProcessor.getGameWorlds().size() + " worlds have been saved.", ChatChannelType.STAFF);
    }
}
