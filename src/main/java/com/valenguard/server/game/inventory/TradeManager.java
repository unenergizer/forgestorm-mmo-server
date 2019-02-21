package com.valenguard.server.game.inventory;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.packet.out.ChatMessagePacketOut;
import com.valenguard.server.network.packet.out.PlayerTradePacketOut;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.valenguard.server.util.Log.println;

public class TradeManager {

    private final Map<Integer, TradeData> tradeDataMap = new HashMap<>();

    public void requestTrade(Player tradeStarter, Player targetPlayer) {
        final int UUID = generateTradeId(tradeStarter, targetPlayer);
        tradeDataMap.put(UUID, new TradeData(UUID, tradeStarter, targetPlayer));
        new ChatMessagePacketOut(tradeStarter, "[Server] Trade request received. Waiting on " + targetPlayer.getName() + "...").sendPacket();
        new ChatMessagePacketOut(targetPlayer, "[Server] Trade request received from " + tradeStarter.getName() + ".").sendPacket();
        new PlayerTradePacketOut(tradeStarter, new TradePacketInfoOut(TradeStatus.TRADE_REQUEST_PLAYER_SENDER, UUID)).sendPacket();
        new PlayerTradePacketOut(targetPlayer, new TradePacketInfoOut(TradeStatus.TRADE_REQUEST_PLAYER_TARGET, UUID)).sendPacket();
    }

    private int generateTradeId(Player traderStarter, Player targetPlayer) {
        int tradeUUID = traderStarter.getServerEntityId();
        tradeUUID <<= 16;
        tradeUUID |= targetPlayer.getServerEntityId();
        return tradeUUID;
    }

    public void targetAcceptedTrade(Player acceptedPlayer, int tradeUUID) {
        TradeData tradeData = tradeDataMap.get(tradeUUID);
        if (tradeData == null) return;
        if (tradeData.targetPlayer != acceptedPlayer) return;

        tradeData.tradeActive = true;
        new ChatMessagePacketOut(tradeData.tradeStarter, "[Server] Trade opened with " + tradeData.targetPlayer.getName() + ".").sendPacket();
        new ChatMessagePacketOut(tradeData.targetPlayer, "[Server] Trade opened with " + tradeData.tradeStarter.getName() + ".").sendPacket();
        new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatus.TRADE_REQUEST_ACCEPT)).sendPacket();
        new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatus.TRADE_REQUEST_ACCEPT)).sendPacket();
    }


    public void tradeCanceled(Player canceler, int tradeUUID, TradeStatus tradeStatus) {
        TradeData tradeData = tradeDataMap.get(tradeUUID);
        if (tradeData == null) return;
        if (!tradeData.isTrader(canceler)) return;

        if (tradeStatus == TradeStatus.TRADE_REQUEST_DECLINE) {
            new ChatMessagePacketOut(tradeData.targetPlayer, "[Server] Trade request canceled.").sendPacket();
            new ChatMessagePacketOut(tradeData.tradeStarter, "[Server] Trade request canceled.").sendPacket();
            new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatus.TRADE_REQUEST_DECLINE)).sendPacket();
            new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatus.TRADE_REQUEST_DECLINE)).sendPacket();
        } else if (tradeStatus == TradeStatus.TRADE_OFFER_DECLINE) {
            new ChatMessagePacketOut(tradeData.targetPlayer, "[Server] Trade offer declined.").sendPacket();
            new ChatMessagePacketOut(tradeData.tradeStarter, "[Server] Trade offer declined.").sendPacket();
            new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatus.TRADE_OFFER_DECLINE)).sendPacket();
            new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatus.TRADE_OFFER_DECLINE)).sendPacket();
        } else {
            println(getClass(), "IMPROPER TRADE STATUS: " + tradeStatus, true, true);
        }

        tradeData.tradeActive = false;
        tradeData.timeLeft = 0;
        tradeData.tradeCanceled = true;
    }

    public boolean isTradeInProgress(Player tradeStarter) {
        for (TradeData tradeData : tradeDataMap.values()) {
            if (tradeData.tradeStarter == tradeStarter) return true;
        }
        return false;
    }

    public void tickTime(float numberOfTicksPassed) {
        if (numberOfTicksPassed % 20 == 0) {

            Iterator<TradeData> iterator = tradeDataMap.values().iterator();
            while (iterator.hasNext()) {
                TradeData tradeData = iterator.next();

                tradeData.timeLeft--;
                if (tradeData.timeLeft <= 0 && !tradeData.tradeActive) {

                    if (!tradeData.tradeCanceled) {
                        // Notify the trade starter that the target tradeStarter didn't respond in time.

                        new ChatMessagePacketOut(tradeData.tradeStarter, "[Server] Trade canceled. Trade timed out.").sendPacket();
                        new ChatMessagePacketOut(tradeData.targetPlayer, "[Server] Trade canceled. Trade timed out.").sendPacket();
                        new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatus.TRADE_REQUEST_TIMED_OUT)).sendPacket();
                        new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatus.TRADE_REQUEST_TIMED_OUT)).sendPacket();
                    }

                    iterator.remove();

                    println(getClass(), "Entries: " + tradeDataMap.size());
                }
            }
        }
    }

    public void tradeAccepted(int tradeUUID) {

        // TODO: Do ITEM SWAPS!

        TradeData tradeData = tradeDataMap.get(tradeUUID);
        new ChatMessagePacketOut(tradeData.tradeStarter, "[Server] Trade accepted!").sendPacket();
        new ChatMessagePacketOut(tradeData.targetPlayer, "[Server] Trade accepted!").sendPacket();
        new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatus.TRADE_OFFER_ACCEPT)).sendPacket();
        new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatus.TRADE_OFFER_ACCEPT)).sendPacket();
    }

    class TradeData {

        // TODO: GENERATE RANDOM UUID

        private static final int MAX_TIME = 20;

        private final int tradeUUID;
        private final Player tradeStarter;
        private final Player targetPlayer;

        private int timeLeft = MAX_TIME;

        private boolean tradeActive = false;
        private boolean tradeCanceled = false;

        TradeData(Integer tradeUUID, Player tradeStarter, Player targetPlayer) {
            this.tradeUUID = tradeUUID;
            this.tradeStarter = tradeStarter;
            this.targetPlayer = targetPlayer;
        }

        private boolean isTrader(Player player) {
            return player == tradeStarter || player == targetPlayer;
        }
    }

}
