package com.valenguard.server.network;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NetworkSettings {
    private int loginPort;
    private String gameIp;
    private int gamePort;
}
