package com.valenguard.server.network.game.packet.in;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.entity.AiEntity;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.game.rpg.ShopOpcodes;
import com.valenguard.server.network.game.shared.*;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.valenguard.server.util.Log.println;

@Opcode(getOpcode = Opcodes.ENTITY_SHOPS)
public class ShopPacketIn implements PacketListener<ShopPacketIn.ShopPacket>, PacketInCancelable {


    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        ShopOpcodes npcShopOpcode = ShopOpcodes.getShopOpcode(clientHandler.readByte());
        short entityId = 0;
        short shopSlot = 0;

        println(getClass(), "Incoming shop packet!");

        if (npcShopOpcode == ShopOpcodes.START_SHOPPING) {
            entityId = clientHandler.readShort();
        } else if (npcShopOpcode == ShopOpcodes.BUY) {
            shopSlot = clientHandler.readShort();
        }

        return new ShopPacket(npcShopOpcode, entityId, shopSlot);
    }

    @Override
    public boolean sanitizePacket(ShopPacket packetData) {

        // The packetReceiver cannot move and shop at the same time.
        if (packetData.getPlayer().isEntityMoving()) {
            println(getClass(), "Tried to send shop packet but was moving.");
            packetData.getPlayer().setCurrentShoppingEntity(null);
            return false;
        }

        if (packetData.shopOpcode == ShopOpcodes.START_SHOPPING) {
            if (!packetData.getPlayer().getGameMap().getAiEntityController().containsKey(packetData.entityId))
                return false;
        }

        println(getClass(), "1");

        if (packetData.shopOpcode == ShopOpcodes.START_SHOPPING) {
            AiEntity aiEntity = (AiEntity) packetData.getPlayer().getGameMap().getAiEntityController().getEntity(packetData.entityId);
            if (aiEntity.getShopId() == -1) return false;
        }

        println(getClass(), "2");

        if (packetData.shopOpcode == ShopOpcodes.BUY || packetData.shopOpcode == ShopOpcodes.SELL) {
            return packetData.getPlayer().getCurrentShoppingEntity() != null;
        }

        println(getClass(), "Passed sanatize checks");

        return true;
    }

    @Override
    public void onEvent(ShopPacket packetData) {

        Player player = packetData.getPlayer();

        if (packetData.shopOpcode == ShopOpcodes.START_SHOPPING) {
            println(getClass(), "Started Shopping!");
            player.setCurrentShoppingEntity((AiEntity) player.getGameMap().getAiEntityController().getEntity(packetData.entityId));
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
