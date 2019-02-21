package com.valenguard.server.network.packet.out;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.inventory.TradePacketInfoOut;
import com.valenguard.server.network.shared.Opcodes;

import static com.valenguard.server.util.Log.println;

public class PlayerTradePacketOut extends ServerAbstractOutPacket {

    private TradePacketInfoOut tradePacketInfoOut;

    public PlayerTradePacketOut(Player receiver, TradePacketInfoOut tradePacketInfoOut) {
        super(Opcodes.PLAYER_TRADE, receiver);
        this.tradePacketInfoOut = tradePacketInfoOut;
    }

    @Override
    protected void createPacket(ValenguardOutputStream write) {

        // Write inner opcode (TradeStatusOpcode
        write.writeByte(tradePacketInfoOut.getTradeOpcode().getTradeOpcodeByte());

        switch (tradePacketInfoOut.getTradeOpcode()) {
            case TRADE_REQUEST_INIT_SENDER:
            case TRADE_REQUEST_INIT_TARGET:
            case TRADE_REQUEST_TARGET_ACCEPT:
            case TRADE_REQUEST_TARGET_DECLINE:
            case TRADE_REQUEST_SERVER_TIMED_OUT:
            case TRADE_OFFER_COMPLETE:
            case TRADE_CANCELED:
                write.writeInt(tradePacketInfoOut.getTradeUUID());
                break;
            case TRADE_OFFER_CONFIRM:
                write.writeInt(tradePacketInfoOut.getTradeUUID());
                write.writeShort(tradePacketInfoOut.getPlayerUUID());
                break;
            default:
                println(getClass(), "Sent unmodified trade status: " + tradePacketInfoOut.getTradeOpcode(), true, true);
                break;
        }
    }
}