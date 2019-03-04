package com.valenguard.server.network.packet.in;

import com.valenguard.server.game.entity.*;
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
        // TODO: Anti-Hack Check: Check that player is close.

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

        if (itemStackDrop.isPickedUp()) return;

        // Check inventory for being full first
        if (!player.getPlayerBag().isBagFull()) {

            println(getClass(), "Sending player ItemStack and removing from map & list!");
            // Despawn the item
            gameMap.queueItemStackDropDespawn(itemStackDrop);

            // Don't let others pick the item up.
            itemStackDrop.setPickedUp(true);

            // Send the player the item
            player.giveItemStack(itemStackDrop.getItemStack());
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
