package com.valenguard.server.game.world.entity;

import com.valenguard.server.game.PlayerConstants;
import com.valenguard.server.game.abilities.WaitingAbility;
import com.valenguard.server.game.character.CharacterClasses;
import com.valenguard.server.game.character.CharacterGenders;
import com.valenguard.server.game.character.CharacterRaces;
import com.valenguard.server.game.rpg.Reputation;
import com.valenguard.server.game.rpg.skills.Skills;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.item.inventory.*;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.game.world.maps.MoveDirection;
import com.valenguard.server.game.world.maps.Warp;
import com.valenguard.server.network.game.packet.out.ChatMessagePacketOut;
import com.valenguard.server.network.game.packet.out.EntityHealPacketOut;
import com.valenguard.server.network.game.packet.out.InventoryPacketOut;
import com.valenguard.server.network.game.packet.out.MovingEntityTeleportPacketOut;
import com.valenguard.server.network.game.shared.ClientHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import static com.valenguard.server.util.Log.println;

@Getter
@Setter
public class Player extends MovingEntity {

    private final ClientHandler clientHandler;

    private boolean loggedInGameWorld = false;

    private final Skills skills = new Skills(this);
    private final PlayerBag playerBag = new PlayerBag(this);
    private final PlayerBank playerBank = new PlayerBank(this);
    private final PlayerEquipment playerEquipment = new PlayerEquipment(this);
    private final PlayerHotBar playerHotBar = new PlayerHotBar(this);
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

        clearCombatTargets();

        // Check to see if the player needs to change maps!
        if (!getGameMap().isGraveYardMap()) {
            println(getClass(), "Warping player to graveyard map!");

            // Warp player to graveyard
            setWarp(new Warp(teleportLocation, facingDirection));
        } else {
            println(getClass(), "Teleporting player to graveyard!");

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

    public List<InventorySlot> getAllGoldSlots() {
        List<InventorySlot> bagSlots = playerBag.getGoldSlots();
        List<InventorySlot> hotBarSlots = playerHotBar.getGoldSlots();
        bagSlots.addAll(hotBarSlots);
        return bagSlots;
    }

    public void give(ItemStack itemStack, boolean sendPacket) {
        InventorySlot slot = null;

        slot = playerBag.findEmptySlot();
        if (slot == null) {
            slot = playerHotBar.findEmptySlot();
        }

        if (slot == null) {
            throw new RuntimeException("The inventory size should be checked before giving items.");
        }

        slot.setItemStack(itemStack);
        if (sendPacket) {
            new InventoryPacketOut(this, new InventoryActions()
                    .set(slot.getInventory().getInventoryType(), slot.getSlotIndex(), itemStack)).sendPacket();
        }
    }
}
