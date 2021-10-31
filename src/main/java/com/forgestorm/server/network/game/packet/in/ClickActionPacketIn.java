package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.rpg.StationaryTypes;
import com.forgestorm.server.game.world.entity.*;
import com.forgestorm.shared.game.world.item.ItemStack;
import com.forgestorm.shared.game.world.item.ItemStackType;
import com.forgestorm.server.game.world.item.inventory.InventorySlot;
import com.forgestorm.server.game.world.item.inventory.PlayerBag;
import com.forgestorm.server.game.world.maps.GameWorld;
import com.forgestorm.server.network.game.shared.*;
import com.forgestorm.shared.network.game.Opcode;
import com.forgestorm.shared.network.game.Opcodes;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.forgestorm.server.util.Log.println;

@Opcode(getOpcode = Opcodes.CLICK_ACTION)
public class ClickActionPacketIn implements PacketListener<ClickActionPacketIn.ClickActionPacket> {

    private static final boolean PRINT_DEBUG = false;

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        byte clickAction = clientHandler.readByte();
        byte entityType = clientHandler.readByte();
        short entityUUID = clientHandler.readShort();
        return new ClickActionPacket(clickAction, EntityType.getEntityType(entityType), entityUUID);
    }

    @Override
    public boolean sanitizePacket(ClickActionPacket packetData) {
        return packetData.clickAction <= 0x02;
    }

    @Override
    public void onEvent(ClickActionPacket packetData) {
        // TODO: Anti-Hack Check: Check that player is exitServer.

        Player player = packetData.getClientHandler().getPlayer();
        GameWorld gameWorld = player.getGameWorld();
        EntityType entityType = packetData.getEntityType();

        switch (entityType) {
            case CLIENT_PLAYER:
            case PLAYER:
                // TODO
                break;
            case MONSTER:
            case NPC:
                aiEntityClick(player, gameWorld, packetData);
                break;
            case ITEM_STACK:
                itemStackDropClick(player, gameWorld, packetData);
                break;
            case SKILL_NODE:
                stationaryEntityClick(gameWorld, packetData);
                break;
        }
    }

    private void aiEntityClick(Player player, GameWorld gameWorld, ClickActionPacket packetData) {
        if (gameWorld.getAiEntityController().doesNotContainKey(packetData.getEntityUUID())) return;

        AiEntity aiEntity = (AiEntity) gameWorld.getAiEntityController().getEntity(packetData.getEntityUUID());

        player.setTargetEntity(aiEntity);
        aiEntity.setTargetEntity(player);
    }

    private void stationaryEntityClick(GameWorld gameWorld, ClickActionPacket packetData) {
        if (gameWorld.getStationaryEntityController().doesNotContainKey(packetData.getEntityUUID())) return;
        StationaryEntity clickedOnEntity = (StationaryEntity) gameWorld.getStationaryEntityController().getEntity(packetData.getEntityUUID());

        // TODO: check if the place they're trying to gather from is empty

        if (clickedOnEntity.getStationaryType() == StationaryTypes.ORE) {
            ServerMain.getInstance().getGameLoop().getProcessMining().addPlayerToMine(packetData.getClientHandler().getPlayer(), clickedOnEntity);
        }
    }

    private void itemStackDropClick(Player player, GameWorld gameWorld, ClickActionPacket packetData) {
        if (gameWorld.getItemStackDropEntityController().doesNotContainKey(packetData.getEntityUUID())) return;

        // Click received, lets pick up the item!
        println(getClass(), "Incoming ItemStack click!", false, PRINT_DEBUG);
        ItemStackDrop itemStackDrop = (ItemStackDrop) gameWorld.getItemStackDropEntityController().getEntity(packetData.getEntityUUID());
        ItemStack itemStack = itemStackDrop.getItemStack();
        PlayerBag playerBag = player.getPlayerBag();

        if (itemStackDrop.isPickedUp()) return;

        // Stack stackable items
        // TODO: Generalize for additional stackable item types
        if (itemStack.getItemStackType() == ItemStackType.GOLD) {
            if (playerBag.getGoldInventorySlot() == null && player.getPlayerBag().isInventoryFull()) return;
        } else {
            // Item is not stackable
            if (playerBag.isInventoryFull()) return;
        }

        // Despawn the item
        gameWorld.getItemStackDropEntityController().queueEntityDespawn(itemStackDrop);

        // Don't let others pick the item up.
        itemStackDrop.setPickedUp(true);

        if (itemStack.getItemStackType() == ItemStackType.GOLD) {
            InventorySlot inventorySlot = playerBag.getGoldInventorySlot();
            if (inventorySlot != null) {

                ItemStack goldItemStack = new ItemStack(inventorySlot.getItemStack());
                goldItemStack.setAmount(goldItemStack.getAmount() + itemStack.getAmount());

                playerBag.setItemStack(inventorySlot.getSlotIndex(), goldItemStack, true);

            } else {
                // Giving the player the ItemStack of gold since they don't have any gold on them.
                player.give(itemStack, true);
            }
        } else {
            // Send the player the item
            player.give(itemStack, true);
        }

    }

    @Getter
    @AllArgsConstructor
    static class ClickActionPacket extends PacketData {
        private final byte clickAction;
        private final EntityType entityType;
        private final short entityUUID;
    }
}
