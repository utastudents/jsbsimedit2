package uta.cse3310.tab.concreteTabs;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import uta.cse3310.tab.simpleTab;

public class threeTab extends simpleTab {
    // attributes that only pertain to the 'concrete' tab called oneTab

    public threeTab(String label) {
        super(label);
        System.out.println("in threeTab constructor");

        // what is unique about this tab....

        panel.setLayout(new GridLayout(2, 1)); // Use GridLayout for panel3
        panel.add(new JButton("Click Me!"));
        panel.add(new JCheckBox("Enable Feature"));
        panel.setBackground(new Color(220, 255, 200)); // Light green background
    }

}
