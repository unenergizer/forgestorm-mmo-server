package com.valenguard.server.command.listeners;

import com.valenguard.server.Server;
import com.valenguard.server.command.Command;
import com.valenguard.server.command.CommandSource;
import com.valenguard.server.command.IncompleteCommand;
import com.valenguard.server.database.AuthenticatedUser;
import com.valenguard.server.game.MessageText;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.maps.GameMapProcessor;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.game.world.maps.MoveDirection;
import com.valenguard.server.game.world.maps.Warp;
import com.valenguard.server.network.game.packet.out.EntityHealPacketOut;
import com.valenguard.server.network.game.packet.out.EntityUpdatePacketOut;

public class PlayerCommands {

    @Command(base = "heal", argLenReq = 1)
    @IncompleteCommand(missing = "heal <packetReceiver>")
    public void healPlayer(CommandSource commandSource, String[] args) {

        short playerId = 0;
        boolean findById = true;

        try {
            playerId = Short.parseShort(args[0]);
        } catch (NumberFormatException e) {
            findById = false;
        }

        Player player;
        if (findById) {
            player = Server.getInstance().getGameManager().findPlayer(playerId);
            if (player == null) {
                commandSource.sendMessage("Could not find a packetReceiver for id: " + playerId);
                return;
            }
        } else {
            player = getPlayer(commandSource, args[0]);
            if (player == null) return;
        }

        commandSource.sendMessage("Healed packetReceiver " + player.getName());

        final Player sendPlayer = player;
        player.getGameMap().getPlayerController().forAllPlayers(anyPlayer ->
                new EntityHealPacketOut(anyPlayer, sendPlayer, sendPlayer.getMaxHealth() - sendPlayer.getCurrentHealth()).sendPacket());
        player.setCurrentHealth(player.getMaxHealth());
    }


    @Command(base = "teleport", argLenReq = 4)
    @IncompleteCommand(missing = "teleport <playerName> <mapName> <x> <y>")
    public void teleportPlayer(CommandSource commandSource, String[] args) {
        String playerName = args[0];
        String mapName = args[1];
        short x;
        short y;

        try {
            x = Short.parseShort(args[2]);
            y = Short.parseShort(args[3]);
        } catch (NumberFormatException e) {
            commandSource.sendMessage("Command arguments must contain valid numbers.");
            commandSource.sendMessage("Accepted Args: teleport <playerName> <mapName> <x> <y>");
            return;
        }

        GameMapProcessor gameMapProcessor = Server.getInstance().getGameManager().getGameMapProcessor();
        Location location = new Location(mapName, x, y);

        if (!gameMapProcessor.doesGameMapExist(mapName)) {
            commandSource.sendMessage("The map <" + mapName + "> does not exist. Check spelling.");
            return;
        }

        if (!gameMapProcessor.doesLocationExist(location)) {
            commandSource.sendMessage("These coordinates <" + x + "," + y + "> are not valid.");
            return;
        }

        Player player = getPlayer(commandSource, playerName);
        if (player == null) return;

        player.setWarp(new Warp(new Location(mapName, x, y), MoveDirection.SOUTH));
        commandSource.sendMessage("Sending <" + playerName + "> to " + location.toString());
    }

    @Command(base = "kick", argLenReq = 1)
    @IncompleteCommand(missing = "kick <playerName>")
    public void kickPlayer(CommandSource commandSource, String[] args) {
        String playerName = args[0];
        Player player = getPlayer(commandSource, playerName);
        if (player == null) return;

        Server.getInstance().getGameManager().kickPlayer(player);
        Server.getInstance().getNetworkManager().getOutStreamManager().removeClient(player.getClientHandler());
        player.getClientHandler().closeConnection();
    }

    @Command(base = "kill", argLenReq = 1)
    @IncompleteCommand(missing = "kick <playerName>")
    public void killPlayer(CommandSource commandSource, String[] args) {
        String playerName = args[0];
        Player player = getPlayer(commandSource, playerName);
        if (player == null) return;

        player.killPlayer();
    }

    @Command(base = "info", argLenReq = 1)
    @IncompleteCommand(missing = "info <playerName>")
    public void getPlayerInfo(CommandSource commandSource, String[] args) {
        String playerName = args[0];
        Player player = getPlayer(commandSource, playerName);
        if (player == null) return;

        AuthenticatedUser authenticatedUser = player.getClientHandler().getAuthenticatedUser();

        commandSource.sendMessage(" ");
        commandSource.sendMessage("---- Player Information ----");
        commandSource.sendMessage("Account Name: " + authenticatedUser.getXfAccountName());
        commandSource.sendMessage("Character Id: " + player.getCharacterDatabaseId());
        commandSource.sendMessage("Account Id: " + authenticatedUser.getDatabaseUserId());
        commandSource.sendMessage("Admin: " + authenticatedUser.isAdmin());
        commandSource.sendMessage("IP: " + authenticatedUser.getIp());
    }

    @Command(base = "speed", argLenReq = 2)
    @IncompleteCommand(missing = "info <playerName> <speed>")
    public void setPlayerSpeed(CommandSource commandSource, String[] args) {
        String playerName = args[0];
        Player player = getPlayer(commandSource, playerName);
        if (player == null) return;

        float oldMoveSpeed = player.getMoveSpeed();
        float moveSpeed = Float.parseFloat(args[1]);
        player.setMoveSpeed(moveSpeed);
        commandSource.sendMessage(MessageText.SERVER + playerName + " move speed set to " + player.getMoveSpeed() + " from " + oldMoveSpeed + ".");

        player.getGameMap().getPlayerController().forAllPlayers(anyPlayer ->
                new EntityUpdatePacketOut(anyPlayer, player, moveSpeed).sendPacket());
    }

    private Player getPlayer(CommandSource commandSource, String playerName) {
        Player player = Server.getInstance().getGameManager().findPlayer(playerName);

        if (player == null) {
            commandSource.sendMessage("The player <" + playerName + "> could not be found. Check spelling.");
            return null;
        }

        return player;
    }
}
