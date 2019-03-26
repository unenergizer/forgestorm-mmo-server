package com.valenguard.server.game;

import com.valenguard.server.Server;
import com.valenguard.server.game.world.item.inventory.PlayerMoveInventoryEvents;
import com.valenguard.server.game.world.task.*;
import lombok.Getter;

/**
 * Class Credit:
 * https://github.com/Wulf/crescent/blob/master/crescent/core/server/ca/live/hk12/crescent/server/CrescentServer.java
 * License:
 * https://github.com/Wulf/crescent/blob/master/LICENSE.txt
 * Modified: Robert Brown & Joseph Rugh
 */
@Getter
public class GameLoop extends Thread {

    private final PlayerMoveInventoryEvents playerMoveInventoryEvents = new PlayerMoveInventoryEvents();
    private final UpdateMovements updateMovements = new UpdateMovements();
    private final WarpManager warpManager = new WarpManager();
    private final ItemTickUpdates itemTickUpdates = new ItemTickUpdates();
    private final CombatTickUpdates combatTickUpdates = new CombatTickUpdates();
    private final EntityRehealTask entityRehealTask = new EntityRehealTask();
    private final AiEntityRespawnTimerTask aiEntityRespawnTimerTask = new AiEntityRespawnTimerTask();

    /* UPDATES PER SECOND */
    private int currentTPS;
    private long variableYieldTime, lastTime;

    public GameLoop() {
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

        Server server = Server.getInstance();

        while (server.getNetworkManager().getGameServerConnection().isRunning()) {
            startTime = System.nanoTime();

            /* ***********************
             *  !! Update Start !!
             ***********************/

            server.getCommandManager().getCommandProcessor().executeCommands();
            server.getNetworkManager().getGameServerConnection().getEventBus().gameThreadPublish();
            playerMoveInventoryEvents.processInventoryEvents();
            updateMovements.tick(numberOfTicksPassed);
            warpManager.tick(numberOfTicksPassed);
            itemTickUpdates.tick(numberOfTicksPassed);
            combatTickUpdates.tick(numberOfTicksPassed);
            entityRehealTask.tick(numberOfTicksPassed);
            server.getGameManager().gameMapTick(numberOfTicksPassed);
            aiEntityRespawnTimerTask.tick(numberOfTicksPassed);
            server.getGameManager().processPlayerQuit();
            server.getGameManager().processPlayerJoin();
            server.getNetworkManager().getOutStreamManager().sendPackets();
            server.getTradeManager().tickTime(numberOfTicksPassed);

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
                    break; // exitServer while loop
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
