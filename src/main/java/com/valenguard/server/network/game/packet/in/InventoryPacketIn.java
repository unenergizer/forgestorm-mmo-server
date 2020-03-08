package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.ItemStackConsumerManager;
import com.valenguard.server.game.world.item.inventory.*;
import com.valenguard.server.game.world.maps.GameMap;
import com.valenguard.server.game.world.maps.ItemStackDropEntityController;
import com.valenguard.server.network.game.packet.out.InventoryPacketOut;
import com.valenguard.server.network.game.shared.*;
import lombok.AllArgsConstructor;

import static com.valenguard.server.util.Log.println;

@Opcode(getOpcode = Opcodes.INVENTORY_UPDATE)
public class InventoryPacketIn implements PacketListener<InventoryPacketIn.InventoryActionsPacket> {

    private static final boolean PRINT_DEBUG = false;

    private ItemStackConsumerManager itemStackConsumerManager = new ItemStackConsumerManager();

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {

        byte inventoryAction = clientHandler.readByte();
        byte fromPosition = -1;
        byte toPosition = -1;
        byte fromWindow = -1;
        byte toWindow = -1;

        byte interactInventory = -1;
        byte slotIndex = -1;

        InventoryActions.ActionType actionType = InventoryActions.ActionType.getActionType(inventoryAction);

        switch (actionType) {
            case MOVE:
                fromPosition = clientHandler.readByte();
                toPosition = clientHandler.readByte();
                byte windowsByte = clientHandler.readByte();
                fromWindow = (byte) (windowsByte >> 4);
                toWindow = (byte) (windowsByte & 0x0F);
                break;
            case DROP:
            case CONSUME:
                interactInventory = clientHandler.readByte();
                slotIndex = clientHandler.readByte();
                break;
        }

        return new InventoryActionsPacket(actionType, fromPosition, toPosition, fromWindow, toWindow, interactInventory, slotIndex);
    }

    @Override
    public boolean sanitizePacket(InventoryActionsPacket packetData) {

        if (packetData.actionType == InventoryActions.ActionType.MOVE) {
            // Making sure they are sending correct window types.
            if (packetData.toWindow >= InventoryType.values().length || packetData.fromWindow >= InventoryType.values().length) {
                return false;
            }
        } else if (packetData.actionType == InventoryActions.ActionType.DROP) {
            if (packetData.interactInventory >= InventoryType.values().length) {
                return false;
            }
        }

        boolean validInventoryAction = packetData.actionType.getGetActionType() <= 3;

        // TODO this should be cleaner
        return validInventoryAction;
    }

    @Override
    public void onEvent(InventoryActionsPacket packetData) {
        if (packetData.actionType == InventoryActions.ActionType.MOVE) {
            moveItemStack(packetData);
        } else if (packetData.actionType == InventoryActions.ActionType.DROP) {
            dropItemStack(packetData);
        } else if (packetData.actionType == InventoryActions.ActionType.CONSUME) {
            consumeItem(packetData);
        }
    }

    private void moveItemStack(InventoryActionsPacket packetData) {
        InventoryType fromWindow = InventoryType.values()[packetData.fromWindow];
        InventoryType toWindow = InventoryType.values()[packetData.toWindow];

        println(getClass(), "FROM WINDOW => " + fromWindow, false, PRINT_DEBUG);
        println(getClass(), "TO WINDOW => " + toWindow, false, PRINT_DEBUG);

        if (!doesNotExceedInventoryLimit(fromWindow, toWindow, packetData)) {
            println(getClass(), "doesNotExceedInventoryLimit: true!", false, PRINT_DEBUG);
            return;
        }

        InventoryMoveType inventoryMoveType = InventoryMovementUtil.getWindowMovementInfo(fromWindow, toWindow);

        println(getClass(), "InventoryMoveType: " + inventoryMoveType, false, PRINT_DEBUG);

        PlayerMoveInventoryEvents playerMoveInventoryEvents = Server.getInstance().getGameLoop().getPlayerMoveInventoryEvents();
        playerMoveInventoryEvents.addInventoryEvent(new InventoryEvent(packetData.getClientHandler().getPlayer(), packetData.fromPosition, packetData.toPosition, inventoryMoveType));
    }

    private void dropItemStack(InventoryActionsPacket packetData) {
        Player player = packetData.getClientHandler().getPlayer();

        InventoryType inventoryType = InventoryType.values()[packetData.interactInventory];

        if (!doesNotExceedInventoryLimit(inventoryType, packetData.slotIndex)) {
            return;
        }

        if (inventoryType == InventoryType.BAG_1) {

            ItemStack itemStack = player.getPlayerBag().getInventorySlotArray()[packetData.slotIndex].getItemStack();
            player.getPlayerBag().removeItemStack(packetData.slotIndex, true);

            GameMap gameMap = player.getGameMap();

            ItemStackDropEntityController itemStackDropEntityController = gameMap.getItemStackDropEntityController();
            itemStackDropEntityController.queueEntitySpawn(
                    itemStackDropEntityController.makeItemStackDrop(
                            itemStack,
                            player.getCurrentMapLocation(),
                            player
                    ));

        } else if (inventoryType == InventoryType.EQUIPMENT) {


        }
    }

    private void consumeItem(InventoryActionsPacket packetData) {
        Player player = packetData.getClientHandler().getPlayer();

        InventoryType inventoryType = InventoryType.values()[packetData.interactInventory];

        if (!doesNotExceedInventoryLimit(inventoryType, packetData.slotIndex)) {
            return;
        }

        if (inventoryType == InventoryType.BAG_1) {

            ItemStack itemStack = player.getPlayerBag().getItemStack(packetData.slotIndex);

            if (!itemStack.isConsumable()) {
                return;
            }

            itemStackConsumerManager.consumeItem(player, itemStack);

            new InventoryPacketOut(player, new InventoryActions().consume(
                    packetData.interactInventory,
                    packetData.slotIndex
            )).sendPacket();
        }
    }

    private boolean doesNotExceedInventoryLimit(InventoryType fromWindow, InventoryType toWindow, InventoryActionsPacket packetData) {
        if (!doesNotExceedInventoryLimit(fromWindow, packetData.fromPosition)) return false;
        if (!doesNotExceedInventoryLimit(toWindow, packetData.toPosition)) return false;
        return true;
    }

    private boolean doesNotExceedInventoryLimit(InventoryType inventoryType, byte slotIndex) {
        if (inventoryType == InventoryType.BAG_1) {
            return slotIndex < InventoryConstants.BAG_SIZE && slotIndex >= 0;
        } else if (inventoryType == InventoryType.EQUIPMENT) {
            return slotIndex < InventoryConstants.EQUIPMENT_SIZE && slotIndex >= 0;
        } else if (inventoryType == InventoryType.BANK) {
            return slotIndex < InventoryConstants.BANK_SIZE && slotIndex >= 0;
        } else if (inventoryType == InventoryType.HOT_BAR) {
            return slotIndex < InventoryConstants.HOT_BAR_SIZE && slotIndex >= 0;
        }
        return true;
    }

    @AllArgsConstructor
    class InventoryActionsPacket extends PacketData {
        private InventoryActions.ActionType actionType;
        private byte fromPosition;
        private byte toPosition;
        private byte fromWindow;
        private byte toWindow;

        private byte interactInventory;
        private byte slotIndex;
    }
}
