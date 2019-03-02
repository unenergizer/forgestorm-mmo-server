package com.valenguard.server.network.packet.in;

import com.valenguard.server.game.entity.*;
import com.valenguard.server.game.maps.GameMap;
import com.valenguard.server.game.rpg.EntityAlignment;
import com.valenguard.server.network.packet.out.EntityAppearancePacketOut;
import com.valenguard.server.network.shared.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.valenguard.server.util.Log.println;

@Opcode(getOpcode = Opcodes.CLICK_ACTION)
public class ClickActionPacketIn implements PacketListener<ClickActionPacketIn.ClickActionPacket> {

    private static final byte LEFT = 0x01;
    private static final byte RIGHT = 0x02;

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        byte clickAction = clientHandler.readByte();
        short entityUUID = clientHandler.readShort();
        return new ClickActionPacket(clickAction, entityUUID);
    }

    @Override
    public boolean sanitizePacket(ClickActionPacket packetData) {
        return packetData.CLICK_ACTION <= 0x02;
    }

    @Override
    public void onEvent(ClickActionPacket packetData) {
        // TODO: Anti-Hack Check: Check that player is close.
        if (packetData.CLICK_ACTION == LEFT) println(getClass(), "Left click action received!");
        else if (packetData.CLICK_ACTION == RIGHT) println(getClass(), "Right click action received!");
        else println(getClass(), "Some other action received??????????", true);

        Player player = packetData.getPlayer();
        GameMap gameMap = player.getGameMap();

        if (gameMap.getAiEntityMap().get(packetData.getENTITY_UUID()) != null) {
            MovingEntity movingEntity = gameMap.getAiEntityMap().get(packetData.getENTITY_UUID());
            EntityAlignment entityAlignment = movingEntity.getEntityAlignment();

            if (entityAlignment == EntityAlignment.HOSTILE || entityAlignment == EntityAlignment.NEUTRAL) {
                player.setTargetEntity(movingEntity);
                movingEntity.setTargetEntity(player);
            }

        } else if (gameMap.getStationaryEntityMap().get(packetData.getENTITY_UUID()) != null) {
            // Click received, lets perform our skill!
            StationaryEntity clickedOnEntity = gameMap.getStationaryEntityMap().get(packetData.getENTITY_UUID());
            if (clickedOnEntity != null) changeEntityAppearance(clickedOnEntity, gameMap);
        } else if (gameMap.getItemStackDropMap().get(packetData.getENTITY_UUID()) != null) {

            // Click received, lets pick up the item!
            ItemStackDrop itemStackDrop = gameMap.getItemStackDropMap().get(packetData.getENTITY_UUID());

            if (itemStackDrop.isPickedUp()) return;

            // Check inventory for being full first
            if (!player.getPlayerBag().isBagFull()) {

                // Despawn the item
                gameMap.queueItemStackDropDespawn(itemStackDrop);

                // Don't let others pick the item up.
                itemStackDrop.setPickedUp(true);

                // Send the player the item
                player.giveItemStack(itemStackDrop.getItemStack());
            }
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
        private final byte CLICK_ACTION;
        private final short ENTITY_UUID;
    }
}
