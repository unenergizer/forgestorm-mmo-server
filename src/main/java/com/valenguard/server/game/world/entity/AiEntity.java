package com.valenguard.server.game.world.entity;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.maps.ItemStackDropEntityController;
import com.valenguard.server.game.world.maps.Location;
import com.valenguard.server.network.game.packet.out.ChatMessagePacketOut;
import com.valenguard.server.util.MoveNode;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Queue;

@Getter
@Setter
public class AiEntity extends MovingEntity {

    private int aiEntityDataID = -1;
    private int expDrop = 0;
    private Integer dropTable = 0;
    private short shopId = -1;
    private boolean isBankKeeper;
    private boolean instantRespawn = false;

    private Queue<MoveNode> moveNodes = new LinkedList<>();

    private Location defaultSpawnLocation;
    private RandomRegionMoveGenerator randomRegionMoveGenerator;

    public void setMovementInfo(float probabilityStill, float probabilityWalkStart, int bounds1x, int bounds1y, int bounds2x, int bounds2y) {
        randomRegionMoveGenerator = new RandomRegionMoveGenerator(this, probabilityStill, probabilityWalkStart, bounds1x, bounds1y, bounds2x, bounds2y);
    }

    public void killAiEntity(MovingEntity killerEntity) {
        getGameMap().getAiEntityController().queueEntityDespawn(this);

        clearCombatTargets();

        // If a AI entity kills and AI entity, do not drop ItemStack
        if (killerEntity == null) return;
        if (killerEntity.getEntityType() != EntityType.PLAYER) return;

        Player killerPlayer = (Player) killerEntity;
        new ChatMessagePacketOut(killerPlayer, "[YELLOW]You killed " + getName() + ".").sendPacket();

        // Give experience
        killerPlayer.getSkills().MELEE.addExperience(this.getExpDrop());

        // Adding/Subtracting reputation
        if (this.getEntityType() == EntityType.NPC) {
            killerPlayer.getReputation().addReputation(((NPC) this).getFaction(), (short) 1000);
        }

        // Give packetReceiver drop table item
        if (this.getDropTable() != null) {
            ItemStack itemStack = Server.getInstance().getDropTableManager().getItemStack(this.getDropTable(), 1);

            ItemStackDropEntityController itemStackDropEntityController = getGameMap().getItemStackDropEntityController();
            itemStackDropEntityController.queueEntitySpawn(itemStackDropEntityController.makeItemStackDrop(
                    itemStack,
                    this.getCurrentMapLocation(),
                    (Player) killerEntity
            ));
        }
    }
}
