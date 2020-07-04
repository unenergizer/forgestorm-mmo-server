package com.forgestorm.server.game.character;

import lombok.Getter;

import static com.forgestorm.server.util.Log.println;


@Getter
public class CharacterDataOut {

    private static final boolean PRINT_DEBUG = false;

    private final int characterId;
    private final String name;
    private final byte headTexture;
    private final Integer hairColor;
    private final Integer eyeColor;
    private final Integer skinColor;

    public CharacterDataOut(int characterId, String name, byte headTexture, int hairColor, int eyeColor, int skinColor) {
        this.characterId = characterId;
        this.name = name;
        this.headTexture = headTexture;
        this.hairColor = hairColor;
        this.eyeColor = eyeColor;
        this.skinColor = skinColor;

        println(getClass(), "ID: " + characterId, false, PRINT_DEBUG);
        println(getClass(), "Name: " + name, false, PRINT_DEBUG);
        println(getClass(), "HeadTexture: " + headTexture, false, PRINT_DEBUG);
        println(getClass(), "HairColor: " + hairColor, false, PRINT_DEBUG);
        println(getClass(), "EyeColor: " + eyeColor, false, PRINT_DEBUG);
        println(getClass(), "SkinColor: " + skinColor, false, PRINT_DEBUG);
    }
}