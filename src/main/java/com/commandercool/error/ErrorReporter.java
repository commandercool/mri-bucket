package com.commandercool.error;

import java.awt.Component;

import javax.swing.JOptionPane;

public class ErrorReporter {

    public static void reportError(Component parent, Throwable throwable) {
        String message = throwable.getMessage();
        JOptionPane.showMessageDialog(parent, "An error has occurred",
                message != null ? message : "Error message is missing ¯\\_(ツ)_/¯", JOptionPane.ERROR_MESSAGE);
    }

}
