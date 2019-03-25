package com.valenguard.server.database.sql;

import com.valenguard.server.game.world.entity.Player;

public interface AbstractSQL {

    void loadSQL(Player player);

    void saveSQL(Player player);

}
