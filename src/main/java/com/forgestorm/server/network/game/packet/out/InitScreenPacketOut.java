package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.UserInterfaceType;
import com.forgestorm.server.network.game.shared.ClientHandler;
import com.forgestorm.shared.network.game.Opcodes;
import com.forgestorm.shared.network.game.GameOutputStream;

public class InitScreenPacketOut extends AbstractPacketOut {

    private final UserInterfaceType userInterfaceType;

    public InitScreenPacketOut(final ClientHandler clientHandler, UserInterfaceType userInterfaceType) {
        super(Opcodes.INIT_SCREEN, clientHandler);

        this.userInterfaceType = userInterfaceType;
    }

    @Override
    public void createPacket(GameOutputStream write) {
        write.writeByte(userInterfaceType.getScreenTypeByte());
    }
}
