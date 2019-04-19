package com.valenguard.server.command.listeners;

import com.valenguard.server.Server;
import com.valenguard.server.command.Command;
import com.valenguard.server.command.CommandSource;
import com.valenguard.server.command.IncompleteCommand;
import com.valenguard.server.game.world.entity.AiEntity;
import com.valenguard.server.game.world.entity.Player;

public class EntityCommands {

    @Command(base = "summon", argLenReq = 1)
    @IncompleteCommand(missing = "summon <entity>")
    public void summon(CommandSource commandSource, String[] args) {

        if (commandSource.getPlayer() == null) {
            commandSource.sendMessage("Only players can send this command.");
            return;
        }

        Player player = commandSource.getPlayer();
        int entityId;
        try {
            entityId = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            commandSource.sendMessage("Invalid Id number for entity.");
            return;
        }

        AiEntity aiEntity = Server.getInstance().getAiEntityDataManager().generateEntity(entityId, player.getCurrentMapLocation());

        player.getGameMap().getAiEntityController().queueEntitySpawn(aiEntity);

        commandSource.sendMessage("Spawning: " + aiEntity);
    }
}
