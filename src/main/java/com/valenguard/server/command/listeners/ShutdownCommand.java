package com.valenguard.server.command.listeners;

import com.valenguard.server.Server;
import com.valenguard.server.command.Command;

public class ShutdownCommand {

    @Command(base = "shutdown")
    public void onShutdown() {
        Server.getInstance().exitServer();
    }

    @Command(base = "exit")
    public void onExit() {
        Server.getInstance().exitServer();
    }
}
