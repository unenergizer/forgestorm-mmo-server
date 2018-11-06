package com.valenguard.server.network.packet.in;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.entity.MoveDirection;
import com.valenguard.server.entity.Player;
import com.valenguard.server.entity.PlayerManager;
import com.valenguard.server.network.shared.ClientHandler;
import com.valenguard.server.network.shared.Opcode;
import com.valenguard.server.network.shared.Opcodes;
import com.valenguard.server.network.shared.PacketListener;

public class PlayerMove implements PacketListener {

    @Opcode(getOpcode = Opcodes.MOVE_REQUEST)
    public void onMoveRequest(ClientHandler clientHandler) {
        MoveDirection direction = MoveDirection.getDirection(clientHandler.readByte());

        if (direction == null || direction == MoveDirection.NONE) return;

        Player player = PlayerManager.getInstance().getPlayer(clientHandler);

        ValenguardMain.getInstance().getServerLoop().getUpdateMovements().addPlayer(player, direction);
    }
}
