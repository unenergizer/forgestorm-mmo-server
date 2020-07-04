package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.command.CommandSource;
import com.forgestorm.server.command.CommandState;
import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.game.MessageText;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.packet.out.ChatMessagePacketOut;
import com.forgestorm.server.network.game.shared.*;
import lombok.AllArgsConstructor;

import static com.forgestorm.server.util.Log.println;

@Opcode(getOpcode = Opcodes.CHAT)
public class ChatMessagePacketIn implements PacketListener<ChatMessagePacketIn.TextMessage> {

    private static final boolean PRINT_DEBUG = false;

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        ChatChannelType chatChannelType = ChatChannelType.getChannelType(clientHandler.readByte());
        String message = clientHandler.readString();
        return new TextMessage(chatChannelType, message);
    }

    @Override
    public boolean sanitizePacket(TextMessage packetData) {
        String text = packetData.text;

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
            StringBuilder stringBuilder = new StringBuilder();

            if (messageSender.getAuthenticatedUser().isAdmin()) {
                stringBuilder.append("[RED]");
            } else if (messageSender.getAuthenticatedUser().isModerator()) {
                stringBuilder.append("[PURPLE]");
            } else {
                stringBuilder.append("[LIGHT_GRAY]");
            }

            stringBuilder.append(messageSender.getPlayer().getName());
            stringBuilder.append("[DARK_GRAY]: [WHITE]");
            stringBuilder.append(packetData.text);

            ServerMain.getInstance().getGameManager().forAllPlayers(onlinePlayer ->
                    new ChatMessagePacketOut(onlinePlayer, packetData.chatChannelType, stringBuilder.toString()).sendPacket());
        }
    }

    private boolean attemptCommand(TextMessage packetData) {
        if (!packetData.getClientHandler().getAuthenticatedUser().isAdmin()) return false;
        if (!packetData.text.startsWith("/")) return false;

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
        String playerName = " [" + player.getName() + "] ";
        CommandState.CommandType commandType = commandState.getCommandType();

        if (commandType == CommandState.CommandType.NOT_FOUND) {
            new ChatMessagePacketOut(player, packetData.chatChannelType, MessageText.ERROR + "Unknown Command").sendPacket();
            println(getClass(), playerName + "Unknown Command");

        } else if (commandType == CommandState.CommandType.SINGE_INCOMPLETE) {
            new ChatMessagePacketOut(player, packetData.chatChannelType, MessageText.ERROR + "[Command] -> " + commandState.getIncompleteMessage()).sendPacket();
            println(getClass(), playerName + " [Command] -> " + commandState.getIncompleteMessage());

        } else if (commandType == CommandState.CommandType.MULTIPLE_INCOMPLETE) {
            new ChatMessagePacketOut(player, packetData.chatChannelType, "[YELLOW]Suggested Alternatives:").sendPacket();
            println(getClass(), playerName + " Suggested Alternatives:");
            for (String incompleteMsg : commandState.getMultipleIncompleteMessages()) {
                new ChatMessagePacketOut(player, packetData.chatChannelType, "[YELLOW] - [Command] -> " + incompleteMsg).sendPacket();
                println(getClass(), playerName + " - [Command] -> " + incompleteMsg);
            }
        }
        return true;
    }

    @AllArgsConstructor
    static class TextMessage extends PacketData {
        private final ChatChannelType chatChannelType;
        private final String text;
    }
}
