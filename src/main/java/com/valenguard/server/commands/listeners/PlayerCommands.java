package com.valenguard.server.commands.listeners;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.commands.Command;
import com.valenguard.server.commands.IncompleteCommand;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.network.packet.out.EntityHealPacketOut;

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

        Player player = ValenguardMain.getInstance().getGameManager().findPlayer(playerId);

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
