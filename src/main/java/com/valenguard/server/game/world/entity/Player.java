package com.valenguard.server.game.world.entity;

import com.valenguard.server.game.rpg.Reputation;
import com.valenguard.server.game.rpg.skills.Skills;
import com.valenguard.server.game.world.item.inventory.PlayerBag;
import com.valenguard.server.game.world.item.inventory.PlayerBank;
import com.valenguard.server.game.world.item.inventory.PlayerEquipment;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.game.world.maps.Warp;
import com.valenguard.server.network.game.shared.ClientHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.Deque;
import java.util.LinkedList;

@Getter
@Setter
public class Player extends MovingEntity {

    private final ClientHandler clientHandler;
    private final Skills skills = new Skills(this);
    private final PlayerBag playerBag = new PlayerBag(this);
    private final PlayerBank playerBank = new PlayerBank(this);
    private final PlayerEquipment playerEquipment = new PlayerEquipment(this);
    private final Reputation reputation = new Reputation(this);
    private final Deque<Location> latestMoveRequests = new LinkedList<>();

    private Warp warp;

    private long pingOutTime = 0;
    private long lastPingTime = 0;

    private int tradeUUID = -1;

    private AiEntity currentShoppingEntity;

    private byte faction;

    /**
     * The amount of time that has progressed since the player
     * has been out of combat.
     */
    private int combatIdleTime;

    public Player(final ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

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

    @Override
    public void gameMapDeregister() {
        super.gameMapDeregister();
        getLatestMoveRequests().clear();
    }
}
