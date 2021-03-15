package com.commandercool.error;

import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ErrorReporter {

    private static Component FRAME;

    public static void setFrame(JFrame frame) {
        ErrorReporter.FRAME = frame;
    }

    public static void reportError(Throwable throwable) {
        if (FRAME != null) {
            String message = throwable.getMessage();
            showMessageDialog(FRAME, "An error has occurred",
                    message != null ? message : "Error message is missing ¯\\_(ツ)_/¯", JOptionPane.ERROR_MESSAGE);
        }
    }

}
