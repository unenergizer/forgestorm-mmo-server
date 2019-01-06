package com.valenguard.server.network.packet.in;

import com.valenguard.server.network.shared.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.valenguard.server.util.Log.println;

@Opcode(getOpcode = Opcodes.CLICK_ACTION)
public class ClickActionPacketIn implements PacketListener<ClickActionPacketIn.ClickActionPacket> {

    public static byte LEFT = 0x01;
    public static byte RIGHT = 0x02;

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        byte inventoryAction = clientHandler.readByte();
        return new ClickActionPacket(inventoryAction);
    }

    @Override
    public boolean sanitizePacket(ClickActionPacket packetData) {
        return packetData.CLICK_ACTION <= 0x02;
    }

    @Override
    public void onEvent(ClickActionPacket packetData) {
        // Do something :)
        if (packetData.CLICK_ACTION == LEFT) println(getClass(), "Left click action received!");
        else if (packetData.CLICK_ACTION == RIGHT) println(getClass(), "Right click action received!");
        else println(getClass(), "Some other action received??????????");
    }

    @Getter
    @AllArgsConstructor
    class ClickActionPacket extends PacketData {
        private final byte CLICK_ACTION;
    }
}
