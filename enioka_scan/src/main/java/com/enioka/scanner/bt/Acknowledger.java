package com.enioka.scanner.bt;

/**
 * A special command used to signal the reception of a message.
 * TODO: should disappear once we have working commands and become a normal command.
 */
public interface Acknowledger {
    /**
     * The command to send on the bluetooth socket when something must be acknowledged positively.
     *
     * @return command.
     */
    byte[] getOkCommand();

    /**
     * The command to send on the bluetooth socket when something must be acknowledged negatively.
     *
     * @return command.
     */
    byte[] getKoCommand(MessageRejectionReason reason);
}
