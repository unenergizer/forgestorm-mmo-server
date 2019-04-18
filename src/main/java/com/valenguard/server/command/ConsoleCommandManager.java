package com.valenguard.server.command;

import java.util.Scanner;

import static com.valenguard.server.util.Log.println;

class ConsoleCommandManager implements Runnable {

    private final CommandManager commandManager;
    private volatile boolean running = false;

    ConsoleCommandManager(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    /**
     * This will start listening to console command.
     */
    public void start() {
        println(getClass(), "Initializing server command...");
        running = true;
        new Thread(this, "ConsoleCommandManager").start();
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        String input;

        while (running) {
            input = scanner.nextLine();

            String[] content = input.split("\\s+");
            CommandState commandState;

            if (content.length == 1) {
                commandState = commandManager.getCommandProcessor().publish(new CommandSource(), content[0], new String[0]);
            } else if (content.length > 1) {
                String[] args = new String[content.length - 1];
                System.arraycopy(content, 1, args, 0, content.length - 1);
                commandState = commandManager.getCommandProcessor().publish(new CommandSource(), content[0], args);
            } else {
                continue;
            }

            CommandState.CommandType commandType = commandState.getCommandType();
            if (commandType == CommandState.CommandType.NOT_FOUND) {
                println(getClass(), "Unknown Command");
            } else if (commandType == CommandState.CommandType.SINGE_INCOMPLETE) {
                println(getClass(), "[Command] -> " + commandState.getIncompleteMessage());
            } else if (commandType == CommandState.CommandType.MULTIPLE_INCOMPLETE) {
                println(getClass(), "Suggested Alternatives:");
                for (String incompleteMsg : commandState.getMultipleIncompleteMessages()) {
                    println(getClass(), "  - [Command] -> " + incompleteMsg);
                }
            }
        }
    }

    void stop() {
        running = false;
    }
}
