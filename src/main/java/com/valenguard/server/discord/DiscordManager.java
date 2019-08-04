package com.valenguard.server.discord;


import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

import javax.security.auth.login.LoginException;
import java.util.List;

import static com.valenguard.server.util.Log.println;

public class DiscordManager {

    private static final String BOT_TOKEN = "NDgwMTc0NzE1OTgyNDQ2NjM4.XUa7uQ.ZhBHLlMFh1AQW9ELkXOrEKCsDAc";
    private static final String CONSOLE_CHANNEL = "607537864275656724";
    private static final boolean USE_DISCORD_LOGGING = true;

    private JDA jdaEvent;
    private boolean isReady = false;

    public void start() {
        try {
            JDA jda = new JDABuilder(BOT_TOKEN)
                    .addEventListener(new ListenerAdapter() {
                        @Override
                        public void onReady(ReadyEvent event) {
                            jdaEvent = event.getJDA();
                            isReady = true;


                            println(DiscordManager.class, "error test!", true);
                        }
                    })
                    .addEventListener(new DiscordListeners())
                    .build();
            try {
                jda.awaitReady();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (LoginException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a message to the Discord console-channel.
     *
     * @param message The message that will be sent.
     */
    public void sendDiscordMessage(String message) {
        if (!USE_DISCORD_LOGGING) return;
        sendDiscordMessage(jdaEvent.getTextChannelById(CONSOLE_CHANNEL), message);
    }

    /**
     * Sends a message to a specific Discord text channel.
     *
     * @param textChannel The channel to send a message to.
     * @param message     The message that will be sent.
     */
    public void sendDiscordMessage(TextChannel textChannel, String message) {
        if (!isReady) return;
        textChannel.sendMessage(message).queue();
    }


    class DiscordListeners extends ListenerAdapter {

        @Override
        public void onMessageReceived(MessageReceivedEvent event) {
            if (event.getAuthor().isBot()) return;
            Message message = event.getMessage();
            String content = message.getContentRaw();
            Guild guild = event.getGuild();
            TextChannel textChannel = event.getTextChannel();

            // Used to get a list of text channels and IDs. Good to set console text channel variables.
            if (content.equalsIgnoreCase("!channelInfo")) {
                sendDiscordMessage(textChannel, "** **");
                sendDiscordMessage(textChannel, "**Guild Information**");
                sendDiscordMessage(textChannel, "GuildName: " + guild.getName());
                sendDiscordMessage(textChannel, "GuildID: " + guild.getIdLong());

                sendDiscordMessage(textChannel, "**Roles List**");
                List<Role> roleList = event.getJDA().getRoles();
                for (Role role : roleList) {
                    sendDiscordMessage(textChannel, "RoleName: " + role.getName());
                    sendDiscordMessage(textChannel, "RoleID: " + role.getIdLong());
                }

                sendDiscordMessage(textChannel, "**Channel List**");
                List<TextChannel> channelList = event.getJDA().getTextChannels();
                for (TextChannel channel : channelList) {
                    sendDiscordMessage(textChannel, "ChannelName: " + channel.getName());
                    sendDiscordMessage(textChannel, "ChannelID: " + channel.getIdLong());
                    sendDiscordMessage(textChannel, "CanTalk: " + channel.canTalk());
                }
            }
        }
    }
}
