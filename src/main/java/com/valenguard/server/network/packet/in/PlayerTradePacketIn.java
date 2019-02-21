package com.valenguard.server.network.packet.in;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.inventory.TradeManager;
import com.valenguard.server.game.inventory.TradeStatus;
import com.valenguard.server.network.shared.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.valenguard.server.util.Log.println;

@Opcode(getOpcode = Opcodes.PLAYER_TRADE)
public class PlayerTradePacketIn implements PacketListener<PlayerTradePacketIn.TradePacketIn> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {

        final TradeStatus tradeStatus = TradeStatus.getTradeStatusOpcode(clientHandler.readByte());
        int tradeUUID = 0;
        short entityUUID = 0;

        switch (tradeStatus) {
            case TRADE_REQUEST_PLAYER_TARGET:
                entityUUID = clientHandler.readShort();
                break;
            case TRADE_REQUEST_ACCEPT:
            case TRADE_REQUEST_DECLINE:
            case TRADE_OFFER_ACCEPT:
            case TRADE_OFFER_DECLINE:
                tradeUUID = clientHandler.readInt();
                break;
            default:
                println(getClass(), "Decode unused trade status: " + tradeStatus, true, true);
                break;
        }

        return new TradePacketIn(tradeStatus, tradeUUID, entityUUID);
    }

    @Override
    public boolean sanitizePacket(TradePacketIn packetData) {
        return true;
//
//        // TODO: Check both players are on same GameMap
//        // TODO: Check distance between players
//        // TODO: Make sure target or starter players are not already in a trade
//        // TODO: Make sure they exist in the map...
//
//
//
//        ValenguardMain valenguardMain = ValenguardMain.getInstance();
//        Player tradeStarter = packetData.getPlayer();
//        GameMap gameMap = packetData.getPlayer().getGameMap();
//        Player targetPlayer = gameMap.findPlayer(packetData.getENTITY_UUID());
//
//        // Check to make sure player is on same map and within X distance
//        if (targetPlayer == null) return false;
//        if (valenguardMain.getTradeManager().isTradeInProgress(tradeStarter)) return false;
//        if (valenguardMain.getTradeManager().isTradeInProgress(tradeStarter)) {
//            // Send trade starter a notification that the target player is already in a trade
//            new ChatMessagePacketOut(targetPlayer, "[Server] " + targetPlayer.getName() + " is in another trade.").sendPacket();
//            return false;
//        }
//        if (!targetPlayer.getCurrentMapLocation().isWithinDistance(packetData.getPlayer().getCurrentMapLocation(), 5)) {
//            new ChatMessagePacketOut(targetPlayer, "[Server] You must be closer to trade.").sendPacket();
//            return false;
//        }
//        return true;
    }

    @Override
    public void onEvent(TradePacketIn packetData) {
        TradeManager tradeManager = ValenguardMain.getInstance().getTradeManager();

        switch (packetData.tradeStatus) {
            case TRADE_REQUEST_PLAYER_TARGET:
                tradeManager.requestTrade(packetData.getPlayer(), packetData.getPlayer().getGameMap().findPlayer(packetData.entityUUID));
                break;
            case TRADE_REQUEST_ACCEPT:
                tradeManager.targetAcceptedTrade(packetData.getPlayer(), packetData.tradeUUID);
                break;
            case TRADE_REQUEST_DECLINE:
                tradeManager.tradeCanceled(packetData.getPlayer(), packetData.tradeUUID, packetData.tradeStatus);
                break;
            case TRADE_OFFER_DECLINE:
                tradeManager.tradeCanceled(packetData.getPlayer(), packetData.tradeUUID, packetData.tradeStatus);
                break;
            case TRADE_OFFER_ACCEPT:
                tradeManager.tradeAccepted(packetData.tradeUUID);
                break;
            default:
                println(getClass(), "onEvent unused trade status: " + packetData.tradeStatus, true, true);
                break;
        }
    }

    @Getter
    @AllArgsConstructor
    class TradePacketIn extends PacketData {
        final TradeStatus tradeStatus;
        final int tradeUUID;
        final short entityUUID;
    }
}
