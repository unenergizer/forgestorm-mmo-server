package com.valenguard.server.command.listeners;

import com.valenguard.server.command.Command;
import com.valenguard.server.command.CommandSource;
import com.valenguard.server.command.IncompleteCommand;
import com.valenguard.server.network.game.packet.out.ChatMessagePacketOut;

public class EntityCommands {

    @Command(base = "entity", argLenReq = 1)
    @IncompleteCommand(missing = "entity <argument>")
    public void healPlayer(CommandSource commandSource, String[] args) {
        if (args[0].equalsIgnoreCase("count")) {
            int aiEntities = commandSource.getPlayer().getGameMap().getAiEntityController().getEntities().size();
            new ChatMessagePacketOut(commandSource.getPlayer(), "Count: " + aiEntities).sendPacket();
        }
    }
}
