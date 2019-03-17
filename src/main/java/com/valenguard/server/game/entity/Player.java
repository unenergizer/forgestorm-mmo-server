package com.valenguard.server.game.entity;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.inventory.*;
import com.valenguard.server.game.maps.Location;
import com.valenguard.server.game.maps.Warp;
import com.valenguard.server.game.rpg.Reputation;
import com.valenguard.server.game.rpg.skills.Skills;
import com.valenguard.server.network.packet.out.EntityAppearancePacketOut;
import com.valenguard.server.network.packet.out.InventoryPacketOut;
import com.valenguard.server.network.shared.ClientHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.Deque;
import java.util.LinkedList;

@Getter
@Setter
public class Player extends MovingEntity {

    private Skills skills = new Skills(this);

    private ClientHandler clientHandler;
    private Deque<Location> latestMoveRequests = new LinkedList<>();

    private Warp warp;

    private long pingOutTime = 0;
    private long lastPingTime = 0;

    private PlayerBag playerBag = new PlayerBag();

    private PlayerEquipment playerEquipment = new PlayerEquipment();

    private int tradeUUID = -1;

    private AiEntity currentShoppingEntity;

    private byte faction;

    private Reputation reputation = new Reputation(this);

    /**
     * The amount of time that has progressed since the player
     * has been out of combat.
     */
    private int combatIdleTime;

    /**
     * If the player is assigned or designed (null) an entity
     * then their combatIdleTime needs to be reset.
     */
    @Override
    public void setTargetEntity(MovingEntity movingEntity) {
        super.setTargetEntity(movingEntity);
        combatIdleTime = 0;
    }

    public void addFutureMoveToQueue(Location moveLocation) {
        latestMoveRequests.add(moveLocation);
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

    public void removeItemStackFromBag(byte slotIndex) {
        playerBag.removeItemStack(slotIndex);
        new InventoryPacketOut(this, new InventoryActions(InventoryActions.REMOVE, slotIndex)).sendPacket();
    }

    public void setItemStack(byte slotIndex, ItemStack itemStack) {
        playerBag.setItemStack(slotIndex, itemStack);
        new InventoryPacketOut(this, new InventoryActions(InventoryActions.SET, slotIndex, itemStack)).sendPacket();
    }

    public void moveItemStackInBag(byte fromPosition, byte toPosition) {
        playerBag.moveItemStacks(fromPosition, toPosition);
        new InventoryPacketOut(this, new InventoryActions(
                InventoryActions.MOVE,
                InventoryType.BAG_1,
                InventoryType.BAG_1,
                fromPosition,
                toPosition)).sendPacket();
    }

    public ItemStackSlotData getGold() {
        return playerBag.getGold();
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
