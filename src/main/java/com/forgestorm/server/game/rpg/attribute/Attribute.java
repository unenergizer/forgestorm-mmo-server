package com.forgestorm.server.game.rpg.attribute;

import java.util.ArrayList;
import java.util.List;

public class Attribute extends BaseAttribute {

    private final List<RawBonus> rawBonusList = new ArrayList<>();
    private final List<FinalBonus> finalBonusList = new ArrayList<>();

    private int finalValue;

    public Attribute(int baseValue, int baseMultiplier) {
        super(baseValue, baseMultiplier);
    }

    public void addRawBonus(RawBonus rawBonus) {
        rawBonusList.add(rawBonus);
    }

    public void removeRawBonus(RawBonus rawBonus) {
        rawBonusList.remove(rawBonus);
    }

    public void addFinalBonus(FinalBonus finalBonus) {
        finalBonusList.add(finalBonus);
    }

    public void removeFinalBonus(FinalBonus finalBonus) {
        finalBonusList.remove(finalBonus);
    }

    private int calculateValue() {
        finalValue = getBaseValue();

        int rawBonusValue = 0;
        int rawBonusMultiplier = 0;

        for (RawBonus rawBonus : rawBonusList) {
            rawBonusValue += rawBonus.getBaseValue();
            rawBonusMultiplier += rawBonus.getBaseMultiplier();
        }

        int finalBonusValue = rawBonusValue;
        int finalBonusMultiplier = rawBonusMultiplier;

        for (FinalBonus finalBonus : finalBonusList) {
            finalBonusValue += finalBonus.getBaseValue();
            finalBonusMultiplier += finalBonus.getBaseMultiplier();
        }

        finalValue += finalBonusValue;
        finalValue *= (1 + finalBonusMultiplier);

        return finalValue;
    }

    public int getFinalValue() {
        return calculateValue();
    }
}
