package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.entity.Entity;
import com.valenguard.server.game.world.entity.EntityType;
import com.valenguard.server.network.game.shared.ClientHandler;
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
        Server.getInstance().getNetworkManager().getOutStreamManager().addServerOutPacket(this);
    }

    /**
     * Creates the packet.
     */
    protected abstract void createPacket(GameOutputStream write);

    /**
     *
     */
    EntityType getEntityType(Entity entity) {
        if (clientHandler.getPlayer().equals(entity)) return EntityType.CLIENT_PLAYER;
        return entity.getEntityType();
    }
}
