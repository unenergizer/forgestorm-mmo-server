package com.valenguard.server.commands;

import com.valenguard.server.ValenguardMain;
import com.valenguard.server.util.Log;

import java.util.Scanner;

public class ConsoleCommandManager implements Runnable {

    /**
     * This will start listening to console commands.
     */
    public void start() {
        Log.println(getClass(), "Initializing server commands...");
        new Thread(this, "ConsoleCommandManager").start();
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        String input = "";

        while (!input.equalsIgnoreCase("stop")) {
            input = scanner.nextLine();

            String[] content =  input.split("\\s+");
            boolean commandFound = false;

            if (content.length == 1) {
                commandFound = ValenguardMain.getInstance().getCommandProcessor().publish(content[0], new String[0]);
            } else if (content.length > 1) {
                String[] args = new String[content.length - 1];
                System.arraycopy(content, 1, args, 0, content.length - 1);
                commandFound = ValenguardMain.getInstance().getCommandProcessor().publish(content[0], args);
            }

            if (!input.equals("stop") && !commandFound) {
                System.out.println("Unknown Command");
            }
        }

        ValenguardMain.getInstance().stop();
    }
}
