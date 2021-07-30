package com.forgestorm.server.game.world.task;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.game.world.maps.DoorManager;

import java.util.Iterator;

public class DoorTimerTask implements AbstractTask {

    @Override
    public void tick(long ticksPassed) {

        DoorManager doorManager = ServerMain.getInstance().getDoorManager();

        for (Iterator<DoorManager.DoorInfo> iterator = doorManager.getDoorOpenList().iterator(); iterator.hasNext(); ) {
            DoorManager.DoorInfo doorInfo = iterator.next();
            int timeLeft = doorInfo.getTimeLeftTillAutoClose() - 1;

            if (timeLeft <= 0) {
                doorManager.serverForceCloseDoor(iterator, doorInfo);
            } else {
                doorInfo.setTimeLeftTillAutoClose(timeLeft);
            }
        }
    }

}
