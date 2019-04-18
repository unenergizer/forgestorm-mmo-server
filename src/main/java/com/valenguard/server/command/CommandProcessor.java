package com.valenguard.server.command;

import lombok.AllArgsConstructor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.google.common.base.Preconditions.checkArgument;


public class CommandProcessor {

    @AllArgsConstructor
    private class CommandInfo {
        private Object listener;
        private Method method;
        private int reqArgs;
        private String incompleteMsg;
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

            if (argumentLengthRequirement != 0) {
                checkArgument(params.length == 2, simpleName + " Missing 2nd parameter for command arguments. Should be type String[].");
                checkArgument(params[1].equals(String[].class), simpleName + " The second command method parameter should be of type String[].");
            } else {
                checkArgument(params.length == 1, simpleName + " Expected only one parameter for command method with no required arguments.");
            }

            String incompleteMsg = "";
            IncompleteCommand[] incompleteCms = method.getAnnotationsByType(IncompleteCommand.class);
            if (incompleteCms.length == 1) incompleteMsg = incompleteCms[0].missing();

            String commandBaseLowerCase = commandBaseMessage.toLowerCase();
            Map<Integer, CommandInfo> commandInfoMap = commandListeners.get(commandBaseLowerCase);
            if (commandInfoMap == null) {
                commandInfoMap = new HashMap<>();
            }
            commandInfoMap.put(argumentLengthRequirement, new CommandInfo(
                    listener, method, cmdAnnotations[0].argLenReq(), incompleteMsg
            ));

            commandListeners.put(commandBaseLowerCase, commandInfoMap);
        }
    }

    private final Queue<PublishInfo> publishedCommands = new ConcurrentLinkedQueue<>();

    private final Map<String, Map<Integer, CommandInfo>> commandListeners = new HashMap<>();

    public synchronized CommandState publish(CommandSource commandSource, String command, String[] args) {
        Map<Integer, CommandInfo> commandInfoMap = commandListeners.get(command.toLowerCase());
        if (commandInfoMap == null) return new CommandState(CommandState.CommandType.NOT_FOUND);
        CommandInfo commandInfo = commandInfoMap.get(args.length);
        if (commandInfo == null) {

            if (commandInfoMap.size() == 1) {
                CommandInfo commandInfoSuggestion = (CommandInfo) commandInfoMap.values().toArray()[0];
                if (commandInfoSuggestion.incompleteMsg.isEmpty())
                    return new CommandState(CommandState.CommandType.NOT_FOUND);
                return new CommandState(CommandState.CommandType.SINGE_INCOMPLETE, commandInfoSuggestion.incompleteMsg);
            }

            String[] incompleteCommands = new String[commandInfoMap.size()];
            int count = 0;
            for (CommandInfo commandInfoSuggestion : commandInfoMap.values()) {
                if (commandInfoSuggestion.incompleteMsg.isEmpty()) continue;
                incompleteCommands[count++] = commandInfoSuggestion.incompleteMsg;
            }

            return new CommandState(CommandState.CommandType.MULTIPLE_INCOMPLETE, incompleteCommands);
        }
        publishedCommands.add(new PublishInfo(commandSource, args, commandInfo));
        return new CommandState(CommandState.CommandType.FOUND);
    }

    public void executeCommands() {
        PublishInfo publishInfo;
        while ((publishInfo = publishedCommands.poll()) != null) {
            Object listener = publishInfo.commandInfo.listener;
            Method method = publishInfo.commandInfo.method;
            try {
                if (publishInfo.commandInfo.reqArgs == 0) {
                    method.invoke(listener, publishInfo.commandSource);
                } else {
                    method.invoke(listener, publishInfo.commandSource, publishInfo.arguments);
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
