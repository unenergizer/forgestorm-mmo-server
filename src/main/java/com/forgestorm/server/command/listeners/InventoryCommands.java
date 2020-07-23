package com.forgestorm.server.command.listeners;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.command.Command;
import com.forgestorm.server.command.CommandSource;
import com.forgestorm.server.command.IncompleteCommand;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.item.ItemStackManager;
import com.forgestorm.server.game.world.item.inventory.InventoryConstants;

public class InventoryCommands {

    @Command(base = "clearinv", argLenReq = 1)
    @IncompleteCommand(missing = "clearinv <playerId>")
    public void onClearItems(CommandSource commandSource, String[] args) {

        short playerId;
        try {

            playerId = Short.parseShort(args[0]);

        } catch (NumberFormatException e) {
            commandSource.sendMessage("Could not parse playerId");
            return;
        }

        Player player = ServerMain.getInstance().getGameManager().findPlayer(playerId);

        if (player == null) {
            commandSource.sendMessage("Could not find player for Id: " + playerId);
            return;
        }

        for (byte i = 0; i < InventoryConstants.BAG_SIZE; i++) {
            player.getPlayerBag().removeItemStack(i, true);
        }
    }

    @Command(base = "giveall")
    public void onGiveAll(CommandSource commandSource) {

        ItemStackManager itemStackManager = ServerMain.getInstance().getItemStackManager();
        for (int item = 0; item < itemStackManager.getNumberOfItems(); item++) {
            int finalItem = item;
            ServerMain.getInstance().getGameManager().forAllPlayers(player ->
                    player.give(itemStackManager.makeItemStack(finalItem, 1), true));
        }
    }

    @Command(base = "give", argLenReq = 1)
    @IncompleteCommand(missing = "give <itemId>")
    public void onGiveItem(CommandSource commandSource, String[] args) {

        int itemId = parseItemId(commandSource, args[0]);
        if (itemId < 0) return;

        ServerMain.getInstance().getGameManager().forAllPlayers(player ->
                player.give(ServerMain.getInstance().getItemStackManager().makeItemStack(itemId, 1), true));

    }

    @Command(base = "give", argLenReq = 2)
    @IncompleteCommand(missing = "give <itemId> <amount>")
    public void onGiveItems(CommandSource commandSource, String[] args) {

        int itemId = parseItemId(commandSource, args[0]);

        try {

            int itemAmount = Integer.parseInt(args[1]);

            if (itemAmount < 1) {
                commandSource.sendMessage("Item amount must be at least 1.");
                return;
            }

            ServerMain.getInstance().getGameManager().forAllPlayers(player ->
                    player.give(ServerMain.getInstance().getItemStackManager().makeItemStack(itemId, itemAmount), true));

        } catch (NumberFormatException e) {
            commandSource.sendMessage("Must provide an itemAmount as a number.");
        }
    }

    private int parseItemId(CommandSource commandSource, String argument) {

        int itemId;

        try {

            itemId = Integer.parseInt(argument);

            int maxItems = ServerMain.getInstance().getItemStackManager().getNumberOfItems() - 1;
            if (itemId < 0) {
                commandSource.sendMessage("The itemId number cannot be below zero.");
                return -1;
            } else if (itemId > maxItems) {
                commandSource.sendMessage("The itemId number cannot be above " + maxItems + ".");
                return -1;
            }

        } catch (NumberFormatException e) {
            commandSource.sendMessage("Must provide an itemId as a number.");
            return -1;
        }

        return itemId;
    }

}
