package com.forgestorm.server.database;

import com.forgestorm.server.ServerMain;
import com.forgestorm.server.network.game.shared.ClientHandler;

import static com.forgestorm.server.util.Log.println;

/**
 * We use this to track and make sure everything related to the character has been saved to the database.
 */
public class CharacterSaveProgress {

    private final static boolean PRINT_DEBUG = true;
    private final ClientHandler clientHandler;
    private boolean characterSaved = false;
    private boolean inventorySaved = false;
    private boolean experienceSaved = false;
    private boolean reputationSaved = false;

    public CharacterSaveProgress(ClientHandler clientHandler) {
        this.clientHandler = clientHandler;
    }

    public void saveProgress(CharacterSaveProgressType saveProgressType) {
        switch (saveProgressType) {
            case CHARACTER_SAVED:
                characterSaved = true;
                break;
            case INVENTORY_SAVED:
                inventorySaved = true;
                break;
            case EXPERIENCE_SAVED:
                experienceSaved = true;
                break;
            case REPUTATION_SAVED:
                reputationSaved = true;
                break;
        }

        testAndSendToCharacterScreen();
    }

    private void testAndSendToCharacterScreen() {
        if (!characterSaved || !inventorySaved || !experienceSaved || !reputationSaved) return;

        println(getClass(), clientHandler.getPlayer().getName() + " saved successfully.", false, PRINT_DEBUG);

        // If the player quit the server, no need to send them to the Character screen.
        if (clientHandler.isPlayerQuitServer()) return;

        // Send the player to the character screen.
        ServerMain.getInstance().getCharacterManager().sendToCharacterScreen(clientHandler);

        // Reset booleans
        characterSaved = false;
        inventorySaved = false;
        experienceSaved = false;
        reputationSaved = false;
    }
}
