package com.valenguard.server.network.packet.in;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.entity.MoveDirection;
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

public class PlayerMove implements PacketListener {

    @Opcode(getOpcode = Opcodes.MOVE_REQUEST)
    public void onMoveRequest(ClientHandler clientHandler) {

        MoveDirection direction = MoveDirection.getDirection(clientHandler.readByte());

        System.out.println("REQUESTED DIRECTION : " + direction);

        Player player = PlayerManager.getInstance().getPlayer(clientHandler);
        // todo this is a terrible way to get the map name
        ValenguardMain.getInstance().getServerLoop().getUpdateMovements().addPlayer(player, direction);
    }
}
