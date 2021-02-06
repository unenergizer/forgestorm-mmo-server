package com.forgestorm.server.command.listeners;

import com.forgestorm.server.command.*;

public class EntityCommands {

    @Command(base = "entity", argLenReq = 1)
    @CommandArguments(missing = "<argument> [count]")
    @CommandPermission(status = CommandPermStatus.CONTENT_DEVELOPER)
    public void getEntityCount(CommandSource commandSource, String[] args) {
        if (args[0].equalsIgnoreCase("count")) {
            int aiEntities = commandSource.getPlayer().getGameWorld().getAiEntityController().getEntities().size();
            commandSource.sendMessage("[YELLOW]Count: [GREEN]" + aiEntities);
        }
    }
}
