package com.valenguard.server.commands.listeners;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.commands.Command;
import com.valenguard.server.util.Log;

public class TicksPerSecond {

    @Command(base = "tps")
    public void onTPSCmd() {
        Log.println(getClass(), "TPS: " + ValenguardMain.getInstance().getGameLoop().getCurrentTPS());
    }
}