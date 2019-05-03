package com.valenguard.server.game.character;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CharacterDataOut {
    private final byte characterId;
    private final String name;
    private final short headId;
    private final byte colorId;
}
