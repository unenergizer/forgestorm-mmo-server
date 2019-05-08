package com.valenguard.server.game.world.task.skills;

import com.valenguard.server.Server;
import com.valenguard.server.game.rpg.StationaryTypes;
import com.valenguard.server.game.rpg.skills.SkillNodeData;
import com.valenguard.server.game.world.entity.Appearance;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.game.world.entity.StationaryEntity;
import com.valenguard.server.game.world.item.ItemStack;
import com.valenguard.server.game.world.maps.ItemStackDropEntityController;
import com.valenguard.server.game.world.task.AbstractTask;
import com.valenguard.server.network.game.packet.out.EntityAppearancePacketOut;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class ProcessMining implements AbstractTask {

    private final Random random = new Random();

    private Map<Player, StationaryEntity> playersMining = new HashMap<>();

    public void addPlayerToMine(Player player, StationaryEntity clickedEntity) {
        playersMining.put(player, clickedEntity);
    }

    public StationaryEntity getMiningNode(Player player) {
        return playersMining.get(player);
    }

    public void removePlayer(Player player) {
        playersMining.remove(player);
    }

    @Override
    public void tick(long ticksPassed) {
        if (ticksPassed % 30 == 0) {
            Iterator<Map.Entry<Player, StationaryEntity>> it = playersMining.entrySet().iterator();
            while (it.hasNext()) {
                if (playerMine(it.next())) {
                    it.remove();
                }
            }

            growBack();
        }
    }

    private void growBack() {

        Server.getInstance().getGameManager().getGameMapProcessor().getGameMaps().values().forEach(map ->
                map.getStationaryEntityController().getEntities()
                        .stream()
                        .filter(stationaryEntity -> stationaryEntity.getStationaryType() == StationaryTypes.ORE)
                        .filter(StationaryEntity::isUsedThisTick)
                        .forEach(stationaryEntity -> {

                            // TODO: read back randomization chance for coming back to life
                            // 15% chance
                            if (random.nextFloat() >= .85f) {
                                if (stationaryEntity.getBodyId() > 0) {
                                    changeEntityAppearance(stationaryEntity, (byte) (stationaryEntity.getBodyId() - 1));
                                }
                            }
                            stationaryEntity.setUsedThisTick(false);
                        }));

    }

    private boolean playerMine(Map.Entry<Player, StationaryEntity> miningData) {

        Player player = miningData.getKey();
        StationaryEntity clickedEntity = miningData.getValue();

        // 0.0f-1.0f (20% chance)
        if (random.nextFloat() >= .8f) {

            Server server = Server.getInstance();
            SkillNodeData skillNodeData = clickedEntity.getSkillNodeData();

            int numberOfUsages = skillNodeData.getNumberOfUsages();
            // 0,1 <- still has ore left 2 empty?
            if (clickedEntity.getBodyId() == numberOfUsages) {
                return true; // Remove them since the skill node is out of usages
            }

            ItemStack giveItemStack = server.getDropTableManager().getItemStack(skillNodeData.getDropTableId(), 1);

            if (player.getPlayerBag().isInventoryFull()) {
                ItemStackDropEntityController itemStackDropEntityController = player.getGameMap().getItemStackDropEntityController();
                itemStackDropEntityController.queueEntitySpawn(itemStackDropEntityController.makeItemStackDrop(
                        giveItemStack,
                        player.getCurrentMapLocation(),
                        player
                ));
            } else {
                player.getPlayerBag().giveItemStack(giveItemStack, true);
            }

            clickedEntity.setUsedThisTick(true);
            player.getSkills().MINING.addExperience(skillNodeData.getExperience());

            changeEntityAppearance(clickedEntity, (byte) (clickedEntity.getBodyId() + 1));

            return true;
        }

        return false;
    }

    private void changeEntityAppearance(StationaryEntity entity, byte appearanceID) {
        Appearance appearance = entity.getAppearance();
        appearance.setMonsterBodyTexture(appearanceID);

        entity.getGameMap().getPlayerController()
                .forAllPlayers(player -> new EntityAppearancePacketOut(player, entity).sendPacket());

    }
}
