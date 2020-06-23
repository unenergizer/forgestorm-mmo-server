package com.forgestorm.server.network.game.shared;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PacketData {
    private byte opcode;
    private ClientHandler clientHandler;
}
