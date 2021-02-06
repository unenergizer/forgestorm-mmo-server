package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.command.CommandSource;
import com.forgestorm.server.command.CommandState;
import com.forgestorm.server.database.AuthenticatedUser;
import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.MessageText;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.packet.out.ChatMessagePacketOut;
import com.forgestorm.server.network.game.shared.*;
import lombok.AllArgsConstructor;

import static com.forgestorm.server.util.Log.println;

@Opcode(getOpcode = Opcodes.CHAT)
public class ChatMessagePacketIn implements PacketListener<ChatMessagePacketIn.TextMessage> {

    private static final boolean PRINT_DEBUG = false;
    private static final String COMMAND_ERROR = "[RED][Command Error] ";

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        ChatChannelType chatChannelType = ChatChannelType.getChannelType(clientHandler.readByte());
        byte messageCount = clientHandler.readByte();
        StringBuilder message = new StringBuilder();

        println(getClass(), "Chat Channel: " + chatChannelType.name(), false, PRINT_DEBUG);
        println(getClass(), "Message Count: " + messageCount, false, PRINT_DEBUG);

        for (byte i = 0; i < messageCount; i++) {
            String string = clientHandler.readString();
            message.append(string);
            println(getClass(), "String Read: " + string, false, PRINT_DEBUG);
        }

        return new TextMessage(chatChannelType, message.toString());
    }

    @Override
    public boolean sanitizePacket(TextMessage packetData) {
        String text = packetData.text;

        // Make sure the incoming byte value isn't higher than the allowed
        if (packetData.chatChannelType.ordinal() >= ChatChannelType.values().length) return false;
        if (packetData.chatChannelType.ordinal() < 0) return false;
        if (packetData.text.length() > GameConstants.MAX_CHAT_LENGTH) return false;

        // Client can not send Combat chat channel messages
        if (packetData.chatChannelType == ChatChannelType.COMBAT) return false;

        if (text == null) return false;
        text = text.trim();

        if (text.isEmpty()) return false;

        println(getClass(), "Message: " + text, false, PRINT_DEBUG);

        return !text.contains("\n") && !text.contains("\r") && !text.contains("\t");
    }

    @Override
    public void onEvent(TextMessage packetData) {

        if (!attemptCommand(packetData)) {
            // TODO : Use StringBuilder

            ClientHandler messageSender = packetData.getClientHandler();
            AuthenticatedUser authenticatedSender = messageSender.getAuthenticatedUser();
            StringBuilder stringBuilder = new StringBuilder();

            if (authenticatedSender.isAdmin()) {
                stringBuilder.append(MessageText.ADMIN_COLOR);
            } else if (authenticatedSender.isModerator()) {
                stringBuilder.append(MessageText.MOD_COLOR);
            } else if (authenticatedSender.isContentDeveloper()) {
                stringBuilder.append(MessageText.CONTENT_DEVELOPER);
            } else {
                stringBuilder.append(MessageText.PLAYER_COLOR);
            }

            stringBuilder.append(messageSender.getPlayer().getName());
            stringBuilder.append(MessageText.CHAT_FORMATTING);
            stringBuilder.append(packetData.text);

            // Send the message to the client!
            ServerMain.getInstance().getGameManager().forAllPlayers(onlinePlayer -> {
                // If the message is a staff message, make sure we send it to the correct clients!
                if (packetData.chatChannelType == ChatChannelType.STAFF) {
                    AuthenticatedUser authenticatedReceiver = onlinePlayer.getClientHandler().getAuthenticatedUser();
                    if (authenticatedReceiver.isAdmin() || authenticatedReceiver.isModerator()) {
                        new ChatMessagePacketOut(onlinePlayer, packetData.chatChannelType, stringBuilder.toString()).sendPacket();
                    }
                } else {
                    // All other messages
                    new ChatMessagePacketOut(onlinePlayer, packetData.chatChannelType, stringBuilder.toString()).sendPacket();
                }
                println(getClass(), "[" + packetData.chatChannelType + "] " + packetData.getClientHandler().getPlayer().getName() + ": " + packetData.text);
            });
        }
    }

    private boolean attemptCommand(TextMessage packetData) {
        if (!packetData.text.startsWith("/")) return false;

        println(getClass(), packetData.getClientHandler().getPlayer().getName() + ": " + packetData.text);

        String[] content = packetData.text.split("\\s+");

        // Strip the "/" off
        content[0] = content[0].replace("/", "");

        CommandState commandState;
        if (content.length == 1) {
            commandState = ServerMain.getInstance().getCommandManager().getCommandProcessor().publish(new CommandSource(packetData.getClientHandler().getPlayer()), content[0], new String[0]);
        } else {
            String[] args = new String[content.length - 1];
            System.arraycopy(content, 1, args, 0, content.length - 1);
            commandState = ServerMain.getInstance().getCommandManager().getCommandProcessor().publish(new CommandSource(packetData.getClientHandler().getPlayer()), content[0], args);
        }

        Player player = packetData.getClientHandler().getPlayer();
        CommandState.CommandType commandType = commandState.getCommandType();
        String commandBase = commandState.getCommandBase();

        if (commandType == CommandState.CommandType.NOT_FOUND) {
            new ChatMessagePacketOut(player, packetData.chatChannelType, COMMAND_ERROR + "[WHITE]Unknown Command...").sendPacket();
        } else if (commandType == CommandState.CommandType.SINGE_INCOMPLETE) {
            new ChatMessagePacketOut(player, packetData.chatChannelType, COMMAND_ERROR + "[GREEN]Suggested alternatives:").sendPacket();
            new ChatMessagePacketOut(player, packetData.chatChannelType, " [YELLOW] 1. /" + commandBase + " " + commandState.getIncompleteMessage()).sendPacket();
        } else if (commandType == CommandState.CommandType.MULTIPLE_INCOMPLETE) {
            new ChatMessagePacketOut(player, packetData.chatChannelType, COMMAND_ERROR + "[GREEN]Suggested alternatives:").sendPacket();
            for (int i = 0; i < commandState.getMultipleIncompleteMessages().length; i++) {
                String incompleteMsg = commandState.getMultipleIncompleteMessages()[i];
                new ChatMessagePacketOut(player, packetData.chatChannelType, " [YELLOW] " + (i + 1) + ". /" + commandBase + " " + incompleteMsg).sendPacket();
            }
        } else if (commandType == CommandState.CommandType.INVALID_PERMISSION) {
            return false;
        }
        return true;
    }

    @AllArgsConstructor
    static class TextMessage extends PacketData {
        private final ChatChannelType chatChannelType;
        private final String text;
    }
}