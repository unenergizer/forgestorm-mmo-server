package com.valenguard.server;

import com.valenguard.server.entity.PlayerManager;
import com.valenguard.server.network.shared.ServerConstants;
import com.valenguard.server.serverupdates.UpdateMovements;
import lombok.Getter;

/**
 * Class Credit:
 * https://github.com/Wulf/crescent/blob/master/crescent/core/server/ca/live/hk12/crescent/server/CrescentServer.java
 * License:
 * https://github.com/Wulf/crescent/blob/master/LICENSE.txt
 * Modified: Robert Brown & Joseph Rugh
 */
public class ServerLoop extends Thread {

    /* UPDATES PER SECOND */
    private int currentTPS;
    private long variableYieldTime, lastTime;

    @Getter
    private UpdateMovements updateMovements = new UpdateMovements();

    public ServerLoop() {
        super("MainServerLoop");
    }

    /**
     * Grabs the current ticks per second of the server.
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

        while (ValenguardMain.getInstance().isOnline()) {
            startTime = System.nanoTime();

            /************************
             *  !! Update Start !!
             ***********************/

            // TODO: Create dynamic pulse event that all server operations listen to.
            updateMovements.updatePlayerMovement();

            // Update ping every X ticks
            if (updates % 10 == 0) {
                PlayerManager.getInstance().getPingManager().tick();
            }

            /************************
             * !! Update End !!
             ***********************/

            sync(ServerConstants.TICKS_PER_SECOND);
            //System.out.println(ConsoleLogger.INFO.toString() + "TPS: " + getCurrentTPS());

            endTime = System.nanoTime();

            updates++;
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
    private void sync(int fps) {
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
