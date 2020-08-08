package com.forgestorm.server.command.listeners;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.command.Command;
import com.forgestorm.server.command.CommandManager;
import com.forgestorm.server.command.CommandSource;
import com.forgestorm.server.command.CommandArguments;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.item.ItemStackManager;
import com.forgestorm.server.game.world.item.inventory.InventoryConstants;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class InventoryCommands {

    private final CommandManager commandManager;
    
    @Command(base = "clearinv", argLenReq = 1)
    @CommandArguments(missing = "<playerName>")
    public void onClearItems(CommandSource commandSource, String[] args) {

        String playerName = args[0];
        Player player = commandManager.getPlayer(commandSource, playerName);

        if (player == null) {
            commandSource.sendMessage("Could not find player : " + playerName);
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
    @CommandArguments(missing = "<itemId>")
    public void onGiveItem(CommandSource commandSource, String[] args) {

        int itemId = parseItemId(commandSource, args[0]);
        if (itemId < 0) return;

        ServerMain.getInstance().getGameManager().forAllPlayers(player ->
                player.give(ServerMain.getInstance().getItemStackManager().makeItemStack(itemId, 1), true));

    }

    @Command(base = "give", argLenReq = 2)
    @CommandArguments(missing = "<itemId> <amount>")
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
