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

        write.writeByte(tradePacketInfoOut.getTradeOpcode().getTradeOpcodeByte());

        switch (tradePacketInfoOut.getTradeOpcode()) {
            case TRADE_REQUEST_PLAYER_SENDER:
                writeUUIDOut(write, tradePacketInfoOut);
                break;
            case TRADE_REQUEST_PLAYER_TARGET:
                writeRequestOut(write, tradePacketInfoOut);
                break;
            case TRADE_REQUEST_ACCEPT:
                break;
            case TRADE_REQUEST_DECLINE:
                break;
            case TRADE_REQUEST_TIMED_OUT:
                break;
            case TRADE_ITEM_ADD:
                break;
            case TRADE_ITEM_REMOVE:
                break;
            case TRADE_OFFER_ACCEPT:
                break;
            case TRADE_OFFER_DECLINE:
                break;
            default:
                println(getClass(), "Create unused trade status: " + tradePacketInfoOut.getTradeOpcode(), true, true);
                break;
        }
    }

    private void writeUUIDOut(ValenguardOutputStream write, TradePacketInfoOut tradePacketInfoOut) {
        write.writeInt(tradePacketInfoOut.getUuid());
    }

    private void writeRequestOut(ValenguardOutputStream write, TradePacketInfoOut tradePacketInfoOut) {
        write.writeInt(tradePacketInfoOut.getUuid());
    }
}