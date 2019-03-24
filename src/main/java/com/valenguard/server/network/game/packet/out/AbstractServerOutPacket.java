package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.entity.Entity;
import com.valenguard.server.game.world.entity.EntityType;
import com.valenguard.server.game.world.entity.Player;
import lombok.Getter;

public abstract class AbstractServerOutPacket {

    /**
     * Opcode to send with the out-going packet.
     */
    @Getter
    private final byte opcode;

    /**
     * The packetReceiver who will receive the packet.
     */
    protected final Player packetReceiver;

    AbstractServerOutPacket(byte opcode, Player packetReceiver) {
        this.opcode = opcode;
        this.packetReceiver = packetReceiver;
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
    protected abstract void createPacket(ValenguardOutputStream write);

    /**
     *
     */
    EntityType getEntityType(Entity entity) {
        if (packetReceiver.equals(entity)) return EntityType.CLIENT_PLAYER;
        return entity.getEntityType();
    }
}
