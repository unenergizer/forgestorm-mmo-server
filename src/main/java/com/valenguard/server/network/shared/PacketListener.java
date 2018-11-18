package com.valenguard.server.network.shared;

public interface PacketListener <T extends PacketData> {

    PacketData decodePacket(final ClientHandler clientHandler);

    boolean sanitizePacket(final T packetData);

    void onEvent(final T packetData);

}
