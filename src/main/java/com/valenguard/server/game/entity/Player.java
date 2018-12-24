package com.valenguard.server.game.entity;

import com.valenguard.server.game.inventory.InventoryActions;
import com.valenguard.server.game.inventory.ItemStack;
import com.valenguard.server.game.inventory.PlayerBag;
import com.valenguard.server.game.inventory.PlayerEquipment;
import com.valenguard.server.game.maps.MoveDirection;
import com.valenguard.server.game.maps.Warp;
import com.valenguard.server.network.packet.out.InventoryPacketOut;
import com.valenguard.server.network.shared.ClientHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Queue;

@Getter
@Setter
public class Player extends MovingEntity {

    private ClientHandler clientHandler;
    private Queue<MoveDirection> latestMoveRequests = new LinkedList<>();

    private Warp warp;

    private long pingOutTime = 0;
    private long lastPingTime = 0;

    @Getter
    private PlayerBag playerBag = new PlayerBag();

    @Getter
    private PlayerEquipment playerEquipment = new PlayerEquipment();

    public void addDirectionToFutureQueue(MoveDirection moveDirection) {
        latestMoveRequests.add(moveDirection);
    }

    public void initEquipment() {
        playerEquipment.init();
    }

    @Override
    public void gameMapDeregister() {
        super.gameMapDeregister();
        getLatestMoveRequests().clear();
    }

    public void giveItemStack(ItemStack itemStack) {
        playerBag.addItemStack(itemStack);
        new InventoryPacketOut(this, new InventoryActions(InventoryActions.GIVE, itemStack)).sendPacket();
    }
}
