package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.entity.Appearance;
import com.valenguard.server.game.world.entity.EntityType;
import com.valenguard.server.game.world.entity.ItemStackDrop;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.inventory.*;
import com.valenguard.server.game.world.maps.GameMap;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.network.game.shared.*;
import lombok.AllArgsConstructor;

import static com.valenguard.server.util.Log.println;

@Opcode(getOpcode = Opcodes.INVENTORY_UPDATE)
public class InventoryPacketIn implements PacketListener<InventoryPacketIn.InventoryActionsPacket> {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {

        byte inventoryAction = clientHandler.readByte();
        byte fromPosition = -1;
        byte toPosition = -1;
        byte fromWindow = -1;
        byte toWindow = -1;

        byte dropInventory = -1;
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
                dropInventory = clientHandler.readByte();
                slotIndex = clientHandler.readByte();
                break;
        }

        return new InventoryActionsPacket(actionType, fromPosition, toPosition, fromWindow, toWindow, dropInventory, slotIndex);
    }

    @Override
    public boolean sanitizePacket(InventoryActionsPacket packetData) {

        if (packetData.actionType == InventoryActions.ActionType.MOVE) {
            // Making sure they are sending correct window types.
            if (packetData.toWindow >= InventoryType.values().length || packetData.fromWindow >= InventoryType.values().length) {
                return false;
            }
        } else if (packetData.actionType == InventoryActions.ActionType.DROP) {
            if (packetData.dropInventory >= InventoryType.values().length) {
                return false;
            }
        }

        boolean validInventoryAction = packetData.actionType.getGetActionType() <= 2;

        // TODO this should be cleaner
        return validInventoryAction;
    }

    @Override
    public void onEvent(InventoryActionsPacket packetData) {
        if (packetData.actionType == InventoryActions.ActionType.MOVE) {
            moveItemStack(packetData);
        } else if (packetData.actionType == InventoryActions.ActionType.DROP) {
            dropItemStack(packetData);
        }
    }

    private void moveItemStack(InventoryActionsPacket packetData) {
        InventoryType fromWindow = InventoryType.values()[packetData.fromWindow];
        InventoryType toWindow = InventoryType.values()[packetData.toWindow];

        if (!doesNotExceedInventoryLimit(fromWindow, toWindow, packetData)) return;

        InventoryMoveType inventoryMoveType = InventoryMovementUtil.getWindowMovementInfo(fromWindow, toWindow);

        PlayerMoveInventoryEvents playerMoveInventoryEvents = Server.getInstance().getGameLoop().getPlayerMoveInventoryEvents();
        playerMoveInventoryEvents.addInventoryEvent(new InventoryEvent(packetData.getPlayer(), packetData.fromPosition, packetData.toPosition, inventoryMoveType));
    }

    private void dropItemStack(InventoryActionsPacket packetData) {

        InventoryType inventoryType = InventoryType.values()[packetData.dropInventory];
        if (inventoryType == InventoryType.BAG_1) {

            if (packetData.slotIndex < 0 || packetData.slotIndex >= InventoryConstants.BAG_SIZE) {
                return;
            }

            ItemStack itemStack = packetData.getPlayer().getPlayerBag().getInventorySlotArray()[packetData.slotIndex].getItemStack();
            packetData.getPlayer().getPlayerBag().removeItemStack(packetData.slotIndex, true);

            GameMap gameMap = packetData.getPlayer().getGameMap();

            ItemStackDrop itemStackDrop = new ItemStackDrop();
            itemStackDrop.setEntityType(EntityType.ITEM_STACK);
            itemStackDrop.setName(itemStack.getName());
            itemStackDrop.setCurrentMapLocation(new Location(packetData.getPlayer().getCurrentMapLocation()));
            itemStackDrop.setAppearance(new Appearance(itemStackDrop, (byte) 0, new short[]{(short) itemStack.getItemId()}));
            itemStackDrop.setItemStack(itemStack);
            itemStackDrop.setDropOwner(packetData.getPlayer());
            itemStackDrop.setServerEntityId(gameMap.getLastItemStackDrop());

            gameMap.setLastItemStackDrop((short) (gameMap.getLastItemStackDrop() + 1));

            gameMap.getItemStackDropEntityController().queueEntitySpawn(itemStackDrop);

        } else if (inventoryType == InventoryType.EQUIPMENT) {

            if (packetData.slotIndex < 0 || packetData.slotIndex >= InventoryConstants.EQUIPMENT_SIZE) {
                // TODO: remove this later
                return;
            }
        }
    }

    private boolean doesNotExceedInventoryLimit(InventoryType fromWindow, InventoryType toWindow, InventoryActionsPacket packetData) {

        if (fromWindow == InventoryType.BAG_1) {
            if (packetData.fromPosition >= InventoryConstants.BAG_SIZE || packetData.fromPosition < 0)
                return false;
        } else if (fromWindow == InventoryType.EQUIPMENT) {
            if (packetData.fromPosition >= InventoryConstants.EQUIPMENT_SIZE || packetData.fromPosition < 0)
                return false;
        } else if (fromWindow == InventoryType.BANK) {
            if (packetData.fromPosition >= InventoryConstants.BANK_SIZE|| packetData.fromPosition < 0)
                return false;
        }

        if (toWindow == InventoryType.BAG_1) {
            return packetData.toPosition < InventoryConstants.BAG_SIZE && packetData.toPosition >= 0;
        } else if (toWindow == InventoryType.EQUIPMENT) {
            return packetData.toPosition < InventoryConstants.EQUIPMENT_SIZE && packetData.toPosition >= 0;
        } else if (toWindow == InventoryType.BANK) {
            return packetData.toPosition < InventoryConstants.BANK_SIZE && packetData.toPosition >= 0;
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

        private byte dropInventory;
        private byte slotIndex;
    }
}
