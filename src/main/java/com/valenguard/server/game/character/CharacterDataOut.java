package com.valenguard.server.game.character;

import lombok.Getter;

import static com.valenguard.server.util.Log.println;


@Getter
public class CharacterDataOut {

    private static final boolean PRINT_DEBUG = false;

    private final int characterId;
    private final String name;
    private final byte headTexture;
    private final byte skinColor;

    public CharacterDataOut(int characterId, String name, byte headTexture, byte skinColor) {
        this.characterId = characterId;
        this.name = name;
        this.headTexture = headTexture;
        this.skinColor = skinColor;

        println(getClass(), "ID: " + characterId, false, PRINT_DEBUG);
        println(getClass(), "Name: " + name, false, PRINT_DEBUG);
        println(getClass(), "HeadTexture: " + headTexture, false, PRINT_DEBUG);
        println(getClass(), "SkinColor: " + skinColor, false, PRINT_DEBUG);
    }
}
