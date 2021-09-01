package com.forgestorm.server.command.listeners;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.command.*;
import com.forgestorm.server.database.AuthenticatedUser;
import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.game.MessageText;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.maps.GameWorldProcessor;
import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.server.game.world.maps.MoveDirection;
import com.forgestorm.server.game.world.maps.Warp;
import com.forgestorm.server.network.game.packet.out.EntityHealPacketOut;
import com.forgestorm.server.network.game.packet.out.EntityUpdatePacketOut;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PlayerCommands {

    private final CommandManager commandManager;

    @Command(base = "heal", argLenReq = 1)
    @CommandArguments(missing = "<playerName>")
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
            player = ServerMain.getInstance().getGameManager().findPlayer(playerId);
            if (player == null) {
                commandSource.sendMessage("[RED]Could not find a player for id: " + playerId);
                return;
            }
        } else {
            player = commandManager.getPlayer(commandSource, args[0]);
            if (player == null) return;
        }

        commandSource.sendMessage("[GREEN]Healed player " + player.getName());

        final Player sendPlayer = player;
        player.getGameWorld().getPlayerController().forAllPlayers(anyPlayer ->
                new EntityHealPacketOut(anyPlayer, sendPlayer, sendPlayer.getMaxHealth() - sendPlayer.getCurrentHealth()).sendPacket());
        player.setCurrentHealth(player.getMaxHealth());
    }

    @Command(base = "teleport", argLenReq = 1)
    @CommandArguments(missing = "<playerName>")
    @CommandPermission(status = CommandPermStatus.MOD)
    public void teleportToPlayer(CommandSource commandSource, String[] args) {
        Player player = commandSource.getPlayer();
        Player teleportToPlayer = commandManager.getPlayer(commandSource, args[0]);

        if (teleportToPlayer == null) return;
        player.setWarp(new Warp(new Location(teleportToPlayer.getCurrentWorldLocation()), teleportToPlayer.getFacingDirection()));
        commandSource.sendMessage("[GREEN]You have teleported to [YELLOW]" + teleportToPlayer.getName() + "[GREEN].");
    }

    @Command(base = "teleport", argLenReq = 5)
    @CommandArguments(missing = "<playerName> <mapName> <x> <y> <z>")
    @CommandPermission(status = CommandPermStatus.MOD)
    public void teleportPlayer(CommandSource commandSource, String[] args) {
        String playerName = args[0];
        String mapName = args[1];
        int x, y;
        short z;

        try {
            x = Integer.parseInt(args[2]);
            y = Integer.parseInt(args[3]);
            z = Short.parseShort(args[4]);
        } catch (NumberFormatException e) {
            commandSource.sendMessage("[RED]Command arguments must contain valid numbers.");
            commandSource.sendMessage("[RED]Accepted Args: teleport <playerName> <mapName> <x> <y> <z>");
            return;
        }

        GameWorldProcessor gameWorldProcessor = ServerMain.getInstance().getGameManager().getGameWorldProcessor();
        Location location = new Location(mapName, x, y, z);

        if (!gameWorldProcessor.doesGameWorldExist(mapName)) {
            commandSource.sendMessage("[RED]The map <" + mapName + "> does not exist. Check spelling.");
            return;
        }

        if (!location.getGameWorld().isTraversable(location)) {
            commandSource.sendMessage("[RED]These coordinates <" + x + "," + y + "> are not valid. Tile has collision.");
            return;
        }

        Player player = commandManager.getPlayer(commandSource, playerName);
        if (player == null) return;

        player.setWarp(new Warp(new Location(mapName, x, y, z), MoveDirection.SOUTH));
        commandSource.sendMessage("[YELLOW]Sending <" + playerName + "> to " + location.toString());
    }

    @Command(base = "kick", argLenReq = 1)
    @CommandArguments(missing = "<playerName>")
    @CommandPermission(status = CommandPermStatus.MOD)
    public void kickPlayer(CommandSource commandSource, String[] args) {
        String playerName = args[0];
        Player player = commandManager.getPlayer(commandSource, playerName);
        if (player == null) return;

        ServerMain.getInstance().getGameManager().kickPlayer(player);
        ServerMain.getInstance().getNetworkManager().getOutStreamManager().removeClient(player.getClientHandler());
        player.getClientHandler().closeConnection();
        commandSource.sendMessage("[RED]" + player.getName() + " [YELLOW]has been kicked.");
    }

    @Command(base = "kill", argLenReq = 1)
    @CommandArguments(missing = "<playerName>")
    @CommandPermission(status = CommandPermStatus.MOD)
    public void killPlayer(CommandSource commandSource, String[] args) {
        String playerName = args[0];
        Player player = commandManager.getPlayer(commandSource, playerName);
        if (player == null) return;

        player.killPlayer();
        commandSource.sendMessage("[RED]" + player.getName() + " [YELLOW]has been killed.");
    }

    @Command(base = "info", argLenReq = 1)
    @CommandArguments(missing = "<playerName>")
    @CommandPermission(status = CommandPermStatus.MOD)
    public void getPlayerInfo(CommandSource commandSource, String[] args) {
        String playerName = args[0];
        Player player = commandManager.getPlayer(commandSource, playerName);
        if (player == null) return;

        AuthenticatedUser authenticatedUser = player.getClientHandler().getAuthenticatedUser();

        commandSource.sendMessage(" ");
        commandSource.sendMessage("[YELLOW]---- Player Information ----");
        commandSource.sendMessage("[GREEN]Account Name" + MessageText.CHAT_FORMATTING + authenticatedUser.getXfAccountName());
        commandSource.sendMessage("[GREEN]Database Id" + MessageText.CHAT_FORMATTING + player.getDatabaseId());
        commandSource.sendMessage("[GREEN]XF Account Id" + MessageText.CHAT_FORMATTING + authenticatedUser.getDatabaseUserId());
        commandSource.sendMessage("[GREEN]Admin" + MessageText.CHAT_FORMATTING + authenticatedUser.isAdmin());
        commandSource.sendMessage("[GREEN]IP" + MessageText.CHAT_FORMATTING + authenticatedUser.getIp());
    }

    @Command(base = "speed", argLenReq = 2)
    @CommandArguments(missing = "<playerName> <speed>")
    public void setPlayerSpeed(CommandSource commandSource, String[] args) {
        String playerName = args[0];
        Player player = commandManager.getPlayer(commandSource, playerName);
        if (player == null) return;

        float oldMoveSpeed = player.getMoveSpeed();

        try {
            float moveSpeed = Float.parseFloat(args[1]);

            if (moveSpeed > 59F) {
                commandSource.sendMessage(MessageText.SERVER + "Speed cannot be above 59.");
                return;
            }

            player.setMoveSpeed(moveSpeed);
            commandSource.sendMessage(MessageText.SERVER + playerName + " move speed set to " + player.getMoveSpeed() + " from " + oldMoveSpeed + ".");

            player.getGameWorld().getPlayerController().forAllPlayers(anyPlayer ->
                    new EntityUpdatePacketOut(anyPlayer, player, moveSpeed).sendPacket());

        } catch (NumberFormatException e) {
            commandSource.sendMessage(MessageText.SERVER + "Second argument must be a float.");
        }
    }

    @Command(base = "collision")
    @CommandPermission(status = CommandPermStatus.CONTENT_DEVELOPER)
    public void toggleCollision(CommandSource commandSource) {
        Player player = commandSource.getPlayer();
        if (player == null) return;

        boolean bypassCollision = !player.isBypassCollision();
        player.setBypassCollision(bypassCollision);

        if (bypassCollision) {
            commandSource.sendMessage("[YELLOW]Collision was toggled [RED]OFF [YELLOW]for player [ORANGE]" + player.getName() + "[YELLOW].", ChatChannelType.STAFF);
        } else {
            commandSource.sendMessage("[YELLOW]Collision was toggled [GREEN]ON [YELLOW]for player [ORANGE]" + player.getName() + "[YELLOW].", ChatChannelType.STAFF);
        }
    }
}
