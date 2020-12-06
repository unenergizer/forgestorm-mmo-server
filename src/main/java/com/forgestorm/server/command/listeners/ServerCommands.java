package com.forgestorm.server.command.listeners;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.command.*;
import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.game.MessageText;
import com.forgestorm.server.network.game.packet.out.ChatMessagePacketOut;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class ServerCommands {

    private final CommandManager commandManager;

    @Command(base = "commands")
    @CommandPermission(status = CommandPermStatus.ALL)
    public void getCommandList(CommandSource commandSource) {
        commandManager.getCommandProcessor().sendCommandList(commandSource);
    }

    @Command(base = "tps")
    public void getTps(CommandSource commandSource) {
        commandSource.sendMessage("[YELLOW]TPS: " + ServerMain.getInstance().getGameLoop().getCurrentTPS());
    }

    @Command(base = "stop")
    public void onStop(CommandSource commandSource) {
        ServerMain.getInstance().exitServer();
    }

    @Command(base = "online")
    @CommandPermission(status = CommandPermStatus.ALL)
    public void accountsOnline(CommandSource commandSource) {
        commandSource.sendMessage("[YELLOW]Accounts Online: " + ServerMain.getInstance().getNetworkManager().getOutStreamManager().clientsOnline());
    }

    @Command(base = "time")
    public void getServerTime(CommandSource commandSource) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        commandSource.sendMessage("[YELLOW]Server Time: " + dtf.format(now));
    }

    @Command(base = "uptime")
    @CommandPermission(status = CommandPermStatus.ALL)
    public void getServerUpTime(CommandSource commandSource) {

        long upTime = System.currentTimeMillis() - ServerMain.SERVER_START_TIME;

        long days = TimeUnit.MILLISECONDS.toDays(upTime);
        upTime -= TimeUnit.DAYS.toMillis(days);
        long hours = TimeUnit.MILLISECONDS.toHours(upTime);
        upTime -= TimeUnit.HOURS.toMillis(hours);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(upTime);
        upTime -= TimeUnit.MINUTES.toMillis(minutes);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(upTime);

        String time = String.format("[GREEN]%d [YELLOW]days, [GREEN]%d [YELLOW]hours, [GREEN]%d [YELLOW]minutes, [GREEN]%d [YELLOW]seconds", days, hours, minutes, seconds);

        commandSource.sendMessage("[YELLOW]Server UpTime: " + time);
    }

    @Command(base = "say")
    @CommandArguments(missing = "<message...>")
    @EndlessArguments
    public void serverSay(CommandSource commandSource, String[] args) {
        ServerMain.getInstance().getGameManager().forAllPlayers(anyPlayer ->
                new ChatMessagePacketOut(anyPlayer, ChatChannelType.GENERAL, MessageText.SERVER + String.join(" ", args)).sendPacket());
    }
}
