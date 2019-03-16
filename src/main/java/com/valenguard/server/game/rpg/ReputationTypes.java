package com.valenguard.server.game.rpg;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReputationTypes {
    DIGNIFIED(3501, 7500, EntityAlignment.FRIENDLY),
    CHERISHED(1501, 3500, EntityAlignment.FRIENDLY),
    FRIENDLY(501, 1500, EntityAlignment.FRIENDLY),

    NEUTRAL(-500, 500, EntityAlignment.NEUTRAL),

    NUISANCE(-501, -1500, EntityAlignment.HOSTILE),
    HOSTILE(-1501, -3500, EntityAlignment.HOSTILE),
    ABOMINATION(-3501, -7500, EntityAlignment.HOSTILE);

    public static ReputationTypes getReputationType(short reputation) {
        for (ReputationTypes reputationTypes : values()) {
            if ((reputation > reputationTypes.minRange && reputation < reputationTypes.maxRange) ||
                    (reputation < reputationTypes.minRange && reputation > reputationTypes.maxRange))
                return reputationTypes;
        }
        return null;
    }

    private int minRange;
    private int maxRange;
    private EntityAlignment entityAlignment;
}
