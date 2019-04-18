package com.valenguard.server.command.listeners;

import com.valenguard.server.Server;
import com.valenguard.server.command.Command;
import com.valenguard.server.command.CommandSource;

public class PlayersOnlineCommand {

    @Command(base = "online")
    public void onTPSCmd(CommandSource commandSource) {
        commandSource.sendMessage("Accounts Online: " + Server.getInstance().getNetworkManager().getOutStreamManager().clientsOnline());
    }
}