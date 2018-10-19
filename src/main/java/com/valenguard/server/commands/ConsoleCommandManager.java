package com.valenguard.server.commands;

import com.valenguard.server.ValenguardMain;

import java.util.Scanner;

public class ConsoleCommandManager implements Runnable {

    /**
     * This will start listening to console commands.
     */
    public void start() {
        System.out.println("[ConsoleCommands] Initializing server commands...");
        new Thread(this, "ConsoleCommandManager").start();
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        String input = "";

        while (!input.equalsIgnoreCase("stop")) {
            input = scanner.next();
            ValenguardMain.getInstance().getCommandProcessor().runListeners(input, null);
        }

        ValenguardMain.getInstance().stop();
    }
}
