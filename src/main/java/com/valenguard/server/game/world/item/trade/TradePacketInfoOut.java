package com.valenguard.server.game.world.item.trade;

import com.valenguard.server.game.world.item.ItemStack;
import lombok.Getter;

@Getter
public class TradePacketInfoOut {

    private final TradeStatusOpcode tradeOpcode;
    private int tradeUUID;
    private short tradeStarterUUID;
    private short tradeTargetUUID;
    private short confirmedPlayerUUID;
    private ItemStack itemStack;
    private byte tradeSlot;

    TradePacketInfoOut(final TradeStatusOpcode tradeStatusOpcode) {
        this.tradeOpcode = tradeStatusOpcode;
    }

    TradePacketInfoOut(TradeStatusOpcode tradeStatusOpcode, int tradeUUID, short tradeStarterUUID, short tradeTargetUUID) {
        this.tradeOpcode = tradeStatusOpcode;
        this.tradeUUID = tradeUUID;
        this.tradeStarterUUID = tradeStarterUUID;
        this.tradeTargetUUID = tradeTargetUUID;
    }

    TradePacketInfoOut(TradeStatusOpcode tradeStatusOpcode, int tradeUUID, short confirmedPlayerUUID) {
        this.tradeOpcode = tradeStatusOpcode;
        this.tradeUUID = tradeUUID;
        this.confirmedPlayerUUID = confirmedPlayerUUID;
    }

    TradePacketInfoOut(int tradeUUID, byte tradeSlot) {
        this.tradeOpcode = TradeStatusOpcode.TRADE_ITEM_REMOVE;
        this.tradeUUID = tradeUUID;
        this.tradeSlot = tradeSlot;
    }

    TradePacketInfoOut(int tradeUUID, ItemStack itemStack) {
        this.tradeOpcode = TradeStatusOpcode.TRADE_ITEM_ADD;
        this.tradeUUID = tradeUUID;
        this.itemStack = itemStack;
    }
}
