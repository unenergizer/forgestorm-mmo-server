package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.game.world.entity.AiEntity;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.inventory.BankActions;
import com.valenguard.server.game.world.maps.GameMap;
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
        switch (packetData.bankAction) {
            case PLAYER_REQUEST_OPEN:

                Player player = packetData.getClientHandler().getPlayer();
                Location playerLocation = player.getFutureMapLocation();
                Location northLocation = new Location(playerLocation).add((short) 0, (short) 1);
                Location eastLocation = new Location(playerLocation).add((short) 1, (short) 0);
                Location southLocation = new Location(playerLocation).add((short) 0, (short) -1);
                Location westLocation = new Location(playerLocation).add((short) -1, (short) 0);

                GameMap gameMap = player.getGameMap();

                if (gameMap.locationHasBankAccess(northLocation)     ||
                        gameMap.locationHasBankAccess(eastLocation)  ||
                        gameMap.locationHasBankAccess(southLocation) ||
                        gameMap.locationHasBankAccess(westLocation)) {

                    player.setBankOpen(true);
                    new BankManagePacketOut(player, BankActions.SERVER_OPEN).sendPacket();

                } else {

                    for (AiEntity aiEntity : gameMap.getAiEntityController().getEntities()) {
                        if (!aiEntity.isBankKeeper()) continue;
                        if (playerLocation.isWithinDistance(aiEntity.getFutureMapLocation(), (short) 1)) {

                            player.setBankOpen(true);
                            new BankManagePacketOut(player, BankActions.SERVER_OPEN).sendPacket();
                            break;
                        }
                    }
                }

                break;
            case PLAYER_REQUEST_CLOSE:
                packetData.getClientHandler().getPlayer().setBankOpen(false);
                break;
        }
    }

    @AllArgsConstructor
    class BankManagePacket extends PacketData {
        private BankActions bankAction;
    }
}