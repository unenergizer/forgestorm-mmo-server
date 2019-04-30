package com.valenguard.server.game.world.entity;

import com.valenguard.server.game.PlayerConstants;
import com.valenguard.server.game.abilities.WaitingAbility;
import com.valenguard.server.game.character.CharacterClasses;
import com.valenguard.server.game.character.CharacterGenders;
import com.valenguard.server.game.character.CharacterRaces;
import com.valenguard.server.game.rpg.Reputation;
import com.valenguard.server.game.rpg.skills.Skills;
import com.valenguard.server.game.world.item.inventory.PlayerBag;
import com.valenguard.server.game.world.item.inventory.PlayerBank;
import com.valenguard.server.game.world.item.inventory.PlayerEquipment;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.game.world.maps.MoveDirection;
import com.valenguard.server.game.world.maps.Warp;
import com.valenguard.server.network.game.packet.out.ChatMessagePacketOut;
import com.valenguard.server.network.game.packet.out.EntityHealPacketOut;
import com.valenguard.server.network.game.packet.out.MovingEntityTeleportPacketOut;
import com.valenguard.server.network.game.shared.ClientHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static com.valenguard.server.util.Log.println;

@Getter
@Setter
public class Player extends MovingEntity {

    private final ClientHandler clientHandler;
    private Integer characterDatabaseId;

    private boolean loggedInGameWorld = false;

    private final Skills skills = new Skills(this);
    private final PlayerBag playerBag = new PlayerBag(this);
    private final PlayerBank playerBank = new PlayerBank(this);
    private final PlayerEquipment playerEquipment = new PlayerEquipment(this);
    private final Reputation reputation = new Reputation(this);
    private final Deque<Location> latestMoveRequests = new LinkedList<>();

    private CharacterClasses characterClass;
    private CharacterGenders characterGender;
    private CharacterRaces characterRace;

    private Warp warp;

    private long pingOutTime = 0;
    private long lastPingTime = 0;

    private int tradeUUID = -1;

    private AiEntity currentShoppingEntity;

    private byte faction;

    /**
     * The amount of time that has progressed since the player
     * has been out of active.
     */
    private int combatIdleTime;

    private boolean isBankOpen;

    private Map<Short, WaitingAbility> queuedAbilities = new HashMap<>();

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

    public void killPlayer() {
        Location teleportLocation = new Location(PlayerConstants.RESPAWN_LOCATION);
        MoveDirection facingDirection = PlayerConstants.SPAWN_FACING_DIRECTION;

        // Check to see if the packetReceiver needs to change maps!
        if (!getGameMap().isGraveYardMap()) {
            println(getClass(), "Warping packetReceiver to graveyard map!");

            // Warp packetReceiver to graveyard
            setWarp(new Warp(teleportLocation, facingDirection));
        } else {
            println(getClass(), "Teleporting packetReceiver to graveyard!");

            // Teleport packetReceiver
            getLatestMoveRequests().clear();
            gameMapRegister(new Warp(teleportLocation, facingDirection));

            // Send all players in map the teleport packet
            getGameMap().getPlayerController().forAllPlayers(player -> new MovingEntityTeleportPacketOut(player, this, teleportLocation, facingDirection).sendPacket());

            // Send other players info about the reheal (if they are still on the same map)
            getGameMap().getPlayerController().forAllPlayers(player -> new EntityHealPacketOut(player, this, getMaxHealth() - getCurrentHealth()).sendPacket());
        }

        // Reheal Player
        setCurrentHealth(getMaxHealth());

        new ChatMessagePacketOut(this, "You died! Respawning you in graveyard!").sendPacket();
    }
}
