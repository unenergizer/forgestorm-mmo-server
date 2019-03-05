package com.valenguard.server.game.inventory;

import lombok.Getter;

@Getter
public class TradePacketInfoOut {

    private TradeStatusOpcode tradeOpcode;
    private int tradeUUID;
    private short tradeStarterUUID;
    private short tradeTargetUUID;
    private short confirmedPlayerUUID;
    private ItemStack itemStack;
    private byte tradeSlot;

    public TradePacketInfoOut(TradeStatusOpcode tradeStatusOpcode) {
        this.tradeOpcode = tradeStatusOpcode;
    }

    public TradePacketInfoOut(TradeStatusOpcode tradeStatusOpcode, int tradeUUID) {
        this.tradeOpcode = tradeStatusOpcode;
        this.tradeUUID = tradeUUID;
    }

    public TradePacketInfoOut(TradeStatusOpcode tradeStatusOpcode, int tradeUUID, short tradeStarterUUID, short tradeTargetUUID) {
        this.tradeOpcode = tradeStatusOpcode;
        this.tradeUUID = tradeUUID;
        this.tradeStarterUUID = tradeStarterUUID;
        this.tradeTargetUUID = tradeTargetUUID;
    }

    public TradePacketInfoOut(TradeStatusOpcode tradeStatusOpcode, int tradeUUID, short confirmedPlayerUUID) {
        this.tradeOpcode = tradeStatusOpcode;
        this.tradeUUID = tradeUUID;
        this.confirmedPlayerUUID = confirmedPlayerUUID;
    }

    public TradePacketInfoOut(TradeStatusOpcode tradeStatusOpcode, int tradeUUID, byte tradeSlot) {
        this.tradeOpcode = tradeStatusOpcode;
        this.tradeUUID = tradeUUID;
        this.tradeSlot = tradeSlot;
    }

    public TradePacketInfoOut(TradeStatusOpcode tradeStatusOpcode, int tradeUUID, ItemStack itemStack) {
        this.tradeOpcode = tradeStatusOpcode;
        this.tradeUUID = tradeUUID;
        this.itemStack = itemStack;
    }
}
