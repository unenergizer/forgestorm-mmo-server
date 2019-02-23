package com.valenguard.server.game.inventory;

import lombok.Getter;

@Getter
public class TradePacketInfoOut {

    private TradeStatusOpcode tradeOpcode;
    private int tradeUUID;
    private short playerUUID;
    private ItemStack itemStack;
    private byte tradeSlot;

    public TradePacketInfoOut(TradeStatusOpcode tradeStatusOpcode) {
        this.tradeOpcode = tradeStatusOpcode;
    }

    public TradePacketInfoOut(TradeStatusOpcode tradeStatusOpcode, int tradeUUID) {
        this.tradeOpcode = tradeStatusOpcode;
        this.tradeUUID = tradeUUID;
    }

    public TradePacketInfoOut(TradeStatusOpcode tradeStatusOpcode, int tradeUUID, short playerUUID) {
        this.tradeOpcode = tradeStatusOpcode;
        this.tradeUUID = tradeUUID;
        this.playerUUID = playerUUID;
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
