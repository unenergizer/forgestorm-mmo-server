package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.world.entity.Entity;
import com.forgestorm.server.game.world.entity.EntityType;
import com.forgestorm.server.network.game.shared.ClientHandler;
import lombok.Getter;

public abstract class AbstractServerOutPacket {

    /**
     * Opcode to send with the out-going packet.
     */
    @Getter
    private final byte opcode;

    final ClientHandler clientHandler;

    AbstractServerOutPacket(byte opcode, ClientHandler clientHandler) {
        this.opcode = opcode;
        this.clientHandler = clientHandler;
    }

    /**
     * Sends the packet to the packetReceiver.
     */
    public void sendPacket() {
        ServerMain.getInstance().getNetworkManager().getOutStreamManager().addServerOutPacket(this);
    }

    /**
     * Creates the packet.
     */
    protected abstract void createPacket(GameOutputStream write);

    /**
     *
     */
    EntityType detectEntityType(Entity entity) {
        if (clientHandler.getPlayer().equals(entity)) return EntityType.CLIENT_PLAYER;
        return entity.getEntityType();
    }
}
