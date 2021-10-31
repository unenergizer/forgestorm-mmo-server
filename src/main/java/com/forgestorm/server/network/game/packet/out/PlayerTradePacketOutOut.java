package com.forgestorm.server.network.game.packet.out;

import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.shared.game.world.item.ItemStack;
import com.forgestorm.server.game.world.item.trade.TradePacketInfoOut;
import com.forgestorm.shared.game.world.item.trade.TradeStatusOpcode;
import com.forgestorm.shared.network.game.Opcodes;
import com.forgestorm.shared.network.game.GameOutputStream;

import static com.forgestorm.server.util.Log.println;

public class PlayerTradePacketOutOut extends AbstractPacketOut {

    private final TradeStatusOpcode tradeOpcode;
    private final int tradeUUID;
    private final short tradeStarterUUID;
    private final short tradeTargetUUID;
    private final short confirmedPlayerUUID;
    private final byte tradeSlot;

    private int itemStackId;
    private int itemStackAmount;

    public PlayerTradePacketOutOut(final Player receiver, final TradePacketInfoOut tradePacketInfoOut) {
        super(Opcodes.PLAYER_TRADE, receiver.getClientHandler());

        this.tradeOpcode = tradePacketInfoOut.getTradeOpcode();
        this.tradeUUID = tradePacketInfoOut.getTradeUUID();
        this.tradeStarterUUID = tradePacketInfoOut.getTradeStarterUUID();
        this.tradeTargetUUID = tradePacketInfoOut.getTradeTargetUUID();
        this.confirmedPlayerUUID = tradePacketInfoOut.getConfirmedPlayerUUID();
        this.tradeSlot = tradePacketInfoOut.getTradeSlot();

        ItemStack itemStack = tradePacketInfoOut.getItemStack();
        if (itemStack == null) return;
        this.itemStackId = itemStack.getItemId();
        this.itemStackAmount = itemStack.getAmount();
    }

    @Override
    public void createPacket(GameOutputStream write) {
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