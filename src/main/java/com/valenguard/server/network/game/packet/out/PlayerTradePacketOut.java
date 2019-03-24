package com.valenguard.server.network.game.packet.out;

import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.trade.TradePacketInfoOut;
import com.valenguard.server.network.game.shared.Opcodes;

import static com.valenguard.server.util.Log.println;

public class PlayerTradePacketOut extends AbstractServerOutPacket {

    private final TradePacketInfoOut tradePacketInfoOut;

    public PlayerTradePacketOut(final Player receiver, final TradePacketInfoOut tradePacketInfoOut) {
        super(Opcodes.PLAYER_TRADE, receiver);
        this.tradePacketInfoOut = tradePacketInfoOut;
    }

    @Override
    protected void createPacket(GameOutputStream write) {

        // Write inner opcode (TradeStatusOpcode
        write.writeByte(tradePacketInfoOut.getTradeOpcode().getTradeOpcodeByte());

        switch (tradePacketInfoOut.getTradeOpcode()) {
            case TRADE_REQUEST_INIT_SENDER:
            case TRADE_REQUEST_INIT_TARGET:
                write.writeInt(tradePacketInfoOut.getTradeUUID());
                // Write the traders Target
                if (packetReceiver.getServerEntityId() == tradePacketInfoOut.getTradeStarterUUID()) {
                    write.writeShort(tradePacketInfoOut.getTradeTargetUUID());
                } else {
                    write.writeShort(tradePacketInfoOut.getTradeStarterUUID());
                }
                break;
            case TRADE_REQUEST_TARGET_ACCEPT:
            case TRADE_REQUEST_TARGET_DECLINE:
            case TRADE_REQUEST_SERVER_TIMED_OUT:
            case TRADE_OFFER_COMPLETE:
            case TRADE_CANCELED:
                write.writeInt(tradePacketInfoOut.getTradeUUID());
                break;
            case TRADE_OFFER_CONFIRM:
            case TRADE_OFFER_UNCONFIRM:
                write.writeInt(tradePacketInfoOut.getTradeUUID());
                write.writeShort(tradePacketInfoOut.getConfirmedPlayerUUID());
                break;
            case TRADE_ITEM_ADD:
                write.writeInt(tradePacketInfoOut.getTradeUUID());
                write.writeInt(tradePacketInfoOut.getItemStack().getItemId());
                // TODO : send other ItemStack information
                break;
            case TRADE_ITEM_REMOVE:
                write.writeInt(tradePacketInfoOut.getTradeUUID());
                write.writeByte(tradePacketInfoOut.getTradeSlot());
                break;
            default:
                println(getClass(), "Sent unmodified trade status: " + tradePacketInfoOut.getTradeOpcode(), true, true);
                break;
        }
    }
}