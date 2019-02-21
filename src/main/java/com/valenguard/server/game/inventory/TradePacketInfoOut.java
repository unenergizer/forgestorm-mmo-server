package com.valenguard.server.game.inventory;

import lombok.Getter;

@Getter
public class TradePacketInfoOut {

    private TradeStatus tradeOpcode;
    private int uuid;

    public TradePacketInfoOut(TradeStatus tradeStatus) {
        this.tradeOpcode = tradeStatus;
    }

    public TradePacketInfoOut(TradeStatus tradeStatus, int uuid) {
        this.tradeOpcode = tradeStatus;
        this.uuid = uuid;
    }
}
