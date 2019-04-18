package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.ScreenType;
import com.valenguard.server.network.game.shared.ClientHandler;
import com.valenguard.server.network.game.shared.Opcodes;

public class InitScreenPacketOut extends AbstractServerOutPacket {

    private final ScreenType screenType;

    public InitScreenPacketOut(final ClientHandler clientHandler, ScreenType screenType) {
        super(Opcodes.INIT_SCREEN, clientHandler);
        this.screenType = screenType;
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        write.writeByte(screenType.getScreenTypeByte());

        switch (screenType) {
            case LOGIN:
                break;
            case CHARACTER_SELECT:
                break;
            case GAME:
                write.writeShort(clientHandler.getPlayer().getServerEntityId());
                break;
        }
    }
}
