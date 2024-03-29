package com.forgestorm.server.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SecondaryUserGroups {
    CONTENT_DEVELOPER((byte) 5);

    private final byte userGroupId;
}
