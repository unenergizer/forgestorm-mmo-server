package com.valenguard.server.game.entity;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.inventory.InventoryActions;
import com.valenguard.server.game.inventory.ItemStack;
import com.valenguard.server.game.inventory.PlayerBag;
import com.valenguard.server.game.inventory.PlayerEquipment;
import com.valenguard.server.game.maps.MoveDirection;
import com.valenguard.server.game.maps.Warp;
import com.valenguard.server.game.rpg.Skills;
import com.valenguard.server.network.packet.out.EntityAppearancePacketOut;
import com.valenguard.server.network.packet.out.InventoryPacketOut;
import com.valenguard.server.network.shared.ClientHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Queue;

@Getter
@Setter
public class Player extends MovingEntity {

    private Skills skills = new Skills(this);

    private ClientHandler clientHandler;
    private Queue<MoveDirection> latestMoveRequests = new LinkedList<>();

    private Warp warp;

    private long pingOutTime = 0;
    private long lastPingTime = 0;

    private PlayerBag playerBag = new PlayerBag();

    private PlayerEquipment playerEquipment = new PlayerEquipment();

    private int tradeUUID = -1;

    public void addDirectionToFutureQueue(MoveDirection moveDirection) {
        latestMoveRequests.add(moveDirection);
    }

    public void initEquipment() {
        playerEquipment.init(this);
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

    public void removeItemStack(byte slotIndex) {
        playerBag.removeItemStack(slotIndex);
        new InventoryPacketOut(this, new InventoryActions(InventoryActions.REMOVE, slotIndex)).sendPacket();
    }

    public void setHeadAppearance(short headTextureId) {
        getAppearance().getTextureIds()[Appearance.HEAD] = headTextureId;
        ValenguardMain.getInstance().getGameManager().sendToAllButPlayer(this, clientHandler ->
                new EntityAppearancePacketOut(clientHandler.getPlayer(), this, EntityAppearancePacketOut.HEAD_INDEX).sendPacket());
    }

    public void setBodyAppearance(short bodyTextureId) {
        getAppearance().getTextureIds()[Appearance.BODY] = bodyTextureId;
        ValenguardMain.getInstance().getGameManager().sendToAllButPlayer(this, clientHandler ->
                new EntityAppearancePacketOut(clientHandler.getPlayer(), this, EntityAppearancePacketOut.BODY_INDEX).sendPacket());
    }

    public void setHelmAppearance(short helmTextureId) {
        getAppearance().getTextureIds()[Appearance.HELM] = helmTextureId;
        ValenguardMain.getInstance().getGameManager().sendToAllButPlayer(this, clientHandler ->
                new EntityAppearancePacketOut(clientHandler.getPlayer(), this, EntityAppearancePacketOut.HELM_INDEX).sendPacket());
    }

    public void setArmorAppearance(short armorTextureId) {
        getAppearance().getTextureIds()[Appearance.ARMOR] = armorTextureId;
        ValenguardMain.getInstance().getGameManager().sendToAllButPlayer(this, clientHandler ->
                new EntityAppearancePacketOut(clientHandler.getPlayer(), this, EntityAppearancePacketOut.ARMOR_INDEX).sendPacket());
    }
}
