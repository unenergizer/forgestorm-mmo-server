package com.valenguard.server.command;

import lombok.Getter;

@Getter
public class CommandState {
    private CommandType commandType;
    private String incompleteMessage;
    private String[] multipleIncompleteMessages;

    CommandState(CommandType commandType) {
        this.commandType = commandType;
    }

    CommandState(CommandType commandType, String incompleteMessage) {
        this.commandType = commandType;
        this.incompleteMessage = incompleteMessage;
    }

    CommandState(CommandType commandType, String[] multipleIncompleteMessages) {
        this.commandType = commandType;
        this.multipleIncompleteMessages = multipleIncompleteMessages;
    }

    public enum CommandType {
        NOT_FOUND,
        SINGE_INCOMPLETE,
        MULTIPLE_INCOMPLETE,
        FOUND
    }
}
