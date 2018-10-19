package com.valenguard.server.commands.listeners;

import com.valenguard.server.commands.Command;
import com.valenguard.server.commands.CommandListener;

import java.nio.channels.Channel;

public class TicksPerSecond implements CommandListener {

    @Command(getCommands = {"/tps"})
    public void onTPSCmd(Channel playerChannel) {
//        ChatMessage message = new ChatMessage();
//        message.setMessage("TPS: " + RetroMmoServer.getInstance().getLoop().getCurrentTPS() + "\n");
//        playerChannel.writeAndFlush(message);
    }
}