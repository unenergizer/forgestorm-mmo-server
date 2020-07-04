package com.forgestorm.server.command.listeners;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.command.Command;
import com.forgestorm.server.command.CommandSource;
import com.forgestorm.server.command.EndlessArguments;
import com.forgestorm.server.command.IncompleteCommand;
import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.game.MessageText;
import com.forgestorm.server.network.game.packet.out.ChatMessagePacketOut;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class ServerCommands {

    @Command(base = "tps")
    public void getTps(CommandSource commandSource) {
        commandSource.sendMessage("TPS: " + ServerMain.getInstance().getGameLoop().getCurrentTPS());
    }

    @Command(base = "stop")
    public void onStop(CommandSource commandSource) {
        ServerMain.getInstance().exitServer();
    }

    @Command(base = "online")
    public void accountsOnline(CommandSource commandSource) {
        commandSource.sendMessage("Accounts Online: " + ServerMain.getInstance().getNetworkManager().getOutStreamManager().clientsOnline());
    }

    @Command(base = "time")
    public void getServerTime(CommandSource commandSource) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        commandSource.sendMessage("Server Time: " + dtf.format(now));
    }

    @Command(base = "uptime")
    public void getServerUpTime(CommandSource commandSource) {

        long upTime = System.currentTimeMillis() - ServerMain.SERVER_START_TIME;

        long days = TimeUnit.MILLISECONDS.toDays(upTime);
        upTime -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(upTime);
        upTime -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(upTime);
        upTime -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(upTime);

        String time = String.format("%d days, %d hours, %d minutes, %d seconds", days, hours, minutes, seconds);

        commandSource.sendMessage("Server UpTime: " + time);
    }

    @Command(base = "say")
    @IncompleteCommand(missing = "say <Message...>")
    @EndlessArguments
    public void serverSay(CommandSource commandSource, String[] args) {
        ServerMain.getInstance().getGameManager().forAllPlayers(anyPlayer ->
                new ChatMessagePacketOut(anyPlayer, ChatChannelType.GENERAL, MessageText.SERVER + String.join(" ", args)).sendPacket());
    }
}
