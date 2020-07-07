package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.game.GameConstants;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.shared.Opcodes;

import java.util.ArrayList;
import java.util.List;

import static com.forgestorm.server.util.Log.println;

public class ChatMessagePacketOut extends AbstractServerOutPacket {

    private static final boolean PRINT_DEBUG = false;

    private final ChatChannelType chatChannelType;
    private final String message;

    public ChatMessagePacketOut(final Player player, final ChatChannelType chatChannelType, final String message) {
        super(Opcodes.CHAT, player.getClientHandler());
        this.chatChannelType = chatChannelType;
        this.message = message;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        List<String> stringList = new ArrayList<>();
        int index = 0;
        while (index < message.length()) {
            stringList.add(message.substring(index, Math.min(index + GameConstants.MAX_CHAT_LENGTH, message.length())));
            index += GameConstants.MAX_CHAT_LENGTH;
        }

        println(getClass(), "Chat Channel: " + chatChannelType.name(), false, PRINT_DEBUG);
        println(getClass(), "Message Count: " + stringList.size(), false, PRINT_DEBUG);

        write.writeByte(ChatChannelType.getByte(chatChannelType));
        write.writeByte((byte) stringList.size());

        for (String string : stringList) {
            write.writeString(string);
            println(getClass(), "String Wrote: " + string, false, PRINT_DEBUG);
        }
    }
}
