package com.valenguard.server.game.inventory;

import lombok.Getter;

@Getter
public class TradePacketInfoOut {

    private TradeStatusOpcode tradeOpcode;
    private int tradeUUID;
    private short playerUUID;

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
}
