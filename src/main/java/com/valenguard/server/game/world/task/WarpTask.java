package com.valenguard.server.game.world.task;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.entity.Player;
import com.valenguard.server.util.Log;

public class WarpTask implements AbstractTask {

    private static final boolean PRINT_DEBUG = false;

    @Override
    public void tick(long ticksPassed) {
        Server.getInstance().getGameManager().forAllPlayersFiltered(this::warpPlayer, player ->
                player.getWarp() != null && !player.isEntityMoving());
    }

    private void warpPlayer(Player player) {
        Log.println(getClass(), "===[ WARP ]========================", false, PRINT_DEBUG);
        Log.println(getClass(), "GameMap: " + player.getCurrentMapLocation().getMapName(), false, PRINT_DEBUG);
        Log.println(getClass(), "CLx: " + player.getCurrentMapLocation().getX(), false, PRINT_DEBUG);
        Log.println(getClass(), "CLy: " + player.getCurrentMapLocation().getY(), false, PRINT_DEBUG);
        Log.println(getClass(), "FLx: " + player.getFutureMapLocation().getX(), false, PRINT_DEBUG);
        Log.println(getClass(), "FLy: " + player.getFutureMapLocation().getY(), false, PRINT_DEBUG);
        Log.println(getClass(), "DRx: " + player.getRealX(), false, PRINT_DEBUG);
        Log.println(getClass(), "DRy: " + player.getRealY(), false, PRINT_DEBUG);

        Server.getInstance().getTradeManager().ifTradeExistCancel(player, "[Server] Trade canceled. Player warping.");
        Server.getInstance().getGameManager().getGameMapProcessor().playerSwitchGameMap(player);
    }
}
