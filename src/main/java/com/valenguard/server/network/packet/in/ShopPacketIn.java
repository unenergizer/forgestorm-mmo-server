package com.valenguard.server.network.packet.in;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.entity.EntityType;
import com.valenguard.server.game.entity.MovingEntity;
import com.valenguard.server.game.entity.NPC;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.rpg.ShopOpcodes;
import com.valenguard.server.network.shared.*;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Opcode(getOpcode = Opcodes.NPC_SHOPS)
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

        // The player cannot move and shop at the same time.
        if (packetData.getPlayer().isEntityMoving()) {
            packetData.getPlayer().setCurrentShoppingEntity(null);
            return false;
        }

        if (packetData.shopOpcode == ShopOpcodes.START_SHOPPING) {
            MovingEntity movingEntity = packetData.getPlayer().getGameMap().getAiEntityMap().get(packetData.entityId);
            if (movingEntity.getEntityType() != EntityType.NPC) return false;
            if (((NPC) movingEntity).getShopId() == -1) return false;
        }

        if (packetData.shopOpcode == ShopOpcodes.BUY || packetData.shopOpcode == ShopOpcodes.SELL) {
            if (packetData.getPlayer().getCurrentShoppingEntity() == null) return false;
        }

        return true;
    }

    @Override
    public void onEvent(ShopPacket packetData) {

        Player player = packetData.getPlayer();

        if (packetData.shopOpcode == ShopOpcodes.START_SHOPPING) {
            player.setCurrentShoppingEntity((NPC) player.getGameMap().getAiEntityMap().get(packetData.entityId));
        } else if (packetData.shopOpcode == ShopOpcodes.BUY) {
            ValenguardMain.getInstance().getEntityShopManager()
                    .buyItem(player.getCurrentShoppingEntity().getShopId(), packetData.shopSlot, player);
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
