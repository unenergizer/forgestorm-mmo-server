package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.network.game.shared.ClientHandler;
import com.forgestorm.server.network.game.shared.Opcodes;

import java.util.List;

public class InitClientPrivilegePacketOut extends AbstractServerOutPacket {

    private final List<Byte> secondaryGroupIds;
    private final boolean isAdmin;
    private final boolean isModerator;

    public InitClientPrivilegePacketOut(final ClientHandler clientHandler) {
        super(Opcodes.INIT_CLIENT_PRIVILEGE, clientHandler);
        this.secondaryGroupIds = clientHandler.getAuthenticatedUser().getSecondaryGroupIds();
        this.isAdmin = clientHandler.getAuthenticatedUser().isAdmin();
        this.isModerator = clientHandler.getAuthenticatedUser().isModerator();
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        // Write secondary permissions
        write.writeByte((byte) secondaryGroupIds.size());
        for (Byte b : secondaryGroupIds) {
            write.writeByte(b);
        }

        // Write explicit permissions
        write.writeBoolean(isAdmin);
        write.writeBoolean(isModerator);
    }
}
