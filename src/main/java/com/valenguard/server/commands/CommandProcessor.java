package com.valenguard.server.commands;

import com.valenguard.server.util.Log;
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

    @AllArgsConstructor
    private class PublishInfo {
        private String[] arguments;
        private CommandInfo commandInfo;
    }

    private Queue<PublishInfo> publishedCommands = new ConcurrentLinkedQueue<>();

    private Map<String, Map<Integer, CommandInfo>> commandListeners = new HashMap<>();

    public void addListener(Object listener) {
        for (Method method : listener.getClass().getMethods()) {
            Command[] cmdAnnotations = method.getAnnotationsByType(Command.class);

            if (cmdAnnotations.length != 1) continue;

            Command command = cmdAnnotations[0];
            String commandBaseMessage = command.base();
            int argumentLengthRequirement = command.argLenReq();
            Class<?>[] params = method.getParameterTypes();

            checkArgument(!(cmdAnnotations[0].argLenReq() < 0), "The Required argument length for a command cannot be below 0.");

            if (argumentLengthRequirement != 0) {
                if (params.length != 1)
                    throw new RuntimeException("The Parameter Length of Command methods must be 1.");

                if (!params[0].equals(String[].class))
                    throw new RuntimeException("The parameter of a Command method must be String[].");
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

    synchronized boolean publish(String command, String[] args) {
        Map<Integer, CommandInfo> commandInfoMap = commandListeners.get(command.toLowerCase());
        if (commandInfoMap == null) return false;
        CommandInfo commandInfo = commandInfoMap.get(args.length);
        if (commandInfo == null) {

            if (commandInfoMap.size() == 1) {
                CommandInfo commandInfoSuggestion = (CommandInfo) commandInfoMap.values().toArray()[0];
                if (commandInfoSuggestion.incompleteMsg.isEmpty()) return false;
                Log.println(getClass(), "[Command] -> " + commandInfoSuggestion.incompleteMsg);
                return true;
            }

            Log.println(getClass(), "Suggested Alternatives:");
            for (CommandInfo commandInfoSuggestion : commandInfoMap.values()) {
                if (commandInfoSuggestion.incompleteMsg.isEmpty()) continue;
                Log.println(getClass(), "  - [Command] -> " + commandInfoSuggestion.incompleteMsg);
            }

            return true;
        }
        publishedCommands.add(new PublishInfo(args, commandInfo));
        return true;
    }

    public void executeCommands() {
        PublishInfo publishInfo;
        while ((publishInfo = publishedCommands.poll()) != null) {
            Object listener = publishInfo.commandInfo.listener;
            Method method = publishInfo.commandInfo.method;
            try {
                if (publishInfo.commandInfo.reqArgs == 0) {
                    method.invoke(listener);
                } else {
                    method.invoke(listener, (Object) publishInfo.arguments);
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
