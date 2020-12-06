package com.forgestorm.server.command.listeners;

import com.forgestorm.server.command.Command;
import com.forgestorm.server.command.CommandSource;
import com.forgestorm.server.command.CommandArguments;
import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.network.game.packet.out.ChatMessagePacketOut;

public class EntityCommands {

    @Command(base = "entity", argLenReq = 1)
    @CommandArguments(missing = "<argument>")
    public void getEntityCount(CommandSource commandSource, String[] args) {
        if (args[0].equalsIgnoreCase("count")) {
            int aiEntities = commandSource.getPlayer().getGameWorld().getAiEntityController().getEntities().size();
            commandSource.sendMessage("[YELLOW]Count: [GREEN]" + aiEntities);
        }
    }
}
