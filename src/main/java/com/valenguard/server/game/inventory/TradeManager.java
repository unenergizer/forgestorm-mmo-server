package com.valenguard.server.game.inventory;

import com.valenguard.server.ValenguardMain;
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

            // Cheap way of letting the iterator below to remove the trade data from the map
            // TODO: FIX ME! OR IT WILL SAY TIMED OUT EVERY TIME WE CANCEL A TRADE
//            tradeData.tradeActive = false;
//            tradeData.timeLeft = 0;
            tradeDataMap.remove(tradeUUID);

            // TODO: Send player ItemStacks
//            List<ItemStack> startersNewItems = getNewItems(tradeData, tradeData.tradeStarter);
//            List<ItemStack> targetsNewItems = getNewItems(tradeData, tradeData.targetPlayer);

            updateInventories(tradeData.tradeStarter, tradeData.tradeStarterItems, tradeData.tradeTargetItems);
            updateInventories(tradeData.targetPlayer, tradeData.tradeTargetItems, tradeData.tradeStarterItems);

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

    private List<ItemStack> getNewItems(TradeData tradeData, Player trader) {
        List<Integer> newStartItemsIds = new ArrayList<>();
        for (ItemStack itemStack : trader.getPlayerBag().getItems()) {
            newStartItemsIds.add(itemStack.itemId);
        }

        boolean traderIsStarter = trader == tradeData.tradeStarter;
        List<Integer> traderItems = traderIsStarter ? tradeData.tradeStarterItems : tradeData.tradeTargetItems;
        List<Integer> othersItems = !traderIsStarter ? tradeData.tradeStarterItems : tradeData.tradeTargetItems;


        newStartItemsIds.removeIf(traderItems::contains);
        newStartItemsIds.addAll(othersItems);

        ItemStackManager itemStackManager = ValenguardMain.getInstance().getItemStackManager();

        List<ItemStack> newItems = new ArrayList<>();
        newStartItemsIds.forEach(itemId -> newItems.add(itemStackManager.makeItemStack(itemId, 1)));

        return newItems;
    }

    /**
     * One size fits all trade cancel.
     *
     * @param canceler          The player who canceled the trade.
     * @param tradeUUID         The trade window unique reference id.
     * @param tradeStatusOpcode The type of trade cancel.
     */
    public void tradeCanceled(Player canceler, int tradeUUID, TradeStatusOpcode tradeStatusOpcode) {
        TradeData tradeData = tradeDataMap.get(tradeUUID);
        if (tradeData == null) return;
        if (!tradeData.isTrader(canceler)) return;

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

    public void sendItem(Player player, int tradeUUID, int itemStackUUID) {
        TradeData tradeData = tradeDataMap.get(tradeUUID);

        if (tradeData.targetPlayer == player) {
            tradeData.tradeTargetItems.add(itemStackUUID);
            new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_ITEM_ADD, tradeUUID, itemStackUUID)).sendPacket();
        } else {
            tradeData.tradeStarterItems.add(itemStackUUID);
            new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_ITEM_ADD, tradeUUID, itemStackUUID)).sendPacket();
        }
    }

    public void removeItem(Player player, int tradeUUID, int itemStackUUID) {
        TradeData tradeData = tradeDataMap.get(tradeUUID);

        if (tradeData.targetPlayer == player) {
            println(getClass(), "Target List size = " + tradeData.tradeTargetItems.size());
            tradeData.tradeTargetItems.remove(tradeUUID);
            println(getClass(), "Target List size = " + tradeData.tradeTargetItems.size());
            new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_ITEM_REMOVE, tradeUUID, itemStackUUID)).sendPacket();
        } else {
            println(getClass(), "Starter List size = " + tradeData.tradeStarterItems.size());
            tradeData.tradeStarterItems.remove(tradeUUID);
            println(getClass(), "Starter List size = " + tradeData.tradeStarterItems.size());
            new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_ITEM_REMOVE, tradeUUID, itemStackUUID)).sendPacket();
        }
    }


    private void updateInventories(Player playerToUpdate, List<Integer> itemsToRemove, List<Integer> itemsToAdd) {
        ItemStackManager itemStackManager = ValenguardMain.getInstance().getItemStackManager();
        // Dump items from trade starter bag
        for (Integer itemStackUUID : itemsToRemove) {
            playerToUpdate.removeItemStack(itemStackManager.makeItemStack(itemStackUUID, 1));
        }

        // Send the trade starter the target player items
        for (Integer itemStackUUID : itemsToAdd) {
            playerToUpdate.giveItemStack(itemStackManager.makeItemStack(itemStackUUID, 1));
        }
    }

    class TradeData {
        private final Player tradeStarter;
        private final Player targetPlayer;

        private int timeLeft = MAX_TIME;

        private boolean tradeActive = false;

        private boolean tradeStarterConfirmedTrade = false;
        private boolean targetPlayerConfirmedTrade = false;

        private List<Integer> tradeStarterItems = new ArrayList<>();
        private List<Integer> tradeTargetItems = new ArrayList<>();

        TradeData(Player tradeStarter, Player targetPlayer) {
            this.tradeStarter = tradeStarter;
            this.targetPlayer = targetPlayer;
        }

        private boolean isTrader(Player player) {
            return player == tradeStarter || player == targetPlayer;
        }
    }
}
