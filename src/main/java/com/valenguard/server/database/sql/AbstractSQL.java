package com.valenguard.server.database.sql;

import com.valenguard.server.database.CharacterSaveProgressType;
import com.valenguard.server.network.game.shared.ClientHandler;

public interface AbstractSQL {

    void loadSQL(ClientHandler clientHandler);

    void saveSQL(ClientHandler clientHandler, CharacterSaveProgressType saveProgressType);

}
