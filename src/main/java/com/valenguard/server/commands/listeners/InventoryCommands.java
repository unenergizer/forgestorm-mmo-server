package com.valenguard.server.commands.listeners;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.commands.Command;
import com.valenguard.server.commands.IncompleteCommand;
import com.valenguard.server.game.inventory.ItemStack;

public class InventoryCommands {

    @Command(base = "give", argLenReq = 1)
    @IncompleteCommand(missing = "give <itemId>")
    public void onGiveItem(String[] args) {

        try {

            int itemId = Integer.parseInt(args[0]);

            if (itemId < 0) {
                System.out.println("The itemId number cannot be below zero.");
                return;
            } else if (itemId > 1779) {
                System.out.println("The itemId number cannot be above 1779.");
                return;
            }

            ValenguardMain.getInstance().getGameManager().forAllPlayers(player ->
                    player.giveItem(new ItemStack(itemId, 1)));

        } catch (NumberFormatException e) {
            System.out.println("Must provide a itemId as a number.");
        }
    }

    @Command(base = "give", argLenReq = 2)
    @IncompleteCommand(missing = "give <itemId> <amount>")
    public void onGiveItems(String[] args) {

        System.out.println("Something incredible happened.");
    }

}
