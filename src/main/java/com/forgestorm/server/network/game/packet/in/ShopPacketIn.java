package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.rpg.ShopOpcodes;
import com.forgestorm.server.game.world.entity.AiEntity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.shared.*;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Opcode(getOpcode = Opcodes.ENTITY_SHOPS)
public class ShopPacketIn implements PacketListener<ShopPacketIn.ShopPacket>, PacketInCancelable {

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        ShopOpcodes npcShopOpcode = ShopOpcodes.getShopOpcode(clientHandler.readByte());
        short entityId = 0;
        short shopSlot = 0;

        if (npcShopOpcode == ShopOpcodes.START_SHOPPING) {
            entityId = clientHandler.readShort();
        } else if (npcShopOpcode == ShopOpcodes.BUY) {
            shopSlot = clientHandler.readShort();
        }

        return new ShopPacket(npcShopOpcode, entityId, shopSlot);
    }

    @Override
    public boolean sanitizePacket(ShopPacket packetData) {
        Player player = packetData.getClientHandler().getPlayer();

        // The player cannot move and shop at the same time.
        if (player.isEntityMoving()) {
            player.setCurrentShoppingEntity(null);
            return false;
        }

        if (packetData.shopOpcode == ShopOpcodes.START_SHOPPING) {
            if (player.getGameMap().getAiEntityController().doesNotContainKey(packetData.entityId))
                return false;
        }

        if (packetData.shopOpcode == ShopOpcodes.START_SHOPPING) {
            AiEntity aiEntity = (AiEntity) player.getGameMap().getAiEntityController().getEntity(packetData.entityId);
            if (aiEntity.getShopId() == -1) return false;
        }

        if (packetData.shopOpcode == ShopOpcodes.BUY || packetData.shopOpcode == ShopOpcodes.SELL) {
            return player.getCurrentShoppingEntity() != null;
        }

        return true;
    }

    @Override
    public void onEvent(ShopPacket packetData) {

        Player player = packetData.getClientHandler().getPlayer();

        if (packetData.shopOpcode == ShopOpcodes.START_SHOPPING) {
            player.setCurrentShoppingEntity((AiEntity) player.getGameMap().getAiEntityController().getEntity(packetData.entityId));
        } else if (packetData.shopOpcode == ShopOpcodes.BUY) {
            ServerMain.getInstance().getEntityShopManager()
                    .playerBuyItemStack(player.getCurrentShoppingEntity().getShopId(), packetData.shopSlot, player);
        } else if (packetData.shopOpcode == ShopOpcodes.STOP_SHOPPING) {
            player.setCurrentShoppingEntity(null);
        }
    }

    @Override
    public List<Class<? extends PacketListener>> excludeCanceling() {
        List<Class<? extends PacketListener>> excludeCanceling = new ArrayList<>();
        excludeCanceling.add(ChatMessagePacketIn.class);
        excludeCanceling.add(PingPacketIn.class);
        return excludeCanceling;
    }

    @Override
    public void onCancel(Player player) {
        player.setCurrentShoppingEntity(null);
    }

    @AllArgsConstructor
    class ShopPacket extends PacketData {
        private ShopOpcodes shopOpcode;
        private short entityId;
        private short shopSlot;
    }
}
