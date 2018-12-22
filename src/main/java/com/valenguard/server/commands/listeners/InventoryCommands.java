package com.valenguard.server.commands.listeners;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.commands.Command;
import com.valenguard.server.commands.IncompleteCommand;
import com.valenguard.server.game.inventory.ItemStack;

public class InventoryCommands {

    @Command(base = "give", argLenReq = 1)
    @IncompleteCommand(missing = "give <itemId>")
    public void onGiveItem(String[] args) {

        int itemId = parseItemId(args[0]);
        if (itemId < 0) return;

        ValenguardMain.getInstance().getGameManager().forAllPlayers(player ->
                player.giveItemStack(new ItemStack(itemId, 1)));

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

            ValenguardMain.getInstance().getGameManager().forAllPlayers(player ->
                    player.giveItemStack(new ItemStack(itemId, itemAmount)));

        } catch (NumberFormatException e) {
            System.out.println("Must provide an itemAmount as a number.");
        }
    }

    private int parseItemId(String argument) {

        int itemId;

        try {

            itemId = Integer.parseInt(argument);

            if (itemId < 0) {
                System.out.println("The itemId number cannot be below zero.");
                return -1;
            } else if (itemId > 1779) {
                System.out.println("The itemId number cannot be above 1779.");
                return -1;
            }

        } catch (NumberFormatException e) {
            System.out.println("Must provide an itemId as a number.");
            return -1;
        }

        return itemId;
    }

}
