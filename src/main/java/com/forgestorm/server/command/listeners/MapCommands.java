package com.forgestorm.server.command.listeners;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.command.Command;
import com.forgestorm.server.command.CommandSource;
import com.forgestorm.server.command.CommandArguments;
import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.game.world.maps.GameMap;
import com.forgestorm.server.game.world.maps.GameMapProcessor;
import com.forgestorm.server.game.world.maps.Tile;
import com.forgestorm.server.network.game.packet.out.ChatMessagePacketOut;

public class MapCommands {

    @Command(base = "create", argLenReq = 3)
    @CommandArguments(missing = "<mapName> <width> <height>")
    public void createWorld(CommandSource commandSource, String[] args) {
        GameMapProcessor gameMapProcessor = ServerMain.getInstance().getGameManager().getGameMapProcessor();
        final int minSize = 10;
        String mapName = args[0];
        int width = Integer.parseInt(args[1]);
        int height = Integer.parseInt(args[2]);

        // Check to make sure the map name doesn't already exist
        if (gameMapProcessor.doesGameMapExist(mapName)) {
            new ChatMessagePacketOut(commandSource.getPlayer(), ChatChannelType.GENERAL, "[RED]Map name already been used. Try a different name").sendPacket();
            return;
        }

        // Check map sizes
        if (width < minSize || height < minSize) {
            new ChatMessagePacketOut(commandSource.getPlayer(), ChatChannelType.GENERAL, "[RED]Map width and height must be larger than " + minSize + ".").sendPacket();
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

        GameMap gameMap = new GameMap(mapName, width, height, tiles); //teleport <playerName> <mapName> <x> <y>
        gameMapProcessor.loadMap(gameMap);
        gameMapProcessor.loadEntities(gameMap);
        new ChatMessagePacketOut(commandSource.getPlayer(), ChatChannelType.GENERAL, "[GREEN]Map " + mapName + " was successfully created. Use the following command to warp to the map").sendPacket();
        new ChatMessagePacketOut(commandSource.getPlayer(), ChatChannelType.GENERAL, "[YELLOW] /teleport " + commandSource.getPlayer().getName() + " " + mapName + " 0 0").sendPacket();
    }
}
