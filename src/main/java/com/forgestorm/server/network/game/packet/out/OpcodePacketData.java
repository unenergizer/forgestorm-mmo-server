package com.forgestorm.server.network.game.packet.out;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
class OpcodePacketData {
    private boolean initialized = false;
    private byte opcode;
    private int numberOfRepeats;
    private List<byte[]> buffers = new ArrayList<>();
}
