package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.UserInterfaceType;
import com.valenguard.server.network.game.shared.ClientHandler;
import com.valenguard.server.network.game.shared.Opcodes;

public class InitScreenPacketOut extends AbstractServerOutPacket {

    private final UserInterfaceType userInterfaceType;

    public InitScreenPacketOut(final ClientHandler clientHandler, UserInterfaceType userInterfaceType) {
        super(Opcodes.INIT_SCREEN, clientHandler);
        this.userInterfaceType = userInterfaceType;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeByte(userInterfaceType.getScreenTypeByte());
    }
}
