package com.valenguard.server.command.listeners;

import com.valenguard.server.Server;
import com.valenguard.server.command.Command;
import com.valenguard.server.command.CommandSource;

public class ShutdownCommand {

    @Command(base = "shutdown")
    public void onShutdown(CommandSource commandSource) {
        Server.getInstance().exitServer();
    }

    @Command(base = "exit")
    public void onExit(CommandSource commandSource) {
        Server.getInstance().exitServer();
    }

    @Command(base = "stop")
    public void onStop(CommandSource commandSource) {
        Server.getInstance().exitServer();
    }
}
