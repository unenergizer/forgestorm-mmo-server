package com.valenguard.server.command.listeners;

import com.valenguard.server.Server;
import com.valenguard.server.command.Command;
import com.valenguard.server.command.IncompleteCommand;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.network.game.packet.out.EntityHealPacketOut;

import static com.valenguard.server.util.Log.println;

public class PlayerCommands {

    @Command(base = "heal", argLenReq = 1)
    @IncompleteCommand(missing = "heal <packetReceiver>")
    public void healPlayer(String[] args) {

        short playerId;

        try {

            playerId = Short.parseShort(args[0]);

        } catch (NumberFormatException e) {
            println(getClass(), "The packetReceiver must be specified by id.", true);
            return;
        }

        Player player = Server.getInstance().getGameManager().findPlayer(playerId);

        if (player == null) {
            println(getClass(), "Could not find a packetReceiver for id: " + playerId, true);
            return;
        } else {
            println(getClass(), "Healed packetReceiver " + player.getName());
        }

        player.getGameMap().getPlayerController().forAllPlayers(anyPlayer ->
                new EntityHealPacketOut(anyPlayer, player, player.getMaxHealth() - player.getCurrentHealth()).sendPacket());
        player.setCurrentHealth(player.getMaxHealth());
    }

}
