package com.forgestorm.server.game.world.entity;

import com.forgestorm.server.game.rpg.EntityAlignment;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NPC extends AiEntity {

    private byte faction;
    private int scriptId;

    public EntityAlignment getAlignmentByPlayer(Player player) {
        return player.getReputation().getAlignment(faction);
    }

    /**
     * If the alignment of an NPC is hostile towards a player,
     * set the first interaction to attack regardless of what
     * it is originally set as.
     *
     * @param player The player who is interacting with this NPC.
     * @return Return "FirstInteraction.ATTACK" if hostile or the original value otherwise.
     */
    public FirstInteraction getFirstInteractionBasedOnAlignment(Player player) {
        if (getAlignmentByPlayer(player) == EntityAlignment.HOSTILE) {
            return FirstInteraction.ATTACK; // Make player attack on first interaction.
        } else {
            return getFirstInteraction();
        }
    }

    /**
     * If the alignment of an NPC is hostile towards a player,
     * prevent the player from using the NPCs shop.
     *
     * @param player The player who is interacting with this NPC.
     * @return Return -1 (NO SHOP) if hostile or the original value otherwise.
     */
    public short getShopIdBasedOnAlignment(Player player) {
        if (getAlignmentByPlayer(player) == EntityAlignment.HOSTILE) {
            return -1; // Do not let player shop with this NPC.
        } else {
            return getShopId();
        }
    }

    /**
     * If the alignment of an NPC is hostile towards a player,
     * prevent the player from opening their bank at this NPC.
     *
     * @param player The player who is interacting with this NPC.
     * @return Return false if hostile or the original value otherwise.
     */
    public boolean getBankKeeperStatusBasedOnAlignment(Player player) {
        if (getAlignmentByPlayer(player) == EntityAlignment.HOSTILE) {
            return false; // Do not let player bank with this NPC.
        } else {
            return isBankKeeper();
        }
    }

    // TODO: Scrips, timers, tasks, questing, etc

}
