package com.valenguard.server.game.rpg.skills;

public enum SkillOpcodes {

    MINING,
    MELEE;

    public byte getSkillOpcodeByte() {
        return (byte) this.ordinal();
    }

}
