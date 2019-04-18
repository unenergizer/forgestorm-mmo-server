package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.Server;
import com.valenguard.server.command.CommandSource;
import com.valenguard.server.command.CommandState;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.packet.out.ChatMessagePacketOut;
import com.valenguard.server.network.game.shared.*;
import lombok.AllArgsConstructor;

import static com.valenguard.server.util.Log.println;

@Opcode(getOpcode = Opcodes.CHAT)
public class ChatMessagePacketIn implements PacketListener<ChatMessagePacketIn.TextMessage> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        return new TextMessage(clientHandler.readString());
    }

    @Override
    public boolean sanitizePacket(TextMessage packetData) {
        String text = packetData.text;

        if (text == null) return false;
        text = text.trim();

        if (text.isEmpty()) return false;
        return !text.contains("\n") && !text.contains("\r") && !text.contains("\t");
    }

    @Override
    public void onEvent(TextMessage packetData) {

        if (!attemptCommand(packetData)) {
            // TODO : Use StringBuilder
            Server.getInstance().getGameManager().forAllPlayers(onlinePlayer ->
                    new ChatMessagePacketOut(onlinePlayer, packetData.getClientHandler().getPlayer().getName() + ": " + packetData.text).sendPacket());
        }
    }

    private boolean attemptCommand(TextMessage packetData) {
        if (!packetData.getClientHandler().isAdmin()) return false;
        if (!packetData.text.startsWith("/")) return false;

        String[] content = packetData.text.split("\\s+");

        // Strip the "/" off
        content[0] = content[0].replace("/", "");

        CommandState commandState;
        if (content.length == 1) {
            commandState = Server.getInstance().getCommandManager().getCommandProcessor().publish(new CommandSource(packetData.getClientHandler().getPlayer()), content[0], new String[0]);
        } else {
            String[] args = new String[content.length - 1];
            System.arraycopy(content, 1, args, 0, content.length - 1);
            commandState = Server.getInstance().getCommandManager().getCommandProcessor().publish(new CommandSource(packetData.getClientHandler().getPlayer()), content[0], args);
        }

        Player player = packetData.getClientHandler().getPlayer();
        String playerName = " [" + player.getName() + "] ";
        CommandState.CommandType commandType = commandState.getCommandType();

        if (commandType == CommandState.CommandType.NOT_FOUND) {
            new ChatMessagePacketOut(player, "Unknown Command").sendPacket();
            println(getClass(), playerName + "Unknown Command");

        } else if (commandType == CommandState.CommandType.SINGE_INCOMPLETE) {
            new ChatMessagePacketOut(player, "[Command] -> " + commandState.getIncompleteMessage()).sendPacket();
            println(getClass(), playerName + " [Command] -> " + commandState.getIncompleteMessage());

        } else if (commandType == CommandState.CommandType.MULTIPLE_INCOMPLETE) {
            new ChatMessagePacketOut(player, "Suggested Alternatives:").sendPacket();
            println(getClass(), playerName + " Suggested Alternatives:");
            for (String incompleteMsg : commandState.getMultipleIncompleteMessages()) {
                new ChatMessagePacketOut(player, " - [Command] -> " + incompleteMsg).sendPacket();
                println(getClass(), playerName + " - [Command] -> " + incompleteMsg);
            }
        }

        return true;
    }

    @AllArgsConstructor
    class TextMessage extends PacketData {
        private String text;
    }
}
