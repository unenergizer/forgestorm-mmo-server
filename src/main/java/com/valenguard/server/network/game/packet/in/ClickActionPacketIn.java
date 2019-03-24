package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.game.world.entity.*;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.ItemStackSlotData;
import com.valenguard.server.game.world.item.ItemStackType;
import com.valenguard.server.game.world.maps.GameMap;
import com.valenguard.server.network.game.packet.out.EntityAppearancePacketOut;
import com.valenguard.server.network.game.shared.*;
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
        // TODO: Anti-Hack Check: Check that packetReceiver is exitServer.

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
        if (!gameMap.getAiEntityController().containsKey(packetData.getEntityUUID())) return;

        AiEntity aiEntity = (AiEntity) gameMap.getAiEntityController().getEntity(packetData.getEntityUUID());

        player.setTargetEntity(aiEntity);
        aiEntity.setTargetEntity(player);
    }

    private void stationaryEntityClick(GameMap gameMap, ClickActionPacket packetData) {
        if (!gameMap.getStationaryEntityController().containsKey(packetData.getEntityUUID())) return;
        StationaryEntity clickedOnEntity = (StationaryEntity) gameMap.getStationaryEntityController().getEntity(packetData.getEntityUUID());
        changeEntityAppearance(clickedOnEntity, gameMap);
    }

    private void itemStackDropClick(Player player, GameMap gameMap, ClickActionPacket packetData) {
        if (!gameMap.getItemStackDropEntityController().containsKey(packetData.getEntityUUID())) return;

        // Click received, lets pick up the item!
        println(getClass(), "Incoming ItemStack click!");
        ItemStackDrop itemStackDrop = (ItemStackDrop) gameMap.getItemStackDropEntityController().getEntity(packetData.getEntityUUID());
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
        gameMap.getItemStackDropEntityController().queueEntityDespawn(itemStackDrop);

        // Don't let others pick the item up.
        itemStackDrop.setPickedUp(true);

        if (itemStack.getItemStackType() == ItemStackType.GOLD) {
            ItemStackSlotData itemStackSlotData = player.getGold();
            if (itemStackSlotData != null) {
                ItemStack goldItemStack = itemStackSlotData.getItemStack();
                goldItemStack.setAmount(goldItemStack.getAmount() + itemStack.getAmount());

                // Removing their previous gold stack.
                player.removeItemStackFromBag(itemStackSlotData.getBagIndex());

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

        gameMap.getPlayerController().forAllPlayers(player -> new EntityAppearancePacketOut(player, entity, EntityAppearancePacketOut.BODY_INDEX).sendPacket());

    }

    @Getter
    @AllArgsConstructor
    class ClickActionPacket extends PacketData {
        private final byte clickAction;
        private final EntityType entityType;
        private final short entityUUID;
    }
}
