package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.shared.PacketListener;

import java.util.List;

public interface PacketInCancelable {

    List<Class<? extends PacketListener>> excludeCanceling();

    void onCancel(Player player);

}
