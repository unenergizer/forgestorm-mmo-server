package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.game.GameConstants;
import com.valenguard.server.game.world.entity.AiEntity;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.inventory.BankActions;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.network.game.packet.out.BankManagePacketOut;
import com.valenguard.server.network.game.shared.*;
import lombok.AllArgsConstructor;

@Opcode(getOpcode = Opcodes.BANK_MANAGEMENT)
public class BankManagePacketIn implements PacketListener<BankManagePacketIn.BankManagePacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        return new BankManagePacket(BankActions.getType(clientHandler.readByte()));
    }

    @Override
    public boolean sanitizePacket(BankManagePacket packetData) {
        return true;
    }

    @Override
    public void onEvent(BankManagePacket packetData) {
        Player player = packetData.getClientHandler().getPlayer();

        switch (packetData.bankAction) {
            case PLAYER_REQUEST_OPEN:

                Location playerLocation = player.getFutureMapLocation();

                for (AiEntity aiEntity : player.getGameMap().getAiEntityController().getEntities()) {
                    if (!aiEntity.isBankKeeper()) continue;
                    if (playerLocation.isWithinDistance(aiEntity.getFutureMapLocation(), GameConstants.MAX_BANK_DISTANCE)) {

                        player.setBankOpen(true);
                        new BankManagePacketOut(player, BankActions.SERVER_OPEN).sendPacket();
                        break;
                    }
                }

                break;
            case PLAYER_REQUEST_CLOSE:
                player.setBankOpen(false);
                break;
        }
    }

    @AllArgsConstructor
    class BankManagePacket extends PacketData {
        private BankActions bankAction;
    }
}