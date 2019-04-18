package com.valenguard.server.command.listeners;

import com.valenguard.server.Server;
import com.valenguard.server.command.Command;
import com.valenguard.server.command.CommandSource;

public class TicksPerSecondCommand {

    @Command(base = "tps")
    public void onTPSCmd(CommandSource commandSource) {
        commandSource.sendMessage("TPS: " + Server.getInstance().getGameLoop().getCurrentTPS());
    }
}