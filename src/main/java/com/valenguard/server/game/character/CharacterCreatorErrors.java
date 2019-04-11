package com.valenguard.server.game.character;

public enum CharacterCreatorErrors {

    NAME_TAKEN;

    public static CharacterCreatorErrors getCharacterErrorType(byte characterErrors) {
        for (CharacterCreatorErrors error : CharacterCreatorErrors.values()) {
            if ((byte) error.ordinal() == characterErrors) {
                return error;
            }
        }
        return null;
    }

    public byte getCharacterErrorTypeByte() {
        return (byte) this.ordinal();
    }
}
