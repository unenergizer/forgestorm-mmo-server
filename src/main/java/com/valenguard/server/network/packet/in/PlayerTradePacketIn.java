package com.valenguard.server.network.packet.in;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.inventory.TradeManager;
import com.valenguard.server.game.inventory.TradeStatusOpcode;
import com.valenguard.server.network.shared.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.valenguard.server.util.Log.println;

@Opcode(getOpcode = Opcodes.PLAYER_TRADE)
public class PlayerTradePacketIn implements PacketListener<PlayerTradePacketIn.TradePacketIn> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {

        final TradeStatusOpcode tradeStatusOpcode = TradeStatusOpcode.getTradeStatusOpcode(clientHandler.readByte());
        int tradeUUID = 0;
        short entityUUID = 0;
        byte slotId = 0;

        switch (tradeStatusOpcode) {
            case TRADE_REQUEST_INIT_TARGET:
                entityUUID = clientHandler.readShort();
                break;
            case TRADE_REQUEST_TARGET_ACCEPT:
            case TRADE_REQUEST_TARGET_DECLINE:
            case TRADE_OFFER_CONFIRM:
            case TRADE_OFFER_UNCONFIRM:
            case TRADE_OFFER_COMPLETE:
            case TRADE_CANCELED:
                tradeUUID = clientHandler.readInt();
                break;
            case TRADE_ITEM_ADD:
            case TRADE_ITEM_REMOVE:
                tradeUUID = clientHandler.readInt();
                slotId = clientHandler.readByte();
                // TODO: Send item add/remove info
                break;
            default:
                println(getClass(), "Decode unused trade status: " + tradeStatusOpcode, true, true);
                break;
        }

        return new TradePacketIn(tradeStatusOpcode, tradeUUID, entityUUID, slotId);
    }

    @Override
    public boolean sanitizePacket(TradePacketIn packetData) {
        // TODO: Check both players are on same GameMap
        // TODO: Check distance between players
        // TODO: Make sure target or starter players are not already in a trade
        // TODO: Make sure they exist in the map...
        return true;
    }

    @Override
    public void onEvent(TradePacketIn packetData) {
        TradeManager tradeManager = ValenguardMain.getInstance().getTradeManager();

        switch (packetData.tradeStatusOpcode) {

            // Stage 1: Init trade
            case TRADE_REQUEST_INIT_TARGET:
                tradeManager.requestTradeInitialized(packetData.getPlayer(), packetData.getPlayer().getGameMap().getPlayerController().findPlayer(packetData.entityUUID));
                break;

            // Stage 2: Wait for TargetPlayer response or time out
            case TRADE_REQUEST_TARGET_ACCEPT:
                tradeManager.targetPlayerAcceptedTradeRequest(packetData.getPlayer(), packetData.tradeUUID);
                break;
            case TRADE_REQUEST_TARGET_DECLINE:
                tradeManager.tradeCanceled(packetData.getPlayer(), packetData.tradeUUID, packetData.tradeStatusOpcode);
                break;

            // Stage 3: Trade started -> adding/removing items from trade window
            case TRADE_ITEM_ADD:
                tradeManager.sendItem(packetData.getPlayer(), packetData.tradeUUID, packetData.getItemSlot());
                break;

            case TRADE_ITEM_REMOVE:
                tradeManager.removeItem(packetData.getPlayer(), packetData.tradeUUID, packetData.getItemSlot());
                break;

            // Stage 4: First Trade Confirm (items are in window, do trade or cancel)
            case TRADE_OFFER_CONFIRM:
                tradeManager.playerConfirmedTrade(packetData.getPlayer(), packetData.tradeUUID);
                break;
            case TRADE_OFFER_UNCONFIRM:
                tradeManager.playerUnconfirmedTrade(packetData.getPlayer(), packetData.tradeUUID);
                break;


            // Stage 5: Final trade confirm
            // case TRADE_OFFER_COMPLETE: -> ONLY SENT TO CLIENT

            // Generic trade cancel
            case TRADE_CANCELED:
                tradeManager.tradeCanceled(packetData.getPlayer(), packetData.tradeUUID, packetData.tradeStatusOpcode);
                break;
            default:
                println(getClass(), "onEvent unused trade status: " + packetData.tradeStatusOpcode, true, true);
                break;
        }
    }

    @Getter
    @AllArgsConstructor
    class TradePacketIn extends PacketData {
        final TradeStatusOpcode tradeStatusOpcode;
        final int tradeUUID;
        final short entityUUID;
        final byte itemSlot;
    }
}
