package com.valenguard.server.game.world.task.skills;

import com.valenguard.server.Server;
import com.valenguard.server.game.rpg.StationaryTypes;
import com.valenguard.server.game.rpg.skills.SkillNodeData;
import com.valenguard.server.game.world.entity.Appearance;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.entity.StationaryEntity;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.task.AbstractTask;
import com.valenguard.server.network.game.packet.out.EntityAppearancePacketOut;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ProcessMining implements AbstractTask {

    @AllArgsConstructor
    private class MiningData {
        private Player player;
        private StationaryEntity clickedEntity;
    }

    private final Random random = new Random();

    private List<MiningData> playersMining = new ArrayList<>();

    public void addPlayerToMine(Player player, StationaryEntity clickedEntity) {
        playersMining.add(new MiningData(player, clickedEntity));
    }

    @Override
    public void tick(long ticksPassed) {
        if (ticksPassed % 30 == 0) {
            Iterator<MiningData> it = playersMining.iterator();
            while (it.hasNext()) {
                if (playerMine(it.next())) {
                    it.remove();
                }
            }
            playersMining.forEach(this::playerMine);

            growBack();
        }
    }

    private void growBack() {

        Server.getInstance().getGameManager().getGameMapProcessor().getGameMaps().values().forEach(map ->
            map.getStationaryEntityController().getEntities()
                    .stream()
                    .filter(stationaryEntity -> stationaryEntity.getStationaryType() == StationaryTypes.ORE)
                    .forEach(stationaryEntity -> {

                        // TODO: read back randomization chance for coming back to life
                        // 15% chance
                        if (random.nextFloat() >= .85f) {
                            if (stationaryEntity.getBodyId() > 0) {
                                   changeEntityAppearance(stationaryEntity, (short) (stationaryEntity.getBodyId() - 1));
                            }
                        }

                    }));

    }

    private boolean playerMine(MiningData miningData) {

        Player player = miningData.player;

        // 0.0f-1.0f (20% chance)
        if (random.nextFloat() >= .8f) {

            Server server = Server.getInstance();
            SkillNodeData skillNodeData = miningData.clickedEntity.getSkillNodeData();

            int numberOfUsages = skillNodeData.getNumberOfUsages();
            // 0,1 <- still has ore left 2 empty?
            if (miningData.clickedEntity.getBodyId() - 1 ==  numberOfUsages) {
                return true; // Remove them since the skill node is out of usages
            }

            ItemStack giveItemStack = server.getDropTableManager().getItemStack(skillNodeData.getDropTableId(), 1);
            player.getPlayerBag().giveItemStack(giveItemStack, true);
            player.getSkills().MINING.addExperience(skillNodeData.getExperience());

            changeEntityAppearance(miningData.clickedEntity, (short) (miningData.clickedEntity.getBodyId() + 1));

            return true;
        }

        return false;
    }

    private void changeEntityAppearance(StationaryEntity entity, short appearanceID) {
        entity.setAppearance(new Appearance(entity, (byte) 0, new short[]{appearanceID}));

        entity.getGameMap().getPlayerController()
                .forAllPlayers(player -> new EntityAppearancePacketOut(player, entity, EntityAppearancePacketOut.BODY_INDEX).sendPacket());

    }
}
