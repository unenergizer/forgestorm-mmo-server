package com.valenguard.server.game.rpg.skills;

public enum SkillOpcodes {

    MINING,
    MELEE;

    public static SkillOpcodes getSkillOpcode(byte entityTypeByte) {
        for (SkillOpcodes skillOpcode : SkillOpcodes.values()) {
            if ((byte) skillOpcode.ordinal() == entityTypeByte) {
                return skillOpcode;
            }
        }
        return null;
    }

    public byte getSkillOpcodeByte() {
        return (byte) this.ordinal();
    }

}