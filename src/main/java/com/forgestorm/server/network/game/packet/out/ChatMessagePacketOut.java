package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.shared.Opcodes;
import com.forgestorm.server.util.Log;

public class ChatMessagePacketOut extends AbstractServerOutPacket {

    private static final boolean PRINT_DEBUG = true;

    private final ChatChannelType chatChannelType;
    private final String message;

    public ChatMessagePacketOut(final Player player, final ChatChannelType chatChannelType, final String message) {
        super(Opcodes.CHAT, player.getClientHandler());
        this.chatChannelType = chatChannelType;
        this.message = message;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeByte(ChatChannelType.getByte(chatChannelType));
        write.writeString(message);
        Log.println(getClass(), chatChannelType.name(), false, PRINT_DEBUG);
        Log.println(getClass(), message, false, PRINT_DEBUG);
    }
}
