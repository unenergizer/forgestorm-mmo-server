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

        if (inventoryAction == InventoryActions.MOVE) {
            fromPosition = clientHandler.readByte();
            toPosition = clientHandler.readByte();
            byte windowsByte = clientHandler.readByte();
            fromWindow = (byte) (windowsByte >> 4);
            toWindow = (byte) (windowsByte & 0x0F);
        } else if (inventoryAction == InventoryActions.DROP) {
            dropInventory = clientHandler.readByte();
            slotIndex = clientHandler.readByte();
        }

        return new InventoryActionsPacket(inventoryAction, fromPosition, toPosition, fromWindow, toWindow, dropInventory, slotIndex);
    }

    @Override
    public boolean sanitizePacket(InventoryActionsPacket packetData) {

        if (packetData.inventoryAction == InventoryActions.MOVE) {
            // Making sure they are sending correct window types.
            if (packetData.toWindow >= InventoryType.values().length || packetData.fromWindow >= InventoryType.values().length) {
                return false;
            }
        } else if (packetData.inventoryAction == InventoryActions.DROP) {
            if (packetData.dropInventory >= InventoryType.values().length) {
                return false;
            }
        }

        // TODO this should be cleaner
        return packetData.inventoryAction <= 2;
    }

    @Override
    public void onEvent(InventoryActionsPacket packetData) {

        if (packetData.inventoryAction == InventoryActions.MOVE) {
            moveItemStack(packetData);
        } else if (packetData.inventoryAction == InventoryActions.DROP) {
            dropItemStack(packetData);
        }

    }

    private void moveItemStack(InventoryActionsPacket packetData) {
        InventoryType fromWindow = InventoryType.values()[packetData.fromWindow];
        InventoryType toWindow = InventoryType.values()[packetData.toWindow];

        if (!doesNotExceedInventoryLimit(fromWindow, toWindow, packetData)) return;

        WindowMovementType windowMovementType = determineWindowMovementInfo(fromWindow, toWindow);

        PlayerMoveInventoryEvents playerMoveInventoryEvents = Server.getInstance().getGameLoop().getPlayerMoveInventoryEvents();
        playerMoveInventoryEvents.addInventoryEvent(new InventoryEvent(packetData.getPlayer(), packetData.fromPosition, packetData.toPosition, windowMovementType));

    }

    private void dropItemStack(InventoryActionsPacket packetData) {

        InventoryType inventoryType = InventoryType.values()[packetData.dropInventory];
        if (inventoryType == InventoryType.BAG_1) {

            if (packetData.slotIndex < 0 || packetData.slotIndex >= InventoryConstants.BAG_SIZE) {
                return;
            }

            ItemStack itemStack = packetData.getPlayer().getPlayerBag().getItems()[packetData.slotIndex];
            packetData.getPlayer().removeItemStackFromBag(packetData.slotIndex);

            GameMap gameMap = packetData.getPlayer().getGameMap();

            ItemStackDrop itemStackDrop = new ItemStackDrop();
            itemStackDrop.setEntityType(EntityType.ITEM_STACK);
            itemStackDrop.setName(itemStack.getName());
            itemStackDrop.setCurrentMapLocation(new Location(packetData.getPlayer().getCurrentMapLocation()));
            itemStackDrop.setAppearance(new Appearance((byte) 0, new short[]{(short) itemStack.getItemId()}));
            itemStackDrop.setItemStack(itemStack);
            itemStackDrop.setDropOwner(packetData.getPlayer());
            itemStackDrop.setServerEntityId(gameMap.getLastItemStackDrop());

            gameMap.setLastItemStackDrop((short) (gameMap.getLastItemStackDrop() + 1));

            gameMap.getItemStackDropEntityController().queueEntitySpawn(itemStackDrop);

        } else if (inventoryType == InventoryType.EQUIPMENT) {

            if (packetData.slotIndex < 0 || packetData.slotIndex >= InventoryConstants.EQUIPMENT_SIZE) {
                return;
            }

            // TODO: remove this later
        }
    }

    private boolean doesNotExceedInventoryLimit(InventoryType fromWindow, InventoryType toWindow, InventoryActionsPacket packetData) {

        if (fromWindow == InventoryType.BAG_1) {
            if (packetData.fromPosition >= InventoryConstants.BAG_SIZE || packetData.fromPosition < 0) return false;
        } else if (fromWindow == InventoryType.EQUIPMENT) {
            if (packetData.fromPosition >= InventoryConstants.EQUIPMENT_SIZE || packetData.fromPosition < 0)
                return false;
        }

        if (toWindow == InventoryType.BAG_1) {
            return packetData.toPosition < InventoryConstants.BAG_SIZE && packetData.toPosition >= 0;
        } else if (fromWindow == InventoryType.EQUIPMENT) {
            return packetData.toPosition < InventoryConstants.EQUIPMENT_SIZE && packetData.toPosition >= 0;
        }

        return true;
    }

    private WindowMovementType determineWindowMovementInfo(InventoryType fromWindow, InventoryType toWindow) {
        if (fromWindow == InventoryType.EQUIPMENT && toWindow == InventoryType.BAG_1) {
            return WindowMovementType.FROM_EQUIPMENT_TO_BAG;
        } else if (fromWindow == InventoryType.BAG_1 && toWindow == InventoryType.EQUIPMENT) {
            return WindowMovementType.FROM_BAG_TO_EQUIPMENT;
        } else if (fromWindow == InventoryType.BAG_1 && toWindow == InventoryType.BAG_1) {
            return WindowMovementType.FROM_BAG_TO_BAG;
        } else if (fromWindow == InventoryType.EQUIPMENT && toWindow == InventoryType.EQUIPMENT) {
            return WindowMovementType.FROM_EQUIPMENT_TO_EQUIPMENT;
        }
        throw new RuntimeException("The sanitization should have already checked for this.");
    }

    @AllArgsConstructor
    class InventoryActionsPacket extends PacketData {
        private byte inventoryAction;
        private byte fromPosition;
        private byte toPosition;
        private byte fromWindow;
        private byte toWindow;

        private byte dropInventory;
        private byte slotIndex;
    }
}
