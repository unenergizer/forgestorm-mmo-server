package com.forgestorm.server.profile;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SecondaryUserGroups {
    CONTENT_DEVELOPER(5);

    private final int userGroupId;
}
