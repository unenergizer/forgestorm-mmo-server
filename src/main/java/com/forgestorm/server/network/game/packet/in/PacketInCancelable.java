package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.shared.PacketListener;

import java.util.List;

public interface PacketInCancelable {

    List<Class<? extends PacketListener>> excludeCanceling();

    void onCancel(Player player);

}
