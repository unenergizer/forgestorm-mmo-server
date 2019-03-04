package com.valenguard.server;

import com.valenguard.server.game.GameConstants;
import com.valenguard.server.game.inventory.PlayerInventoryEvents;
import com.valenguard.server.game.task.CombatTickUpdates;
import com.valenguard.server.game.task.ItemTickUpdates;
import com.valenguard.server.game.task.UpdateMovements;
import com.valenguard.server.game.task.WarpManager;
import com.valenguard.server.network.ServerConnection;
import lombok.Getter;

/**
 * Class Credit:
 * https://github.com/Wulf/crescent/blob/master/crescent/core/server/ca/live/hk12/crescent/server/CrescentServer.java
 * License:
 * https://github.com/Wulf/crescent/blob/master/LICENSE.txt
 * Modified: Robert Brown & Joseph Rugh
 */
public class GameLoop extends Thread {

    /* UPDATES PER SECOND */
    private int currentTPS;
    private long variableYieldTime, lastTime;

    @Getter
    private PlayerInventoryEvents playerInventoryEvents = new PlayerInventoryEvents(); // TODO: MOVE

    @Getter
    private final UpdateMovements updateMovements = new UpdateMovements(); //TODO: MOVE

    private final WarpManager warpManager = new WarpManager(); // TODO: MOVE

    @Getter
    private final ItemTickUpdates itemTickUpdates = new ItemTickUpdates();

    private final CombatTickUpdates combatTickUpdates = new CombatTickUpdates();


    GameLoop() {
        super("GameThread");
    }

    /**
     * Grabs the current ticks per second of the server.
     * <p>
     * TODO: USE FOR COMMANDS to SEND TO SERVER/CLIENT
     *
     * @return The current server ticks per second.
     */
    public int getCurrentTPS() {
        return currentTPS;
    }

    @Override
    public void run() {
        int updates = 0;
        //float timeStep = 1.0F / ServerConstants.TICKS_PER_SECOND;
        long time = 0;
        long nanoSecond = 1000000000; // 1 second -> 1000 ms -> 1000*1,000,000 ns
        long startTime, endTime;

        long numberOfTicksPassed = 0;

        while (ServerConnection.getInstance().isRunning()) {
            startTime = System.nanoTime();

            /* ***********************
             *  !! Update Start !!
             ***********************/

            ValenguardMain valenguardMain = ValenguardMain.getInstance();

            valenguardMain.getCommandProcessor().executeCommands();
            ServerConnection.getInstance().getEventBus().gameThreadPublish();
            playerInventoryEvents.processInventoryEvents();
            updateMovements.updateEntityMovement();
            warpManager.warpPlayers();
            itemTickUpdates.tickItemsDespawn();
            combatTickUpdates.tickCombat(numberOfTicksPassed);
            valenguardMain.getGameManager().gameMapTick(numberOfTicksPassed);
            valenguardMain.getAiEntityRespawnTimer().tickRespawnTime();
            valenguardMain.getGameManager().processPlayerJoin();
            valenguardMain.getOutStreamManager().sendPackets();
            valenguardMain.getTradeManager().tickTime(numberOfTicksPassed);


            /* ***********************
             * !! Update End !!
             ***********************/

            sync(GameConstants.TICKS_PER_SECOND);

            endTime = System.nanoTime();

            updates++;
            numberOfTicksPassed++;

            time += endTime - startTime;
            if (time >= nanoSecond) {
                time -= nanoSecond;
                currentTPS = updates;
                updates = 0;
            }
        }
    }

    /**
     * Author: kappa (On the LWJGL Forums)
     * An accurate sync method that adapts automatically
     * to the system it runs on to provide reliable results.
     *
     * @param fps The desired frame rate, in frames per second.
     */
    private void sync(@SuppressWarnings("SameParameterValue") int fps) {
        if (fps <= 0) return;

        long sleepTime = 1000000000 / fps; // nanoseconds to sleep this frame
        long yieldTime = Math.min(sleepTime, variableYieldTime + sleepTime % (1000 * 1000));
        long overSleep = 0; // time the sync goes over by

        try {
            while (true) {
                long t = System.nanoTime() - lastTime;

                if (t < sleepTime - yieldTime) {
                    Thread.sleep(1);
                } else if (t < sleepTime) {
                    // burn the last few CPU cycles to ensure accuracy
                    Thread.yield();
                } else {
                    overSleep = t - sleepTime;
                    break; // exit while loop
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lastTime = System.nanoTime() - Math.min(overSleep, sleepTime);

            // auto tune the time sync should yield
            if (overSleep > variableYieldTime) {
                // increase by 200 microseconds (1/5 a ms)
                variableYieldTime = Math.min(variableYieldTime + 200 * 1000, sleepTime);
            } else if (overSleep < variableYieldTime - 200 * 1000) {
                // decrease by 2 microseconds
                variableYieldTime = Math.max(variableYieldTime - 2 * 1000, 0);
            }
        }
    }
}
