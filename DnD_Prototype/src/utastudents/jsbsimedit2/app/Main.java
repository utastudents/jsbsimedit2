package utastudents.jsbsimedit2.app;

import utastudents.jsbsimedit2.ui.shell.ShellFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Optional: Makes scrollbars not ugly
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

            new ShellFrame().setVisible(true);
        });
    }
}
