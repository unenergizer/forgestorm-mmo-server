package com.forgestorm.server.network.game.packet.in;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.rpg.ShopOpcodes;
import com.forgestorm.server.game.world.entity.AiEntity;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.network.game.shared.*;
import com.forgestorm.shared.network.game.Opcode;
import com.forgestorm.shared.network.game.Opcodes;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static com.forgestorm.server.util.Log.println;

@Opcode(getOpcode = Opcodes.ENTITY_SHOPS)
public class ShopPacketIn implements PacketListener<ShopPacketIn.ShopPacket>, PacketInCancelable {

    private static final boolean PRINT_DEBUG = false;

    @Override
    public PacketData decodePacket(ClientHandler clientHandler) {
        ShopOpcodes npcShopOpcode = ShopOpcodes.getShopOpcode(clientHandler.readByte());
        short entityId = -1;
        short shopSlot = -1;

        if (npcShopOpcode == ShopOpcodes.START_SHOPPING) {
            entityId = clientHandler.readShort();
        } else if (npcShopOpcode == ShopOpcodes.BUY) {
            shopSlot = clientHandler.readShort();
        }

        println(getClass(), "Opcode: " + npcShopOpcode, false, PRINT_DEBUG);
        println(getClass(), "EntityId: " + entityId, false, PRINT_DEBUG);
        println(getClass(), "ShopSlot: " + shopSlot, false, PRINT_DEBUG);

        return new ShopPacket(npcShopOpcode, entityId, shopSlot);
    }

    @Override
    public boolean sanitizePacket(ShopPacket packetData) {
        Player player = packetData.getClientHandler().getPlayer();

        // The player cannot move and shop at the same time.
        if (player.isEntityMoving()) {
            player.setCurrentShoppingEntity(null);
            println(getClass(), "Player moved, chancel shopping.", false, PRINT_DEBUG);
            return false;
        }

        if (packetData.shopOpcode == ShopOpcodes.START_SHOPPING) {
            if (player.getGameWorld().getAiEntityController().doesNotContainKey(packetData.entityId)) {
                println(getClass(), "Entity does not exist, can not start shopping.", false, PRINT_DEBUG);
                return false;
            }
        }

        if (packetData.shopOpcode == ShopOpcodes.START_SHOPPING) {
            AiEntity aiEntity = (AiEntity) player.getGameWorld().getAiEntityController().getEntity(packetData.entityId);
            if (aiEntity.getShopId() == -1) {
                println(getClass(), "Shop value is -1 (shop does not exist for this entity)", false, PRINT_DEBUG);
                return false;
            }
        }

        if (packetData.shopOpcode == ShopOpcodes.BUY || packetData.shopOpcode == ShopOpcodes.SELL) {
            if (player.getCurrentShoppingEntity() == null) {
                println(getClass(), "Player has not chosen an entity to shop from.", false, PRINT_DEBUG);
                return false;
            }
        }

        return true;
    }

    @Override
    public void onEvent(ShopPacket packetData) {

        Player player = packetData.getClientHandler().getPlayer();

        if (packetData.shopOpcode == ShopOpcodes.START_SHOPPING) {
            println(getClass(), "Setting player shop entity.", false, PRINT_DEBUG);
            player.setCurrentShoppingEntity((AiEntity) player.getGameWorld().getAiEntityController().getEntity(packetData.entityId));
        } else if (packetData.shopOpcode == ShopOpcodes.BUY) {
            println(getClass(), "Player is buying an item.", false, PRINT_DEBUG);
            ServerMain.getInstance().getEntityShopManager()
                    .playerBuyItemStack(player.getCurrentShoppingEntity().getShopId(), packetData.shopSlot, player);
        } else if (packetData.shopOpcode == ShopOpcodes.STOP_SHOPPING) {
            println(getClass(), "Player is no longer shopping.", false, PRINT_DEBUG);
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
