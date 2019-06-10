package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.network.game.shared.ClientHandler;
import com.valenguard.server.network.game.shared.Opcodes;

public class InitClientPrivilegePacketOut extends AbstractServerOutPacket {

    private final boolean isAdmin;

    public InitClientPrivilegePacketOut(final ClientHandler clientHandler, boolean isAdmin) {
        super(Opcodes.INIT_CLIENT_PRIVILEGE, clientHandler);
        this.isAdmin = isAdmin;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeBoolean(isAdmin);
    }
}
