package com.valenguard.server.network.shared;

import com.valenguard.server.game.entity.Player;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PacketData {
    private byte opcode;
    private Player player;
}
