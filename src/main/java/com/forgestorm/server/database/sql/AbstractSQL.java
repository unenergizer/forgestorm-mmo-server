package com.forgestorm.server.database.sql;

import com.forgestorm.server.database.CharacterSaveProgressType;
import com.forgestorm.server.network.game.shared.ClientHandler;

public interface AbstractSQL {

    void loadSQL(ClientHandler clientHandler);

    void saveSQL(ClientHandler clientHandler, CharacterSaveProgressType saveProgressType);

}
