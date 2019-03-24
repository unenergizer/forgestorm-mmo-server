package com.valenguard.server.network.game.shared;

import com.valenguard.server.game.world.entity.Player;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PacketData {
    private byte opcode;
    private Player player;
}
