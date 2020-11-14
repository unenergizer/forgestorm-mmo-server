package com.forgestorm.server.command.listeners;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.command.Command;
import com.forgestorm.server.command.CommandSource;
import com.forgestorm.server.command.CommandArguments;
import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.game.world.maps.GameWorldProcessor;
import com.forgestorm.server.game.world.maps.Tile;
import com.forgestorm.server.network.game.packet.out.ChatMessagePacketOut;

public class WorldCommands {

    @Command(base = "create", argLenReq = 3)
    @CommandArguments(missing = "<worldName> <width> <height>")
    public void createWorld(CommandSource commandSource, String[] args) {
        GameWorldProcessor gameWorldProcessor = ServerMain.getInstance().getGameManager().getGameWorldProcessor();
        final int minSize = 10;
        String worldName = args[0];
        int width = Integer.parseInt(args[1]);
        int height = Integer.parseInt(args[2]);

        // Check to make sure the world name doesn't already exist
        if (gameWorldProcessor.doesGameWorldExist(worldName)) {
            new ChatMessagePacketOut(commandSource.getPlayer(), ChatChannelType.GENERAL, "[RED]World name already been used. Try a different name").sendPacket();
            return;
        }

        // Check world sizes
        if (width < minSize || height < minSize) {
            new ChatMessagePacketOut(commandSource.getPlayer(), ChatChannelType.GENERAL, "[RED]World width and height must be larger than " + minSize + ".").sendPacket();
            return;
        }

        Tile[][] tiles = new Tile[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Tile tile = new Tile();
                tile.setTraversable(true);
                tiles[i][j] = tile;
            }
        }

        new ChatMessagePacketOut(commandSource.getPlayer(), ChatChannelType.GENERAL, "[RED] THIS NEEDS TO BE REDONE! PLEASE LOOK IT UP AND FIX... ").sendPacket();

//        GameWorld gameWorld = new GameWorld(worldName, width, height, tiles); //teleport <playerName> <worldName> <x> <y>
//        gameWorldProcessor.loadWorld(gameWorld);
//        gameWorldProcessor.loadEntities(gameWorld);
//        new ChatMessagePacketOut(commandSource.getPlayer(), ChatChannelType.GENERAL, "[GREEN]World " + worldName + " was successfully created. Use the following command to warp to the world").sendPacket();
//        new ChatMessagePacketOut(commandSource.getPlayer(), ChatChannelType.GENERAL, "[YELLOW] /teleport " + commandSource.getPlayer().getName() + " " + worldName + " 0 0").sendPacket();
    }
}
