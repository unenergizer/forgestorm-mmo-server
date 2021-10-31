package com.forgestorm.server.game.world.entity;

import com.forgestorm.server.game.ChatChannelType;
import com.forgestorm.server.game.PlayerConstants;
import com.forgestorm.server.game.abilities.WaitingAbility;
import com.forgestorm.server.game.character.CharacterClasses;
import com.forgestorm.server.game.character.CharacterGenders;
import com.forgestorm.server.game.character.CharacterRaces;
import com.forgestorm.server.game.rpg.Reputation;
import com.forgestorm.server.game.rpg.skills.Skills;
import com.forgestorm.shared.game.world.item.ItemStack;
import com.forgestorm.server.game.world.item.inventory.*;
import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.shared.game.world.maps.MoveDirection;
import com.forgestorm.shared.game.world.maps.Warp;
import com.forgestorm.server.network.game.packet.out.ChatMessagePacketOutOut;
import com.forgestorm.server.network.game.packet.out.EntityHealPacketOutOut;
import com.forgestorm.server.network.game.packet.out.InventoryPacketOutOut;
import com.forgestorm.server.network.game.packet.out.MovingEntityTeleportPacketOutOut;
import com.forgestorm.server.network.game.shared.ClientHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

import static com.forgestorm.server.util.Log.println;

@Getter
@Setter
public class Player extends MovingEntity {

    private static final boolean PRINT_DEBUG = false;

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

    private int tradeUUID = -1;

    private AiEntity currentShoppingEntity;

    private byte faction;

    private boolean bypassCollision = false;

    /**
     * The amount of time that has progressed since the player
     * has been out of active.
     */
    private int combatIdleTime;

    private boolean isBankOpen;

    private Map<Short, WaitingAbility> queuedAbilities = new HashMap<>();

    /**
     * Used to determine how long a player has been AFK.
     */
    private long idleTimestamp = System.currentTimeMillis();

    public Player(final ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    public void updatePlayerIdleTimestamp(byte opcode) {
        println(getClass(), "UpdatedPlayerIdleTimestamp. Opcode received: " + opcode, false, PRINT_DEBUG);
        this.idleTimestamp = System.currentTimeMillis();
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
    public void gameWorldDeregister() {
        super.gameWorldDeregister();
        getLatestMoveRequests().clear();
    }

    public void killPlayer() {
        Location teleportLocation = new Location(PlayerConstants.RESPAWN_LOCATION);
        MoveDirection facingDirection = PlayerConstants.SPAWN_FACING_DIRECTION;

        clearCombatTargets();

        // Check to see if the player needs to change worlds!
        if (!getGameWorld().isGraveYardWorld()) {
            println(getClass(), "Warping " + getName() + " to graveyard world!", false, PRINT_DEBUG);

            // Warp player to graveyard
            setWarp(new Warp(teleportLocation, facingDirection));
        } else {
            println(getClass(), "Teleporting " + getName() + " to graveyard!", false, PRINT_DEBUG);

            // Teleport packetReceiver
            getLatestMoveRequests().clear();
            gameWorldRegister(new Warp(teleportLocation, facingDirection));

            // Send all players in world the teleport packet
            getGameWorld().getPlayerController().forAllPlayers(player -> new MovingEntityTeleportPacketOutOut(player, this, teleportLocation, facingDirection).sendPacket());

            // Send other players info about the reheal (if they are still on the same world)
            getGameWorld().getPlayerController().forAllPlayers(player -> new EntityHealPacketOutOut(player, this, getMaxHealth() - getCurrentHealth()).sendPacket());
        }

        // Reheal Player
        setCurrentHealth(getMaxHealth());

        new ChatMessagePacketOutOut(this, ChatChannelType.COMBAT, "[RED]You died! Respawning you in graveyard!").sendPacket();
    }

    public List<InventorySlot> getAllGoldSlots() {
        List<InventorySlot> bagSlots = playerBag.getGoldSlots();
        List<InventorySlot> hotBarSlots = playerHotBar.getGoldSlots();
        bagSlots.addAll(hotBarSlots);
        return bagSlots;
    }

    public void give(ItemStack itemStack, boolean sendPacket) {
        InventorySlot slot = playerBag.findEmptySlot();

        if (slot == null) slot = playerHotBar.findEmptySlot();
        if (slot == null) throw new RuntimeException("The inventory size should be checked before giving items.");

        slot.setItemStack(itemStack);
        if (sendPacket) {
            new InventoryPacketOutOut(this, new InventoryActions()
                    .set(slot.getInventory().getInventoryType(), slot.getSlotIndex(), itemStack)).sendPacket();
        }
    }
}
