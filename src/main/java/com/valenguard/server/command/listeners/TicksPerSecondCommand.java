package com.valenguard.server.command.listeners;

import com.valenguard.server.Server;
import com.valenguard.server.command.Command;
import com.valenguard.server.util.Log;

public class TicksPerSecondCommand {

    @Command(base = "tps")
    public void onTPSCmd() {
        Log.println(getClass(), "TPS: " + Server.getInstance().getGameLoop().getCurrentTPS());
    }
}