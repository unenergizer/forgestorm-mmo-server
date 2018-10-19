package com.valenguard.server.commands;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.Channel;
import java.util.HashMap;
import java.util.Map;

public class CommandProcessor {

    private final Map<CommandListener, Map<String[], Method>> commandListeners = new HashMap<>();

    public void addListener(CommandListener commandListener) {
        Map<String[], Method> commandMethods = new HashMap<>();

        for (Method method : commandListener.getClass().getMethods()) {
            Command[] commands = method.getAnnotationsByType(Command.class);

            if (commands.length == 0) continue;
            if (commands.length > 1)
                throw new RuntimeException("The annotation @Command may only be used once per method.");

            Class<?>[] parameters = method.getParameterTypes();

            if (parameters.length != 1)
                throw new RuntimeException("The length of the parameters of a method with the @Command annotation must be 1.");

            if (!parameters[0].equals(Channel.class)) {
                throw new RuntimeException(
                        "The parameter for a method with the @Command annotation must be of type: " + Channel.class);
            }

            commandMethods.put(commands[0].getCommands(), method);
        }

        commandListeners.put(commandListener, commandMethods);
    }

    public boolean runListeners(String command, Channel playerChannel) {
        for (CommandListener commandListener : commandListeners.keySet()) {
            Map<String[], Method> commands = commandListeners.get(commandListener);
            for (String[] methodCommands : commands.keySet()) {
                for (String methodCmd : methodCommands) {
                    if (methodCmd.equals(command)) {
                        try {
                            commands.get(methodCommands).invoke(commandListener, playerChannel);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
