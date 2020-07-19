package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.item.trade.TradePacketInfoOut;
import com.forgestorm.server.game.world.item.trade.TradeStatusOpcode;
import com.forgestorm.server.network.game.shared.Opcodes;

import static com.forgestorm.server.util.Log.println;

public class PlayerTradePacketOut extends AbstractServerOutPacket {

    private final TradeStatusOpcode tradeOpcode;
    private final int tradeUUID;
    private final short tradeStarterUUID;
    private final short tradeTargetUUID;
    private final short confirmedPlayerUUID;
    private final int itemStackId;
    private final int itemStackAmount;
    private final byte tradeSlot;

    public PlayerTradePacketOut(final Player receiver, final TradePacketInfoOut tradePacketInfoOut) {
        super(Opcodes.PLAYER_TRADE, receiver.getClientHandler());

        this.tradeOpcode = tradePacketInfoOut.getTradeOpcode();
        this.tradeUUID = tradePacketInfoOut.getTradeUUID();
        this.tradeStarterUUID = tradePacketInfoOut.getTradeStarterUUID();
        this.tradeTargetUUID = tradePacketInfoOut.getTradeTargetUUID();
        this.confirmedPlayerUUID = tradePacketInfoOut.getConfirmedPlayerUUID();
        this.itemStackId = tradePacketInfoOut.getItemStack().getItemId();
        this.itemStackAmount = tradePacketInfoOut.getItemStack().getAmount();
        this.tradeSlot = tradePacketInfoOut.getTradeSlot();
    }

    @Override
    protected void createPacket(GameOutputStream write) {
        Player packetReceiver = clientHandler.getPlayer();

        // Write inner opcode (TradeStatusOpcode
        write.writeByte(tradeOpcode.getTradeOpcodeByte());

        switch (tradeOpcode) {
            case TRADE_REQUEST_INIT_SENDER:
            case TRADE_REQUEST_INIT_TARGET:
                write.writeInt(tradeUUID);
                // Write the traders Target
                if (packetReceiver.getServerEntityId() == tradeStarterUUID) {
                    write.writeShort(tradeTargetUUID);
                } else {
                    write.writeShort(tradeStarterUUID);
                }
                break;
            case TRADE_REQUEST_TARGET_ACCEPT:
            case TRADE_REQUEST_TARGET_DECLINE:
            case TRADE_REQUEST_SERVER_TIMED_OUT:
            case TRADE_OFFER_COMPLETE:
            case TRADE_CANCELED:
                write.writeInt(tradeUUID);
                break;
            case TRADE_OFFER_CONFIRM:
            case TRADE_OFFER_UNCONFIRM:
                write.writeInt(tradeUUID);
                write.writeShort(confirmedPlayerUUID);
                break;
            case TRADE_ITEM_ADD:
                write.writeInt(tradeUUID);
                write.writeInt(itemStackId);
                write.writeInt(itemStackAmount);
                break;
            case TRADE_ITEM_REMOVE:
                write.writeInt(tradeUUID);
                write.writeByte(tradeSlot);
                break;
            default:
                println(getClass(), "Sent unmodified trade status: " + tradeOpcode, true, true);
                break;
        }
    }
}