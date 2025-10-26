package uta.cse3310;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import uta.cse3310.tab.concreteTabs.oneTab;
import uta.cse3310.tab.concreteTabs.threeTab;
import uta.cse3310.tab.concreteTabs.twoTab;

public class App {

    public static void main(String[] args) {

        // Create the main frame
        JFrame frame = new JFrame("JTabbedPane Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);
        frame.setLocationRelativeTo(null); // Center the frame

        // Create a JTabbedPane
        JTabbedPane tabbedPane = new JTabbedPane();

        // demonstrates using inheritiance to structure code for each
        // type and each special 'tab' in the program.

        // the array is probably not the ideal data structure for this list, but it is
        // simple.

        uta.cse3310.tab.baseTab frameTabs[] = new uta.cse3310.tab.baseTab[3];

        frameTabs[0] = new oneTab("Tab 1");
        tabbedPane.addTab("Tab 1", null, frameTabs[0].panel, "This is the first tab.");
        tabbedPane.setMnemonicAt(0, KeyEvent.VK_1); // Set mnemonic for Tab 1

        frameTabs[1] = new twoTab("Tab 2");
        tabbedPane.addTab("Tab 2", null, frameTabs[1].panel, "This is the second tab.");
        tabbedPane.setMnemonicAt(1, KeyEvent.VK_2); // Set mnemonic for Tab 2

        frameTabs[2] = new threeTab("Tab 3");
        tabbedPane.addTab("Tab 3", null, frameTabs[2].panel, "This is the third tab.");
        tabbedPane.setMnemonicAt(2, KeyEvent.VK_3); // Set mnemonic for Tab 3

        // Add the JTabbedPane to the frame
        frame.add(tabbedPane, BorderLayout.CENTER);

        // Make the frame visible
        frame.setVisible(true);

    }
}
