package uta.cse3310.tab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class baseTab {
    // attributes common to all tabs
    public JPanel panel;
    public String label;

    public baseTab(String label) {

        System.out.println("in base Tab constructor");
        // this is common to all tabs
        panel = new JPanel();
        panel.setLayout(new BorderLayout()); // Use BorderLayout for panel1

        panel.setBackground(new Color(200, 220, 255)); // Light blue background
    }
}
