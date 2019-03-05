package com.valenguard.server.network.packet.in;

import com.valenguard.server.game.entity.*;
import com.valenguard.server.game.inventory.ItemStack;
import com.valenguard.server.game.inventory.ItemStackSlotData;
import com.valenguard.server.game.inventory.ItemStackType;
import com.valenguard.server.game.maps.GameMap;
import com.valenguard.server.network.packet.out.EntityAppearancePacketOut;
import com.valenguard.server.network.shared.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.valenguard.server.util.Log.println;

@Opcode(getOpcode = Opcodes.CLICK_ACTION)
public class ClickActionPacketIn implements PacketListener<ClickActionPacketIn.ClickActionPacket> {

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
        // TODO: Anti-Hack Check: Check that packetReceiver is close.

        Player player = packetData.getPlayer();
        GameMap gameMap = player.getGameMap();
        EntityType entityType = packetData.getEntityType();

        switch (entityType) {
            case CLIENT_PLAYER:
            case PLAYER:
                // TODO
                break;
            case MONSTER:
            case NPC:
                aiEntityClick(player, gameMap, packetData);
                break;
            case ITEM_STACK:
                itemStackDropClick(player, gameMap, packetData);
                break;
            case SKILL_NODE:
                stationaryEntityClick(gameMap, packetData);
                break;
        }
    }

    private void aiEntityClick(Player player, GameMap gameMap, ClickActionPacket packetData) {
        if (!gameMap.getAiEntityMap().containsKey(packetData.getEntityUUID())) return;

        AiEntity aiEntity = gameMap.getAiEntityMap().get(packetData.getEntityUUID());

        player.setTargetEntity(aiEntity);
        aiEntity.setTargetEntity(player);
    }

    private void stationaryEntityClick(GameMap gameMap, ClickActionPacket packetData) {
        if (!gameMap.getStationaryEntityMap().containsKey(packetData.getEntityUUID())) return;
        StationaryEntity clickedOnEntity = gameMap.getStationaryEntityMap().get(packetData.getEntityUUID());
        changeEntityAppearance(clickedOnEntity, gameMap);
    }

    private void itemStackDropClick(Player player, GameMap gameMap, ClickActionPacket packetData) {
        if (!gameMap.getItemStackDropMap().containsKey(packetData.getEntityUUID())) return;

        // Click received, lets pick up the item!
        println(getClass(), "Incoming ItemStack click!");
        ItemStackDrop itemStackDrop = gameMap.getItemStackDropMap().get(packetData.getEntityUUID());
        ItemStack itemStack = itemStackDrop.getItemStack();

        if (itemStackDrop.isPickedUp()) return;

        // TODO: generalize for other
        if (itemStack.getItemStackType() == ItemStackType.GOLD) {
            if (player.getGold() == null && player.getPlayerBag().isBagFull()) {
                return;
            }
        } else {
            if (player.getPlayerBag().isBagFull()) {
                return;
            }
        }

        // Despawn the item
        gameMap.queueItemStackDropDespawn(itemStackDrop);

        // Don't let others pick the item up.
        itemStackDrop.setPickedUp(true);

        if (itemStack.getItemStackType() == ItemStackType.GOLD) {
            ItemStackSlotData itemStackSlotData = player.getGold();
            if (itemStackSlotData != null) {
                ItemStack goldItemStack = itemStackSlotData.getItemStack();
                goldItemStack.setAmount(goldItemStack.getAmount() + itemStack.getAmount());

                // Removing their previous gold stack.
                player.removeItemStack(itemStackSlotData.getBagIndex());

                player.setItemStack(itemStackSlotData.getBagIndex(), goldItemStack);
            }
        } else {
            // Send the packetReceiver the item
            player.giveItemStack(itemStack);
        }

    }

    private void changeEntityAppearance(Entity entity, GameMap gameMap) {
        short appearanceID = entity.getAppearance().getTextureId(0);

        if (appearanceID >= 3) appearanceID = 0;
        else appearanceID++;

        entity.setAppearance(new Appearance((byte) 0, new short[]{appearanceID}));

        gameMap.forAllPlayers(player -> new EntityAppearancePacketOut(player, entity, EntityAppearancePacketOut.BODY_INDEX).sendPacket());

    }

    @Getter
    @AllArgsConstructor
    class ClickActionPacket extends PacketData {
        private final byte clickAction;
        private final EntityType entityType;
        private final short entityUUID;
    }
}
