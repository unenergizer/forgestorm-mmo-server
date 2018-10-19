package com.valenguard.server.network.packet.in;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.entity.Player;
import com.valenguard.server.entity.PlayerManager;
import com.valenguard.server.network.shared.ClientHandler;
import com.valenguard.server.network.shared.Opcode;
import com.valenguard.server.network.shared.Opcodes;
import com.valenguard.server.network.shared.PacketListener;

/********************************************************
 * Valenguard MMO Client and Valenguard MMO Server Info
 *
 * Owned by Robert A Brown & Joseph Rugh
 * Created by Robert A Brown & Joseph Rugh
 *
 * Project Title: valenguard-client
 * Original File Date: 1/8/2018 @ 5:28 PM
 * ______________________________________________________
 *
 * Copyright © 2017 Valenguard.com. All Rights Reserved.
 *
 * No part of this project and/or code and/or source code 
 * and/or source may be reproduced, distributed, or 
 * transmitted in any form or by any means, including 
 * photocopying, recording, or other electronic or 
 * mechanical methods, without the prior written 
 * permission of the owner.
 *******************************************************/

public class MoveRequest implements PacketListener {

    //@Opcode(getOpcode = Opcodes.MOVE_REQUEST)
    public void onMoveRequest(ClientHandler clientHandler) {
        byte moveData = clientHandler.readByte();

        String move = "MoveDirection: " + Byte.toString(moveData) + " = ";
        int x = 0;
        int y = 0;

        switch (moveData) {
            case (byte) 0x01:
                move = move + "North";
                x = 0;
                y = 1;
                break;
            case (byte) 0x02:
                move = move + "South";
                x = 0;
                y = -1;
                break;
            case (byte) 0x03:
                move = move + "West";
                x = -1;
                y = 0;
                break;
            case (byte) 0x04:
                move = move + "East";
                x = 1;
                y = 0;
                break;
        }

        System.out.println(move);

        Player player = PlayerManager.getInstance().getPlayer(clientHandler);
        ValenguardMain.getInstance().getServerLoop().getUpdateMovements().addPlayer(player, player.getMapData().getMapName(), x, y);
    }
}
