package com.forgestorm.server.command.listeners;

import com.forgestorm.server.command.*;
import com.forgestorm.server.game.world.entity.Player;

public class MessageCommands {

    @Command(base = "msg")
    @IncompleteCommand(missing =  "msg <Message...>")
    @EndlessArguments
    @CommandPermission(status = CommandPermStatus.ALL)
    public void msg(CommandSource commandSource, String[] args) {

        Player player = commandSource.getPlayer();



        StringBuilder messageBuilder = new StringBuilder(args[0]);
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(" ").append(args[i]);
        }

        String message = messageBuilder.toString();

        System.out.println("Player: " + player + "is trying to send message: " + message);

    }
}
