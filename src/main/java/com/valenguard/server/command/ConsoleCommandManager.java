package com.valenguard.server.command;

import com.valenguard.server.Server;
import com.valenguard.server.util.Log;

import java.util.Scanner;

class ConsoleCommandManager implements Runnable {

    private final CommandManager commandManager;

    ConsoleCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * This will start listening to console command.
     */
    public void start() {
        Log.println(getClass(), "Initializing server command...");
        new Thread(this, "ConsoleCommandManager").start();
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        String input = "";

        while (!input.equalsIgnoreCase("exitServer")) {
            input = scanner.nextLine();

            String[] content = input.split("\\s+");
            boolean commandFound = false;

            if (content.length == 1) {
                commandFound = commandManager.getCommandProcessor().publish(content[0], new String[0]);
            } else if (content.length > 1) {
                String[] args = new String[content.length - 1];
                System.arraycopy(content, 1, args, 0, content.length - 1);
                commandFound = commandManager.getCommandProcessor().publish(content[0], args);
            }

            if (!input.equals("exitServer") && !commandFound) {
                System.out.println("Unknown Command");
            }
        }

        Server.getInstance().exitServer();
    }
}
