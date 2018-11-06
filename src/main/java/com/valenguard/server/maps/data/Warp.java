package com.valenguard.server.maps.data;

import com.valenguard.server.entity.MoveDirection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class Warp {
    private String mapName;
    private int toX;
    @Setter
    private int toY;
    private MoveDirection moveDirection;
}
