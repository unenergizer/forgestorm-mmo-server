package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.database.AuthenticatedUser;
import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.shared.Opcodes;

import static com.forgestorm.server.util.Log.println;

public class ChatMessagePacketOut extends AbstractServerOutPacket {

    private static final boolean PRINT_DEBUG = false;

    private final Player player;
    private final ChatChannelType chatChannelType;
    private final String message;

    public ChatMessagePacketOut(final Player player, final ChatChannelType chatChannelType, final String message) {
        super(Opcodes.CHAT, player.getClientHandler());
        this.player = player;
        this.chatChannelType = chatChannelType;
        this.message = message;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        // Don't send staff chat messages to clients that shouldn't read them
        if (chatChannelType == ChatChannelType.STAFF) {
            AuthenticatedUser authenticatedUser = player.getClientHandler().getAuthenticatedUser();
            if (!authenticatedUser.isAdmin() || !authenticatedUser.isModerator()) return;
        }
        write.writeByte(ChatChannelType.getByte(chatChannelType));
        write.writeString(message);
        println(getClass(), chatChannelType.name(), false, PRINT_DEBUG);
        println(getClass(), message, false, PRINT_DEBUG);
    }
}
