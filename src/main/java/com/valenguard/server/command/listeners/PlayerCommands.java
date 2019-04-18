package com.valenguard.server.command.listeners;

import com.valenguard.server.Server;
import com.valenguard.server.command.Command;
import com.valenguard.server.command.CommandSource;
import com.valenguard.server.command.IncompleteCommand;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.packet.out.EntityHealPacketOut;

public class PlayerCommands {

    @Command(base = "heal", argLenReq = 1)
    @IncompleteCommand(missing = "heal <packetReceiver>")
    public void healPlayer(CommandSource commandSource, String[] args) {

        short playerId = 0;
        boolean findById = true;

        try {

            playerId = Short.parseShort(args[0]);

        } catch (NumberFormatException e) {
            findById = false;
        }

        Player player;
        if (findById) {
            player = Server.getInstance().getGameManager().findPlayer(playerId);
            if (player == null) {
                commandSource.sendMessage("Could not find a packetReceiver for id: " + playerId);
                return;
            }
        } else {
            player = Server.getInstance().getGameManager().findPlayer(args[0]);
            if (player == null) {
                commandSource.sendMessage("Could not find a packetReceiver for name: " + args[0]);
                return;
            }
        }

        commandSource.sendMessage("Healed packetReceiver " + player.getName());

        final Player sendPlayer = player;
        player.getGameMap().getPlayerController().forAllPlayers(anyPlayer ->
                new EntityHealPacketOut(anyPlayer, sendPlayer, sendPlayer.getMaxHealth() - sendPlayer.getCurrentHealth()).sendPacket());
        player.setCurrentHealth(player.getMaxHealth());
    }

}
