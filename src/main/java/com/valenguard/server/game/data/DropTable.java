package com.valenguard.server.game.data;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DropTable {
    private final int dropTableID;
    private Integer itemStackID;

    public DropTable(int dropTableID) {
        this.dropTableID = dropTableID;
    }
}
