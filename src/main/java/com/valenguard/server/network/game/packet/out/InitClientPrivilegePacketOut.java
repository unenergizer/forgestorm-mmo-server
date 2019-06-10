package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.network.game.shared.ClientHandler;
import com.valenguard.server.network.game.shared.Opcodes;

public class InitClientPrivilegePacketOut extends AbstractServerOutPacket {

    private final boolean isAdmin;
    private final boolean isModerator;

    public InitClientPrivilegePacketOut(final ClientHandler clientHandler) {
        super(Opcodes.INIT_CLIENT_PRIVILEGE, clientHandler);
        this.isAdmin = clientHandler.getAuthenticatedUser().isAdmin();
        this.isModerator = clientHandler.getAuthenticatedUser().isModerator();
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeBoolean(isAdmin);
        write.writeBoolean(isModerator);
    }
}
