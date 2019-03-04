package com.valenguard.server.network.packet.out;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.entity.Player;
import lombok.Getter;

public abstract class ServerAbstractOutPacket {

    /**
     * Opcode to send with the out-going packet.
     */
    @Getter
    private final byte opcode;

    /**
     * The packetReceiver who will receive the packet.
     */
    protected final Player packetReceiver;

    ServerAbstractOutPacket(byte opcode, Player packetReceiver) {
        this.opcode = opcode;
        this.packetReceiver = packetReceiver;
    }

    /**
     * Sends the packet to the packetReceiver.
     */
    public void sendPacket() {
        ValenguardMain.getInstance().getOutStreamManager().addServerOutPacket(this);
    }

    /**
     * Creates the packet.
     */
    protected abstract void createPacket(ValenguardOutputStream write);
}
