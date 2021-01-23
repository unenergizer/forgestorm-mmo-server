package com.forgestorm.server.game.world.task;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.MessageText;
import com.forgestorm.server.game.world.entity.Player;
import com.forgestorm.server.game.world.item.inventory.BankActions;
import com.forgestorm.server.game.world.maps.Warp;
import com.forgestorm.server.network.game.packet.out.BankManagePacketOut;
import com.forgestorm.server.network.game.packet.out.MovingEntityTeleportPacketOut;
import com.forgestorm.server.util.Log;

public class WarpTask implements AbstractTask {

    private static final boolean PRINT_DEBUG = false;

    @Override
    public void tick(long ticksPassed) {
        ServerMain.getInstance().getGameManager().forAllPlayersFiltered(this::warpPlayer, player ->
                player.getWarp() != null && !player.isEntityMoving());
    }

    private void warpPlayer(Player player) {
        Log.println(getClass(), "===[ WARP ]========================", false, PRINT_DEBUG);
        Log.println(getClass(), "GameWorld: " + player.getCurrentWorldLocation().getWorldName(), false, PRINT_DEBUG);
        Log.println(getClass(), "CLx: " + player.getCurrentWorldLocation().getX(), false, PRINT_DEBUG);
        Log.println(getClass(), "CLy: " + player.getCurrentWorldLocation().getY(), false, PRINT_DEBUG);
        Log.println(getClass(), "FLx: " + player.getFutureWorldLocation().getX(), false, PRINT_DEBUG);
        Log.println(getClass(), "FLy: " + player.getFutureWorldLocation().getY(), false, PRINT_DEBUG);
        Log.println(getClass(), "DRx: " + player.getRealX(), false, PRINT_DEBUG);
        Log.println(getClass(), "DRy: " + player.getRealY(), false, PRINT_DEBUG);

        if (player.isBankOpen()) {
            new BankManagePacketOut(player, BankActions.SERVER_CLOSE).sendPacket();
            player.setBankOpen(false);
        }

        ServerMain.getInstance().getTradeManager().ifTradeExistCancel(player, MessageText.SERVER + "Trade canceled. Player warping.");

        Warp warp = player.getWarp();
        if (warp.getWarpDestination().getGameWorld().getWorldName().equals(player.getCurrentWorldLocation().getWorldName())) {
            // Same world, just location switch
            player.getLatestMoveRequests().clear();
            player.gameWorldRegister(warp);
            player.setWarp(null);

            // Send all players in world the teleport packet
            player.getGameWorld().getPlayerController().forAllPlayers(otherPlayer -> new MovingEntityTeleportPacketOut(otherPlayer, player, warp.getWarpDestination(), warp.getDirectionToFace()).sendPacket());
        } else {
            // World switch
            ServerMain.getInstance().getGameManager().getGameWorldProcessor().playerSwitchGameWorld(player);
        }
    }
}
