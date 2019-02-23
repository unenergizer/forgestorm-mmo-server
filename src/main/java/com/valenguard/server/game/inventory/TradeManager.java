package com.valenguard.server.game.inventory;

import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.packet.out.ChatMessagePacketOut;
import com.valenguard.server.network.packet.out.PlayerTradePacketOut;

import java.util.*;

import static com.valenguard.server.util.Log.println;

public class TradeManager {

    private static final int MAX_TIME = 20;
    private final Map<Integer, TradeData> tradeDataMap = new HashMap<>();

    /**
     * Stage 1: TradeStarter {@link Player} sends a request to TargetPlayer to initial a trade window.
     *
     * @param tradeStarter The request sender {@link Player}
     * @param targetPlayer The target {@link Player}
     */
    public void requestTradeInitialized(Player tradeStarter, Player targetPlayer) {
        if (isTradeInProgress(tradeStarter)) return;
        final int UUID = generateTradeId(tradeStarter, targetPlayer);

        tradeDataMap.put(UUID, new TradeData(tradeStarter, targetPlayer));

        new ChatMessagePacketOut(tradeStarter, "[Server] Trade request received. Waiting on " + targetPlayer.getName() + "...").sendPacket();
        new ChatMessagePacketOut(targetPlayer, "[Server] Trade request received from " + tradeStarter.getName() + ".").sendPacket();
        new PlayerTradePacketOut(tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_REQUEST_INIT_SENDER, UUID)).sendPacket();
        new PlayerTradePacketOut(targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_REQUEST_INIT_TARGET, UUID)).sendPacket();
    }

    /**
     * The TargetPlayer has accepted the initial trade request. Now we tell them to open the trade window.
     *
     * @param targetPlayer The targetPlayer who accepted the trade.
     * @param tradeUUID    The trade window unique reference id.
     */
    public void targetPlayerAcceptedTradeRequest(Player targetPlayer, int tradeUUID) {
        if (!isValidTrade(targetPlayer, tradeUUID)) return;
        TradeData tradeData = tradeDataMap.get(tradeUUID);

        if (tradeData == null) return;
        if (tradeData.targetPlayer != targetPlayer) return;

        tradeData.tradeActive = true;

        new ChatMessagePacketOut(tradeData.tradeStarter, "[Server] Trade opened with " + tradeData.targetPlayer.getName() + ".").sendPacket();
        new ChatMessagePacketOut(tradeData.targetPlayer, "[Server] Trade opened with " + tradeData.tradeStarter.getName() + ".").sendPacket();
        new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_REQUEST_TARGET_ACCEPT)).sendPacket();
        new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_REQUEST_TARGET_ACCEPT)).sendPacket();
    }

    public void playerConfirmedTrade(Player confirmedPlayer, int tradeUUID, TradeStatusOpcode tradeStatusOpcode) {
        if (!isValidTrade(confirmedPlayer, tradeUUID)) return;
        TradeData tradeData = tradeDataMap.get(tradeUUID);

        // TODO: Set boolean on who confirmed trade. Don't do trade until both booleans are set by both players

        if (confirmedPlayer == tradeData.targetPlayer) {
            tradeData.targetPlayerConfirmedTrade = true;
        } else {
            tradeData.tradeStarterConfirmedTrade = true;
        }

        if (tradeData.tradeStarterConfirmedTrade && tradeData.targetPlayerConfirmedTrade) {
            /*
             * TRADE COMPLETED / FINALIZED
             * TRADE ITEMS CONFIRMED FOR BOTH PLAYERS
             */
            new ChatMessagePacketOut(tradeData.tradeStarter, "[Server] Trade offer confirmation accepted!").sendPacket();
            new ChatMessagePacketOut(tradeData.targetPlayer, "[Server] Trade offer confirmation accepted!").sendPacket();
            new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_OFFER_COMPLETE)).sendPacket();
            new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_OFFER_COMPLETE)).sendPacket();

            // Trade finished, clear data
            tradeDataMap.remove(tradeUUID);

            // Update player inventories
            List<ItemStack> starterGiveItems = generateGiveItems(tradeData.tradeStarter, tradeData.tradeStarterItems);
            List<ItemStack> targetGiveItems = generateGiveItems(tradeData.targetPlayer, tradeData.tradeTargetItems);

            updateInventories(tradeData.tradeStarter, tradeData.tradeStarterItems, targetGiveItems);
            updateInventories(tradeData.targetPlayer, tradeData.tradeTargetItems, starterGiveItems);

        } else {
            /*
             * TRADE ITEMS CONFIRMED FOR ONE PLAYER
             */
            new ChatMessagePacketOut(tradeData.tradeStarter, "[Server] " + confirmedPlayer.getName() + " has confirmed the trade!").sendPacket();
            new ChatMessagePacketOut(tradeData.targetPlayer, "[Server] " + confirmedPlayer.getName() + " has confirmed the trade!").sendPacket();
            new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_OFFER_CONFIRM, tradeUUID, confirmedPlayer.getServerEntityId())).sendPacket();
            new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_OFFER_CONFIRM, tradeUUID, confirmedPlayer.getServerEntityId())).sendPacket();
        }
    }

    /**
     * One size fits all trade cancel.
     *
     * @param canceler          The player who canceled the trade.
     * @param tradeUUID         The trade window unique reference id.
     * @param tradeStatusOpcode The type of trade cancel.
     */
    public void tradeCanceled(Player canceler, int tradeUUID, TradeStatusOpcode tradeStatusOpcode) {
        if (!isValidTrade(canceler, tradeUUID)) return;
        TradeData tradeData = tradeDataMap.get(tradeUUID);

        if (tradeStatusOpcode == TradeStatusOpcode.TRADE_REQUEST_TARGET_DECLINE) {
            new ChatMessagePacketOut(tradeData.targetPlayer, "[Server] Trade request declined.").sendPacket();
            new ChatMessagePacketOut(tradeData.tradeStarter, "[Server] Trade request declined.").sendPacket();
            new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_REQUEST_TARGET_DECLINE)).sendPacket();
            new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_REQUEST_TARGET_DECLINE)).sendPacket();
        } else if (tradeStatusOpcode == TradeStatusOpcode.TRADE_CANCELED) {
            new ChatMessagePacketOut(tradeData.targetPlayer, "[Server] Trade offer canceled.").sendPacket();
            new ChatMessagePacketOut(tradeData.tradeStarter, "[Server] Trade offer canceled.").sendPacket();
            new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_CANCELED)).sendPacket();
            new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_CANCELED)).sendPacket();
        }

        tradeData.tradeActive = false;
        tradeData.timeLeft = 0;
    }

    private int generateTradeId(Player traderStarter, Player targetPlayer) {
        int tradeUUID = traderStarter.getServerEntityId();
        tradeUUID <<= 16;
        tradeUUID |= targetPlayer.getServerEntityId();
        return tradeUUID;
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

                    // Notify the trade starter that the target tradeStarter didn't respond in time.
                    new ChatMessagePacketOut(tradeData.tradeStarter, "[Server] Trade canceled. Trade timed out.").sendPacket();
                    new ChatMessagePacketOut(tradeData.targetPlayer, "[Server] Trade canceled. Trade timed out.").sendPacket();
                    new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_REQUEST_SERVER_TIMED_OUT)).sendPacket();
                    new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_REQUEST_SERVER_TIMED_OUT)).sendPacket();

                    iterator.remove();

                    println(getClass(), "Entries: " + tradeDataMap.size());
                }
            }
        }
    }

    public void sendItem(Player player, int tradeUUID, byte itemSlot) {
        if (!isValidTrade(player, tradeUUID)) return;
        if (!slotInsideWindow(itemSlot)) return;

        TradeData tradeData = tradeDataMap.get(tradeUUID);

        ItemStack itemStack = player.getPlayerBag().getItems()[itemSlot];
        if (itemStack == null) return;

        if (tradeData.targetPlayer == player) {
            //TODO => check if full?
            tradeData.addItem(tradeData.tradeTargetItems, itemSlot);
            new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_ITEM_ADD, tradeUUID, itemStack)).sendPacket();
        } else {
            tradeData.addItem(tradeData.tradeStarterItems, itemSlot);
            new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_ITEM_ADD, tradeUUID, itemStack)).sendPacket();
        }
    }

    public void removeItem(Player player, int tradeUUID, byte itemSlot) {
        if (!isValidTrade(player, tradeUUID)) return;
        if (!slotInsideWindow(itemSlot)) return;

        TradeData tradeData = tradeDataMap.get(tradeUUID);

        ItemStack itemStack = player.getPlayerBag().getItems()[itemSlot];
        if (itemStack == null) return;

        if (tradeData.targetPlayer == player) {
            if (tradeData.tradeTargetItems[itemSlot] != null) return;
            tradeData.tradeTargetItems[itemSlot] = null;
            new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_ITEM_REMOVE, tradeUUID, itemSlot)).sendPacket();
        } else {
            if (tradeData.tradeStarterItems[itemSlot] != null) return;
            tradeData.tradeStarterItems[itemSlot] = null;
            new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_ITEM_REMOVE, tradeUUID, itemSlot)).sendPacket();
        }
    }

    private boolean slotInsideWindow(byte itemSlot) {
        return itemSlot >= 0 && itemSlot < 5 * 9;
    }

    private boolean isValidTrade(Player trader, int tradeUUID) {
        if (!tradeDataMap.containsKey(tradeUUID)) return false;
        return tradeDataMap.get(tradeUUID).isTrader(trader);
    }

    private List<ItemStack> generateGiveItems(Player trader, Byte[] tradeItems) {
        List<ItemStack> giveItems = new ArrayList<>();
        ItemStack[] bagItems = trader.getPlayerBag().getItems();
        for (Byte itemSlot : tradeItems) {
            if (itemSlot == null) continue;
            ItemStack itemStack = bagItems[itemSlot];
            giveItems.add(itemStack);
        }
        return giveItems;
    }

    private void updateInventories(Player playerToUpdate, Byte[] itemsToRemove, List<ItemStack> itemsToAdd) {

        // Dump items from trade starter bag
        for (Byte slotIndex : itemsToRemove) {
            if (slotIndex != null) {
                playerToUpdate.removeItemStack(slotIndex);
            }
        }

        // Send the trade starter the target player items
        itemsToAdd.forEach(playerToUpdate::giveItemStack);
    }

    class TradeData {
        private final Player tradeStarter;
        private final Player targetPlayer;

        private int timeLeft = MAX_TIME;

        private boolean tradeActive = false;

        private boolean tradeStarterConfirmedTrade = false;
        private boolean targetPlayerConfirmedTrade = false;

        private Byte[] tradeStarterItems = new Byte[5 * 9];
        private Byte[] tradeTargetItems = new Byte[5 * 9];

        TradeData(Player tradeStarter, Player targetPlayer) {
            this.tradeStarter = tradeStarter;
            this.targetPlayer = targetPlayer;
        }

        private boolean isTrader(Player player) {
            return player == tradeStarter || player == targetPlayer;
        }

        private void addItem(Byte[] tradeItems, byte bagSlot) {
            for (byte i = 0; i < tradeItems.length; i++) {
                if (tradeItems[i] == null) {
                    tradeItems[i] = bagSlot;
                    return;
                }
            }
        }
    }
}
