package com.forgestorm.server.game.world.entity;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.world.item.ItemStack;
import com.forgestorm.server.game.world.maps.ItemStackDropEntityController;
import com.forgestorm.server.game.world.maps.Location;
import com.forgestorm.server.network.game.packet.out.ChatMessagePacketOut;
import com.forgestorm.server.util.MoveNode;
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

    private short regionStartX;
    private short regionStartY;
    private short regionEndX;
    private short regionEndY;

    public void setMovementInfo(float probabilityStill, float probabilityWalkStart) {
        randomRegionMoveGenerator = new RandomRegionMoveGenerator(this, probabilityStill, probabilityWalkStart);
    }

    public void setRegionLocations(int regionStartX, int regionStartY, int regionEndX, int regionEndY) {
        this.regionStartX = (short) Math.min(regionStartX, regionEndX);
        this.regionEndX = (short) Math.max(regionStartX, regionEndX);
        this.regionStartY = (short) Math.min(regionStartY, regionEndY);
        this.regionEndY = (short) Math.max(regionStartY, regionEndY);
    }

    public boolean isAiEntityInRegion(Location attemptLocation) {
        return attemptLocation.getX() >= regionStartX && attemptLocation.getY() >= regionStartY && attemptLocation.getX() <= regionEndX && attemptLocation.getY() <= regionEndY;
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
        new ChatMessagePacketOut(killerPlayer, "[GREEN]You gained " + this.getExpDrop() + " experience.").sendPacket();

        // Adding/Subtracting reputation
        if (this.getEntityType() == EntityType.NPC) {
            killerPlayer.getReputation().addReputation(((NPC) this).getFaction(), (short) 1000);
        }

        // Give player drop table item
        if (this.getDropTable() != null) {
            ItemStack[] itemStacks = ServerMain.getInstance().getDropTableManager().getItemStack(this.getDropTable(), 1);

            ItemStackDropEntityController itemStackDropEntityController = getGameMap().getItemStackDropEntityController();

            for (ItemStack itemStack : itemStacks) {
                if (itemStack == null) continue;
                itemStackDropEntityController.queueEntitySpawn(itemStackDropEntityController.makeItemStackDrop(
                        itemStack,
                        this.getCurrentMapLocation(),
                        (Player) killerEntity
                ));
            }
        }
    }

    public void removeAiEntity() {
        getGameMap().getAiEntityController().removeEntity(this);
    }
}
