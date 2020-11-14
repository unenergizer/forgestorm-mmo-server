package com.forgestorm.server.game.world.item.trade;

import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.game.GameLoop;
import com.forgestorm.server.game.MessageText;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.item.ItemStack;
import com.forgestorm.server.game.world.item.inventory.InventoryConstants;
import com.forgestorm.server.game.world.item.inventory.InventorySlot;
import com.forgestorm.server.game.world.item.inventory.PlayerBag;
import com.forgestorm.server.game.world.task.AbstractTask;
import com.forgestorm.server.network.game.packet.out.ChatMessagePacketOut;
import com.forgestorm.server.network.game.packet.out.PlayerTradePacketOut;

import java.util.*;

import static com.forgestorm.server.util.Log.println;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class TradeManager implements AbstractTask {

    private static final boolean PRINT_DEBUG = false;

    /**
     * The max time to wait before the trade request times out.
     */
    private static final int MAX_TIME = 20;

    /**
     * The max trade distance allowed between two players.
     */
    private static final short MAX_TRADE_DISTANCE = 5;

    /**
     * This holds a <tradeUUID, TradeData> for a live trade.
     */
    private final Map<Integer, TradeData> tradeDataMap = new HashMap<>();

    /**
     * Stage 1:
     * TradeStarter {@link Player} sends a request to TargetPlayer to initial a trade window.
     *
     * @param tradeStarter The request sender {@link Player}
     * @param targetPlayer The target {@link Player}
     */
    public void requestTradeInitialized(Player tradeStarter, Player targetPlayer) {
        println(getClass(), "method: requestTradeInitialized(" + tradeStarter + "," + targetPlayer + ")", false, PRINT_DEBUG);
        if (isTradeInProgress(tradeStarter, targetPlayer)) {
            new ChatMessagePacketOut(tradeStarter, ChatChannelType.TRADE, MessageText.SERVER + targetPlayer.getName() + " is already trading.").sendPacket();
            return;
        }
        if (!checkMapSanity(tradeStarter, targetPlayer)) {
            new ChatMessagePacketOut(tradeStarter, ChatChannelType.TRADE, MessageText.SERVER + "You must be closer to begin a trade.").sendPacket();
            return;
        }

        final int tradeUUID = generateTradeId(tradeStarter, targetPlayer);

        tradeStarter.setTradeUUID(tradeUUID);
        tradeDataMap.put(tradeUUID, new TradeData(tradeStarter, targetPlayer));

        new ChatMessagePacketOut(tradeStarter, ChatChannelType.TRADE, MessageText.SERVER + "Trade request received. Waiting on " + targetPlayer.getName() + "...").sendPacket();
        new ChatMessagePacketOut(targetPlayer, ChatChannelType.TRADE, MessageText.SERVER + "Trade request received from " + tradeStarter.getName() + ".").sendPacket();
        new PlayerTradePacketOut(tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_REQUEST_INIT_SENDER, tradeUUID, tradeStarter.getServerEntityId(), targetPlayer.getServerEntityId())).sendPacket();
        new PlayerTradePacketOut(targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_REQUEST_INIT_TARGET, tradeUUID, tradeStarter.getServerEntityId(), targetPlayer.getServerEntityId())).sendPacket();

        println(getClass(), "method: requestTradeInitialized() -> Trade Request Sent", false, PRINT_DEBUG);
    }

    /**
     * Stage 2:
     * The TargetPlayer has accepted the initial trade request. Now we tell them to open the trade window.
     *
     * @param targetPlayer The targetPlayer who accepted the trade.
     * @param tradeUUID    The trade window unique reference id.
     */
    public void targetPlayerAcceptedTradeRequest(Player targetPlayer, int tradeUUID) {
        println(getClass(), "method: targetPlayerAcceptedTradeRequest(" + targetPlayer + ", " + tradeUUID + ")", false, PRINT_DEBUG);
        if (!isValidTrade(targetPlayer, tradeUUID)) return;
        TradeData tradeData = tradeDataMap.get(tradeUUID);

        if (tradeData == null) return;
        if (tradeData.targetPlayer != targetPlayer) return;

        tradeData.tradeActive = true;

        targetPlayer.setTradeUUID(tradeUUID);
        new ChatMessagePacketOut(tradeData.tradeStarter, ChatChannelType.TRADE, MessageText.SERVER + "Trade opened with " + tradeData.targetPlayer.getName() + ".").sendPacket();
        new ChatMessagePacketOut(tradeData.targetPlayer, ChatChannelType.TRADE, MessageText.SERVER + "Trade opened with " + tradeData.tradeStarter.getName() + ".").sendPacket();
        new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_REQUEST_TARGET_ACCEPT)).sendPacket();
        new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_REQUEST_TARGET_ACCEPT)).sendPacket();

        println(getClass(), "method: targetPlayerAcceptedTradeRequest() -> Success", false, PRINT_DEBUG);
    }

    /**
     * Stage 3:
     * Called when we need to send the target player an update about an item they added to the trade window.
     *
     * @param player    The player who added the item.
     * @param tradeUUID The unique id for this trade.
     * @param itemSlot  The slot the item was contained in.
     */
    public void sendItem(Player player, int tradeUUID, byte itemSlot) {
        println(getClass(), "method: sendItem(" + player + ", " + tradeUUID + ")", false, PRINT_DEBUG);
        if (!isValidTrade(player, tradeUUID)) return;
        if (!slotInsideWindow(itemSlot)) return;

        TradeData tradeData = tradeDataMap.get(tradeUUID);

        ItemStack itemStack = player.getPlayerBag().getInventorySlotArray()[itemSlot].getItemStack();
        if (itemStack == null) return;

        if (tradeData.targetPlayer == player) {
            tradeData.addItem(tradeData.tradeTargetItems, itemSlot);
            new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_ITEM_ADD, tradeUUID, itemStack)).sendPacket();

            println(getClass(), "method: sendItem() -> TargetPlayer Add Item", false, PRINT_DEBUG);
        } else {
            tradeData.addItem(tradeData.tradeStarterItems, itemSlot);
            new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_ITEM_ADD, tradeUUID, itemStack)).sendPacket();

            println(getClass(), "method: sendItem() -> TradeStarter Add Item", false, PRINT_DEBUG);
        }
    }

    /**
     * Stage 3:
     * Called when we need to remove an item from the trade menu.
     *
     * @param player    The player who removed the item.
     * @param tradeUUID The unique id for this trade.
     * @param itemSlot  The slot the item was removed from.
     */
    public void removeItem(Player player, int tradeUUID, byte itemSlot) {

        println(getClass(), "[1] Remove trade item called! Player: " + player, false, PRINT_DEBUG);

        if (!isValidTrade(player, tradeUUID)) return;
        if (!slotInsideWindow(itemSlot)) return;

        TradeData tradeData = tradeDataMap.get(tradeUUID);

        ItemStack itemStack = player.getPlayerBag().getInventorySlotArray()[itemSlot].getItemStack();
        if (itemStack == null) return;

        println(getClass(), "[2] Remove trade item called! Player: " + player.getName());

        if (tradeData.targetPlayer == player) {
            println(getClass(), "[3] Remove trade item called! Player: " + player.getName());
// TODO            if (tradeData.tradeTargetItems[itemSlot] != null) return;
            tradeData.tradeTargetItems[itemSlot] = null;
            new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_ITEM_REMOVE, tradeUUID, itemSlot)).sendPacket();

            println(getClass(), "[4] Remove trade item called! Player: " + player.getName());
        } else {
            println(getClass(), "[5] Remove trade item called! Player: " + player.getName());
// TODO            if (tradeData.tradeStarterItems[itemSlot] != null) return;
            tradeData.tradeStarterItems[itemSlot] = null;
            new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_ITEM_REMOVE, tradeUUID, itemSlot)).sendPacket();

            println(getClass(), "[6] Remove trade item called! Player: " + player.getName());
        }
    }

    /**
     * Stage 4:
     * Called when a trade confirmation has been received.
     *
     * @param confirmedPlayer The player who confirmed the item trade
     * @param tradeUUID       The trade window unique reference id.
     */
    public void playerConfirmedTrade(Player confirmedPlayer, int tradeUUID) {
        println(getClass(), "method: playerConfirmedTrade(" + confirmedPlayer + ", " + tradeUUID + ")", false, PRINT_DEBUG);
        if (!isValidTrade(confirmedPlayer, tradeUUID)) return;
        TradeData tradeData = tradeDataMap.get(tradeUUID);

        // Set boolean on who confirmed trade. Don't do trade until both booleans are set by both players
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
            new ChatMessagePacketOut(tradeData.tradeStarter, ChatChannelType.TRADE, MessageText.SERVER + "Trade offer confirmation accepted!").sendPacket();
            new ChatMessagePacketOut(tradeData.targetPlayer, ChatChannelType.TRADE, MessageText.SERVER + "Trade offer confirmation accepted!").sendPacket();
            new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_OFFER_COMPLETE)).sendPacket();
            new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_OFFER_COMPLETE)).sendPacket();

            // Trade finished, clear io
            removeTradeData(tradeUUID, true);

            // Update player inventories
            List<ItemStack> starterGiveItems = generateGiveItems(tradeData.tradeStarter, tradeData.tradeStarterItems);
            List<ItemStack> targetGiveItems = generateGiveItems(tradeData.targetPlayer, tradeData.tradeTargetItems);

            // Check to make sure the player has room for the traded items
            if (!checkInventoryForTradeSpace(tradeData.tradeStarter, starterGiveItems, targetGiveItems)) {
                new ChatMessagePacketOut(tradeData.tradeStarter, ChatChannelType.TRADE, MessageText.SERVER + "Trade canceled! You don't have enough item space.").sendPacket();
                new ChatMessagePacketOut(tradeData.targetPlayer, ChatChannelType.TRADE, MessageText.SERVER + "Trade canceled! " + tradeData.tradeStarter + " didn't have enough item space.").sendPacket();
                return;
            }
            if (!checkInventoryForTradeSpace(tradeData.targetPlayer, targetGiveItems, starterGiveItems)) {
                new ChatMessagePacketOut(tradeData.tradeStarter, ChatChannelType.TRADE, MessageText.SERVER + "Trade canceled! " + tradeData.targetPlayer + " didn't have enough item space.").sendPacket();
                new ChatMessagePacketOut(tradeData.targetPlayer, ChatChannelType.TRADE, MessageText.SERVER + "Trade canceled! You don't have enough item space.").sendPacket();
                return;
            }

            // Finally update player inventories
            updateInventories(tradeData.tradeStarter, tradeData.tradeStarterItems, targetGiveItems);
            updateInventories(tradeData.targetPlayer, tradeData.tradeTargetItems, starterGiveItems);

            println(getClass(), "method: playerConfirmedTrade() -> Trade Success!", false, PRINT_DEBUG);
        } else {
            /*
             * TRADE ITEMS CONFIRMED FOR ONE PLAYER
             */
            new ChatMessagePacketOut(tradeData.tradeStarter, ChatChannelType.TRADE, MessageText.SERVER + "" + confirmedPlayer.getName() + " has confirmed the trade!").sendPacket();
            new ChatMessagePacketOut(tradeData.targetPlayer, ChatChannelType.TRADE, MessageText.SERVER + "" + confirmedPlayer.getName() + " has confirmed the trade!").sendPacket();
            new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_OFFER_CONFIRM, tradeUUID, confirmedPlayer.getServerEntityId())).sendPacket();
            new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_OFFER_CONFIRM, tradeUUID, confirmedPlayer.getServerEntityId())).sendPacket();

            println(getClass(), "method: playerConfirmedTrade() -> Trade confirmed for single player. Player: " + confirmedPlayer.getName(), false, PRINT_DEBUG);
        }
    }

    public void playerUnconfirmedTrade(Player confirmedPlayer, int tradeUUID) {
        println(getClass(), "method: playerUnconfirmedTrade(" + confirmedPlayer + ", " + tradeUUID + ")", false, PRINT_DEBUG);
        if (!isValidTrade(confirmedPlayer, tradeUUID)) return;

        TradeData tradeData = tradeDataMap.get(tradeUUID);

        // Make sure both players haven't already confirmed the trade!
        if (tradeData.tradeStarterConfirmedTrade && tradeData.targetPlayerConfirmedTrade) return;

        // Set boolean on who confirmed trade. Don't do trade until both booleans are set by both players
        if (confirmedPlayer == tradeData.targetPlayer) {
            tradeData.targetPlayerConfirmedTrade = false;
        } else {
            tradeData.tradeStarterConfirmedTrade = false;
        }

        new ChatMessagePacketOut(tradeData.tradeStarter, ChatChannelType.TRADE, MessageText.SERVER + "" + confirmedPlayer.getName() + " has unconfirmed the trade!").sendPacket();
        new ChatMessagePacketOut(tradeData.targetPlayer, ChatChannelType.TRADE, MessageText.SERVER + "" + confirmedPlayer.getName() + " has unconfirmed the trade!").sendPacket();
        new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_OFFER_UNCONFIRM, tradeUUID, confirmedPlayer.getServerEntityId())).sendPacket();
        new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_OFFER_UNCONFIRM, tradeUUID, confirmedPlayer.getServerEntityId())).sendPacket();

        println(getClass(), "method: playerUnconfirmedTrade() -> trade now unconfirmed", false, PRINT_DEBUG);
    }

    /**
     * One size fits all trade cancel.
     *
     * @param canceler          The player who canceled the trade.
     * @param tradeUUID         The trade window unique reference id.
     * @param tradeStatusOpcode The type of trade cancel.
     */
    public void tradeCanceled(Player canceler, int tradeUUID, TradeStatusOpcode tradeStatusOpcode) {
        println(getClass(), "method: tradeCanceled(" + canceler + ", " + tradeUUID + ")", false, PRINT_DEBUG);
        if (!isValidTrade(canceler, tradeUUID)) return;
        TradeData tradeData = tradeDataMap.get(tradeUUID);

        if (tradeStatusOpcode == TradeStatusOpcode.TRADE_REQUEST_TARGET_DECLINE) {
            new ChatMessagePacketOut(tradeData.targetPlayer, ChatChannelType.TRADE, MessageText.SERVER + "Trade request declined.").sendPacket();
            new ChatMessagePacketOut(tradeData.tradeStarter, ChatChannelType.TRADE, MessageText.SERVER + "Trade request declined.").sendPacket();
            new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_REQUEST_TARGET_DECLINE)).sendPacket();
            new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_REQUEST_TARGET_DECLINE)).sendPacket();
        } else if (tradeStatusOpcode == TradeStatusOpcode.TRADE_CANCELED) {
            new ChatMessagePacketOut(tradeData.targetPlayer, ChatChannelType.TRADE, MessageText.SERVER + "Trade offer canceled.").sendPacket();
            new ChatMessagePacketOut(tradeData.tradeStarter, ChatChannelType.TRADE, MessageText.SERVER + "Trade offer canceled.").sendPacket();
            new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_CANCELED)).sendPacket();
            new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_CANCELED)).sendPacket();
        }

        tradeData.tradeActive = false;
        tradeData.timeLeft = 0;

        println(getClass(), "method: tradeCanceled() -> Trade successfully canceled!", false, PRINT_DEBUG);
    }

    private boolean checkInventoryForTradeSpace(Player player, List<ItemStack> givingItems, List<ItemStack> receivingItems) {
        return player.getPlayerBag().takenSlots() - givingItems.size() + receivingItems.size() <= InventoryConstants.BAG_SIZE;
    }

    /**
     * Used to check if the trade needs to be canceled.
     *
     * @param player The player were checking against.
     */
    public void ifTradeExistCancel(Player player, String cancelMessage) {
        if (!tradeDataMap.containsKey(player.getTradeUUID())) return;
        println(getClass(), "method: ifTradeExistCancel(" + player + ", " + cancelMessage + ")", false, PRINT_DEBUG);
        TradeData tradeData = tradeDataMap.get(player.getTradeUUID());

        new ChatMessagePacketOut(tradeData.tradeStarter, ChatChannelType.TRADE, cancelMessage).sendPacket();
        new ChatMessagePacketOut(tradeData.targetPlayer, ChatChannelType.TRADE, cancelMessage).sendPacket();
        new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_CANCELED)).sendPacket();
        new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_CANCELED)).sendPacket();

        removeTradeData(player.getTradeUUID(), true);
        println(getClass(), "method: ifTradeExistCancel() -> Success", false, PRINT_DEBUG);
    }

    /**
     * Removes the {@link TradeData} from the tradeDataMap.
     *
     * @param tradeUUID      The key in the tradeDataMap.
     * @param removeMapEntry Used to prevent {@link ConcurrentModificationException}
     */
    private void removeTradeData(int tradeUUID, boolean removeMapEntry) {
        if (!tradeDataMap.containsKey(tradeUUID)) return;

        TradeData tradeData = tradeDataMap.get(tradeUUID);
        tradeData.tradeStarter.setTradeUUID(-1);
        tradeData.targetPlayer.setTradeUUID(-1);

        if (removeMapEntry) tradeDataMap.remove(tradeUUID);
    }

    /**
     * Generates a unique tradeUUID based on both players server id.
     *
     * @param traderStarter The player who initialized the trade.
     * @param targetPlayer  The player who is going to receive the trade request.
     * @return A unique trade id.
     */
    private int generateTradeId(Player traderStarter, Player targetPlayer) {
        int tradeUUID = traderStarter.getServerEntityId();
        tradeUUID <<= 16;
        tradeUUID |= targetPlayer.getServerEntityId();
        println(getClass(), "method: generateTradeId(" + traderStarter + ", " + targetPlayer + ", TradeID: " + tradeUUID + ")", false, PRINT_DEBUG);
        return tradeUUID;
    }

    /**
     * Ticks the trade time out time.
     *
     * @param ticksPassed The number of ticks passed in the {@link GameLoop}
     */
    @Override
    public void tick(long ticksPassed) {
        if (ticksPassed % 20 == 0) {

            Iterator<TradeData> iterator = tradeDataMap.values().iterator();
            while (iterator.hasNext()) {
                TradeData tradeData = iterator.next();

                tradeData.timeLeft--;
                if (tradeData.timeLeft <= 0 && !tradeData.tradeActive) {

                    removeTradeData(tradeData.tradeStarter.getTradeUUID(), false);

                    // Notify the trade starter that the target tradeStarter didn't respond in time.
                    new ChatMessagePacketOut(tradeData.tradeStarter, ChatChannelType.TRADE, MessageText.SERVER + "Trade canceled. Trade timed out.").sendPacket();
                    new ChatMessagePacketOut(tradeData.targetPlayer, ChatChannelType.TRADE, MessageText.SERVER + "Trade canceled. Trade timed out.").sendPacket();
                    new PlayerTradePacketOut(tradeData.tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_REQUEST_SERVER_TIMED_OUT)).sendPacket();
                    new PlayerTradePacketOut(tradeData.targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_REQUEST_SERVER_TIMED_OUT)).sendPacket();

                    iterator.remove();

                    println(getClass(), "Open Trade Entries: " + tradeDataMap.size(), false, PRINT_DEBUG);
                }
            }
        }
    }

    /**
     * Test to see if a trade has already been started for the given packetReceiver.
     *
     * @param tradeStarter The player we are going to test.
     * @return True if they have already started a trade, false otherwise.
     */
    private boolean isTradeInProgress(Player tradeStarter, Player targetPlayer) {
        for (TradeData tradeData : tradeDataMap.values()) {
            if (tradeData.tradeStarter == tradeStarter || tradeData.targetPlayer == tradeStarter) return true;
            if (tradeData.tradeStarter == targetPlayer || tradeData.targetPlayer == targetPlayer) return true;
        }
        return false;
    }

    /**
     * Does array bounds checking for the itemSlot in reference to the bag size.
     *
     * @param itemSlot The slot number to test.
     * @return True if it is in range, false otherwise.
     */
    private boolean slotInsideWindow(byte itemSlot) {
        return itemSlot >= 0 && itemSlot < InventoryConstants.BAG_SIZE;
    }

    /**
     * Sanity check to make sure that the player who is doing a trade action is contained in the
     * tradeDataMap.
     *
     * @param trader    The player we are verifying.
     * @param tradeUUID The unique id in reference to this trade.
     * @return True if the player passes the check, false otherwise.
     */
    private boolean isValidTrade(Player trader, int tradeUUID) {

        // Make sure the tradeUUID exists
        if (!tradeDataMap.containsKey(tradeUUID)) return false;

        // Make sure this player is a valid trader for this trade
        if (!tradeDataMap.get(tradeUUID).isTrader(trader)) return false;

        TradeData tradeData = tradeDataMap.get(tradeUUID);
        Player tradeStarter = tradeData.tradeStarter;
        Player targetPlayer = tradeData.targetPlayer;

        // Check for Map related issues
        if (!checkMapSanity(tradeStarter, targetPlayer)) {
            removeTradeData(tradeUUID, true);
            return false;
        }

        return true;
    }

    /**
     * Checks to make sure the player is trading within map related rules.
     *
     * @param tradeStarter The {@link Player} that started the trade.
     * @param targetPlayer The {@link Player} that received the trade request.
     * @return True if passing sanity checks, false otherwise.
     */
    private boolean checkMapSanity(Player tradeStarter, Player targetPlayer) {
        // Make sure the player is on the same map as the other one
        if (!tradeStarter.getWorldName().equals(targetPlayer.getWorldName())) {
            new ChatMessagePacketOut(targetPlayer, ChatChannelType.TRADE, MessageText.SERVER + "Trade canceled because a player left the map.").sendPacket();
            new ChatMessagePacketOut(tradeStarter, ChatChannelType.TRADE, MessageText.SERVER + "Trade canceled because a player left the map.").sendPacket();
            new PlayerTradePacketOut(tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_CANCELED)).sendPacket();
            new PlayerTradePacketOut(targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_CANCELED)).sendPacket();

            removeTradeData(tradeStarter.getTradeUUID(), true);
            return false;
        }

        // Make sure the player is within the correct distance
        if (!tradeStarter.getCurrentWorldLocation().isWithinDistance(targetPlayer.getCurrentWorldLocation(), MAX_TRADE_DISTANCE)) {
            new ChatMessagePacketOut(targetPlayer, ChatChannelType.TRADE, MessageText.SERVER + "Trade canceled because both players are too far apart.").sendPacket();
            new ChatMessagePacketOut(tradeStarter, ChatChannelType.TRADE, MessageText.SERVER + "Trade canceled because both players are too far apart.").sendPacket();
            new PlayerTradePacketOut(tradeStarter, new TradePacketInfoOut(TradeStatusOpcode.TRADE_CANCELED)).sendPacket();
            new PlayerTradePacketOut(targetPlayer, new TradePacketInfoOut(TradeStatusOpcode.TRADE_CANCELED)).sendPacket();

            removeTradeData(tradeStarter.getTradeUUID(), true);
            return false;
        }
        return true;
    }

    /**
     * Generates the {@link ItemStack}s needed to give a packetReceiver.
     *
     * @param trader     The player that is offering items.
     * @param tradeItems The item slots of the {@link PlayerBag}
     * @return A list of generated {@link ItemStack}
     */
    private List<ItemStack> generateGiveItems(Player trader, Byte[] tradeItems) {
        List<ItemStack> giveItems = new ArrayList<>();
        InventorySlot[] bagItems = trader.getPlayerBag().getInventorySlotArray();
        for (Byte itemSlot : tradeItems) {
            if (itemSlot == null) continue;
            ItemStack itemStack = bagItems[itemSlot].getItemStack();
            giveItems.add(itemStack);
        }
        return giveItems;
    }

    /**
     * Does the final transferring of times.
     *
     * @param playerToUpdate The player who will have their client item
     *                       and server {@link PlayerBag} updated.
     * @param itemsToRemove  The {@link ItemStack}s we are going to remove from their bag.
     * @param itemsToAdd     The {@link ItemStack}s we are going to add to their bag.
     */
    private void updateInventories(Player playerToUpdate, Byte[] itemsToRemove, List<ItemStack> itemsToAdd) {
        // Dump items from trade starter bag
        for (Byte slotIndex : itemsToRemove) {
            if (slotIndex != null) {
                playerToUpdate.getPlayerBag().removeItemStack(slotIndex, true);
            }
        }

        // Send the trade starter the target player items
        for (ItemStack itemStack : itemsToAdd) {
            playerToUpdate.give(itemStack, true);
        }
    }

    /**
     * A container class that holds information about a trade between two {@link Player}s
     */
    static class TradeData {
        private final Player tradeStarter;
        private final Player targetPlayer;

        private int timeLeft = MAX_TIME;

        private boolean tradeActive = false;

        private boolean tradeStarterConfirmedTrade = false;
        private boolean targetPlayerConfirmedTrade = false;

        private final Byte[] tradeStarterItems = new Byte[InventoryConstants.BAG_SIZE];
        private final Byte[] tradeTargetItems = new Byte[InventoryConstants.BAG_SIZE];

        TradeData(Player tradeStarter, Player targetPlayer) {
            this.tradeStarter = tradeStarter;
            this.targetPlayer = targetPlayer;
        }

        /**
         * Test to see if the player supplied is a participant in this trade
         *
         * @param player The player we are testing.
         * @return True if they are a valid participant, false otherwise.
         */
        private boolean isTrader(Player player) {
            return player == tradeStarter || player == targetPlayer;
        }

        /**
         * Adds a trade item to the trade window. The item is represented at
         * the slot index of the bag window.
         *
         * @param tradeItems The trade items for a trader.
         * @param bagSlot    The slot of the bag being reference in the trade
         *                   window.
         */
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
