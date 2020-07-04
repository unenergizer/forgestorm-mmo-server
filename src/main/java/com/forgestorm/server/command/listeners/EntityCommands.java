package com.forgestorm.server.command.listeners;

import com.forgestorm.server.command.Command;
import com.forgestorm.server.command.CommandSource;
import com.forgestorm.server.command.IncompleteCommand;
import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.network.game.packet.out.ChatMessagePacketOut;

public class EntityCommands {

    @Command(base = "entity", argLenReq = 1)
    @IncompleteCommand(missing = "entity <argument>")
    public void healPlayer(CommandSource commandSource, String[] args) {
        if (args[0].equalsIgnoreCase("count")) {
            int aiEntities = commandSource.getPlayer().getGameMap().getAiEntityController().getEntities().size();
            new ChatMessagePacketOut(commandSource.getPlayer(), ChatChannelType.GENERAL, "Count: " + aiEntities).sendPacket();
        }
    }
}
