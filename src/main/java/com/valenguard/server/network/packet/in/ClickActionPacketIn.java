package com.valenguard.server.network.packet.in;

import com.valenguard.server.game.entity.Appearance;
import com.valenguard.server.game.entity.Entity;
import com.valenguard.server.game.entity.ItemStackDrop;
import com.valenguard.server.game.entity.StationaryEntity;
import com.valenguard.server.game.maps.GameMap;
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

        GameMap gameMap = packetData.getPlayer().getGameMap();

        if (gameMap.getStationaryEntitiesList().get(packetData.getENTITY_UUID()) != null) {
            // Click received, lets perform our skill!
            StationaryEntity clickedOnEntity = gameMap.getStationaryEntitiesList().get(packetData.getENTITY_UUID());
            if (clickedOnEntity != null) changeEntityAppearance(clickedOnEntity, gameMap);
        } else if (gameMap.getItemStackDropList().get(packetData.getENTITY_UUID()) != null) {
            // Click received, lets pick up the item!
            ItemStackDrop itemStackDrop = gameMap.getItemStackDropList().get(packetData.getENTITY_UUID());

            // Check inventory for being full first
            if (!packetData.getPlayer().getPlayerBag().isBagFull()) {

                // Despawn the item
                gameMap.queueItemStackDropDespawn(itemStackDrop);

                // Send the player the item
                packetData.getPlayer().giveItemStack(itemStackDrop.getItemStack());
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
