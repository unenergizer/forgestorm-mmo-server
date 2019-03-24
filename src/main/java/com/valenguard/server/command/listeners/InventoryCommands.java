package com.valenguard.server.command.listeners;

import com.valenguard.server.Server;
import com.valenguard.server.command.Command;
import com.valenguard.server.command.IncompleteCommand;

public class InventoryCommands {

    @Command(base = "give", argLenReq = 1)
    @IncompleteCommand(missing = "give <itemId>")
    public void onGiveItem(String[] args) {

        int itemId = parseItemId(args[0]);
        if (itemId < 0) return;

        Server.getInstance().getGameManager().forAllPlayers(player ->
                player.giveItemStack(Server.getInstance().getItemStackManager().makeItemStack(itemId, 1)));

    }

    @Command(base = "give", argLenReq = 2)
    @IncompleteCommand(missing = "give <itemId> <amount>")
    public void onGiveItems(String[] args) {

        int itemId = parseItemId(args[0]);

        try {

            int itemAmount = Integer.parseInt(args[1]);

            if (itemAmount < 1) {
                System.out.println("Item amount must be at least 1.");
                return;
            }

            Server.getInstance().getGameManager().forAllPlayers(player ->
                    player.giveItemStack(Server.getInstance().getItemStackManager().makeItemStack(itemId, itemAmount)));

        } catch (NumberFormatException e) {
            System.out.println("Must provide an itemAmount as a number.");
        }
    }

    private int parseItemId(String argument) {

        int itemId;

        try {

            itemId = Integer.parseInt(argument);

            int maxItems = Server.getInstance().getItemStackManager().numberOfItems() - 1;
            if (itemId < 0) {
                System.out.println("The itemId number cannot be below zero.");
                return -1;
            } else if (itemId > maxItems) {
                System.out.println("The itemId number cannot be above " + maxItems + ".");
                return -1;
            }

        } catch (NumberFormatException e) {
            System.out.println("Must provide an itemId as a number.");
            return -1;
        }

        return itemId;
    }

}
