package com.valenguard.server.game.character;

public enum CharacterClasses {

    FIGHTER,
    MAGE,
    HEALER;

    public static CharacterClasses getType(byte typeByte) {
        for (CharacterClasses type : CharacterClasses.values()) {
            if ((byte) type.ordinal() == typeByte) {
                return type;
            }
        }
        return null;
    }

    public byte getTypeByte() {
        return (byte) this.ordinal();
    }
}
