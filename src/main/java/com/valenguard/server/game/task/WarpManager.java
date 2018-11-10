package com.valenguard.server.game.task;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.game.entity.Player;
import com.valenguard.server.util.Log;

public class WarpManager {

    private static final boolean PRINT_DEBUG = false;

    public void warpPlayers() {
        ValenguardMain.getInstance().getGameManager().forAllPlayersFiltered(this::warpPlayer, player ->
                player.getWarp() != null && !player.isEntityMoving());
    }

    private void warpPlayer(Player player) {
        Log.println(getClass(), "===[P WARP]========================", false, PRINT_DEBUG);
        Log.println(getClass(), "GameMap: " + player.getCurrentMapLocation().getMapName(), false, PRINT_DEBUG);
        Log.println(getClass(), "CLx: " + player.getCurrentMapLocation().getX(), false, PRINT_DEBUG);
        Log.println(getClass(), "CLy: " + player.getCurrentMapLocation().getY(), false, PRINT_DEBUG);
        Log.println(getClass(), "FLx: " + player.getFutureMapLocation().getX(), false, PRINT_DEBUG);
        Log.println(getClass(), "FLy: " + player.getFutureMapLocation().getY(), false, PRINT_DEBUG);
        Log.println(getClass(), "DRx: " + player.getRealX(), false, PRINT_DEBUG);
        Log.println(getClass(), "DRy: " + player.getRealY(), false, PRINT_DEBUG);

        ValenguardMain.getInstance().getGameManager().playerSwitchGameMap(player);
    }

}
