package com.valenguard.server.command;

import lombok.AllArgsConstructor;
import org.apache.logging.log4j.core.util.ArrayUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.valenguard.server.util.Log.println;


public class CommandProcessor {

    private final Queue<PublishInfo> publishedCommands = new ConcurrentLinkedQueue<>();

    private final Map<String, Map<Integer, CommandInfo>> commandArgLimitedListeners = new HashMap<>();
    private final Map<String, Map<Integer, CommandInfo>> commandArgEndlessListeners = new HashMap<>();

    @AllArgsConstructor
    private class CommandInfo {
        private Object listener;
        private Method method;
        private int reqArgs;
        private String incompleteMsg;
        boolean endlessArguments;
    }

    void addListener(Object listener) {
        for (Method method : listener.getClass().getMethods()) {
            Command[] cmdAnnotations = method.getAnnotationsByType(Command.class);

            if (cmdAnnotations.length != 1) continue;

            Command command = cmdAnnotations[0];
            String commandBaseMessage = command.base();
            int argumentLengthRequirement = command.argLenReq();
            Class<?>[] params = method.getParameterTypes();

            String simpleName = "[" + listener.getClass().getSimpleName() + ":" + method.getName() + "]";

            checkArgument(params.length >= 1, simpleName + " Command methods require at least one parameter. The CommandSource.");
            checkArgument(params[0].equals(CommandSource.class), simpleName + " The first argument for a command method must be the CommandSource.");
            checkArgument(!(cmdAnnotations[0].argLenReq() < 0), simpleName + " The Required argument length for a command cannot be below 0.");

            boolean endlessArguments = method.getAnnotationsByType(EndlessArguments.class).length == 1;
            if (argumentLengthRequirement != 0 || endlessArguments) {
                checkArgument(params.length == 2, simpleName + " Missing 2nd parameter for command arguments. Should be type String[].");
                checkArgument(params[1].equals(String[].class), simpleName + " The second command method parameter should be of type String[].");
            } else {
                checkArgument(params.length == 1, simpleName + " Expected only one parameter for command method with no required arguments.");
            }

            String incompleteMsg = "";
            IncompleteCommand[] incompleteCms = method.getAnnotationsByType(IncompleteCommand.class);
            if (incompleteCms.length == 1) incompleteMsg = incompleteCms[0].missing();

            CommandInfo commandInfo = new CommandInfo(
                    listener, method, cmdAnnotations[0].argLenReq(), incompleteMsg, endlessArguments);

            if (endlessArguments) {

                addListenerToMap(commandBaseMessage, argumentLengthRequirement, commandInfo, commandArgEndlessListeners);

            } else {

                addListenerToMap(commandBaseMessage, argumentLengthRequirement, commandInfo, commandArgLimitedListeners);
            }
        }
    }

    private void addListenerToMap(String commandBaseMessage, int argumentLengthRequirement,
                                  CommandInfo commandInfo, Map<String, Map<Integer, CommandInfo>> listenersMap) {

        String commandBaseLowerCase = commandBaseMessage.toLowerCase();
        Map<Integer, CommandInfo> commandInfoMap = listenersMap.get(commandBaseLowerCase);
        if (commandInfoMap == null) {
            commandInfoMap = new HashMap<>();
        }

        commandInfoMap.put(argumentLengthRequirement, commandInfo);
        listenersMap.put(commandBaseLowerCase, commandInfoMap);
    }

    public synchronized CommandState publish(CommandSource commandSource, String command, String[] args) {
        Map<Integer, CommandInfo> commandInfoMap = commandArgLimitedListeners.get(command.toLowerCase());

        // Checking if the command even exist.
        if (commandInfoMap == null) {
            commandInfoMap = commandArgEndlessListeners.get(command.toLowerCase());
            if (commandInfoMap == null) {
                return new CommandState(CommandState.CommandType.NOT_FOUND);
            }

            for (Map.Entry<Integer, CommandInfo> commandInfo : commandInfoMap.entrySet()) {
                if (args.length <= commandInfo.getKey()) continue;
                publishedCommands.add(new PublishInfo(commandSource, args, commandInfo.getValue()));
                return new CommandState(CommandState.CommandType.FOUND);
            }

            String[] incompleteCommands = getCommandSuggestions(commandInfoMap);
            if (incompleteCommands.length == 0) {
                return new CommandState(CommandState.CommandType.NOT_FOUND);
            } else if (incompleteCommands.length == 1) {
                return new CommandState(CommandState.CommandType.SINGE_INCOMPLETE, incompleteCommands[0]);
            } else {
                return new CommandState(CommandState.CommandType.MULTIPLE_INCOMPLETE, incompleteCommands);
            }
        }

        CommandInfo commandInfo = commandInfoMap.get(args.length);
        if (commandInfo == null) {

            // Adding the incomplete commands of the two maps together.
            String[] incompleteCommands = getCommandSuggestions(commandInfoMap);
            commandInfoMap = commandArgEndlessListeners.get(command.toLowerCase());
            if (commandInfoMap != null) {
                incompleteCommands = Stream.concat(Arrays.stream(incompleteCommands),
                              Arrays.stream(getCommandSuggestions(commandInfoMap))).toArray(String[]::new);
            }

            if (incompleteCommands.length == 0) {
                return new CommandState(CommandState.CommandType.NOT_FOUND);
            } else if (incompleteCommands.length == 1) {
                return new CommandState(CommandState.CommandType.SINGE_INCOMPLETE, incompleteCommands[0]);
            } else {
                return new CommandState(CommandState.CommandType.MULTIPLE_INCOMPLETE, incompleteCommands);
            }
        }
        publishedCommands.add(new PublishInfo(commandSource, args, commandInfo));
        return new CommandState(CommandState.CommandType.FOUND);
    }

    private String[] getCommandSuggestions(Map<Integer, CommandInfo> commandInfoMap) {
        String[] incompleteCommands = new String[commandInfoMap.size()];
        int count = 0;
        for (CommandInfo commandInfoSuggestion : commandInfoMap.values()) {
            if (commandInfoSuggestion.incompleteMsg.isEmpty()) continue;
            incompleteCommands[count++] = commandInfoSuggestion.incompleteMsg;
        }
        return incompleteCommands;
    }

    public void executeCommands() {
        PublishInfo publishInfo;
        while ((publishInfo = publishedCommands.poll()) != null) {
            Object listener = publishInfo.commandInfo.listener;
            Method method = publishInfo.commandInfo.method;
            try {
                if (publishInfo.commandInfo.reqArgs != 0 || publishInfo.commandInfo.endlessArguments) {
                    method.invoke(listener, publishInfo.commandSource, publishInfo.arguments);
                } else {
                    method.invoke(listener, publishInfo.commandSource);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    @AllArgsConstructor
    private class PublishInfo {
        private CommandSource commandSource;
        private String[] arguments;
        private CommandInfo commandInfo;
    }
}
