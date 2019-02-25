package com.valenguard.server.network.packet.in;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.shared.PacketListener;

import java.util.List;

public interface PacketInCancelable {

    List<Class<? extends PacketListener>> excludeCanceling();

    void onCancel(Player player);

}
