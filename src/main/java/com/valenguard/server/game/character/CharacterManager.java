package com.valenguard.server.game.character;

import com.valenguard.server.game.world.entity.Player;

public class CharacterManager {

    public boolean checkUniqueName(String name) {
        // TODO: check to make sure the players chosen name is unique
        return true;
    }

    public void createCharacter(Player player, String characterName, byte factionId, byte headId, byte bodyId, byte colorId) {
        // TODO: Insert into SQL and then load player defaults!

    }

    public void deleteCharacter(Player player, int characterId) {
        // TODO: Soft delete the players character / place deleted flag on character, but do NOT remove entry (for recovery purposes)
    }

    public void characterLogin(Player player, int characterId) {
        // TODO: Trigger SQL data load
    }

    public void characterLogout(Player player) {
        // TODO: Save player login information and send the character back to menu
    }
}
