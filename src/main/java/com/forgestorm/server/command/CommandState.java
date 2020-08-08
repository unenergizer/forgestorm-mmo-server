package com.forgestorm.server.command;

import lombok.Getter;

@Getter
public class CommandState {
    private String commandBase;
    private CommandType commandType;
    private String incompleteMessage;
    private String[] multipleIncompleteMessages;

    CommandState(String commandBase, CommandType commandType) {
        this.commandBase = commandBase;
        this.commandType = commandType;
    }

    CommandState(String commandBase, CommandType commandType, String incompleteMessage) {
        this.commandBase = commandBase;
        this.commandType = commandType;
        this.incompleteMessage = incompleteMessage;
    }

    CommandState(String commandBase, CommandType commandType, String[] multipleIncompleteMessages) {
        this.commandBase = commandBase;
        this.commandType = commandType;
        this.multipleIncompleteMessages = multipleIncompleteMessages;
    }

    public enum CommandType {
        NOT_FOUND,
        SINGE_INCOMPLETE,
        MULTIPLE_INCOMPLETE,
        INVALID_PERMISSION,
        FOUND
    }
}
