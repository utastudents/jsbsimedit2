package uta.cse3310;

import java.io.File;
import java.util.List;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import uta.cse3310.tab.concreteTabs.oneTab;
import uta.cse3310.tab.concreteTabs.twoTab;
import uta.cse3310.tab.concreteTabs.threeTab;


import jakarta.xml.bind.Marshaller;
import generated.FdmConfig;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

public class App {

    public static void main(String[] args) {

        // start jaxb / xml example code
        try {

            File file = new File("f16.xml");
            // JAXBContext jaxbContext = JAXBContext.newInstance(FdmConfig.class);

            // Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            // FdmConfig cfg = (FdmConfig) jaxbUnmarshaller.unmarshal(file);

            // System.out.println(cfg.getName());

            JAXBContext jc = JAXBContext.newInstance("generated");

            Unmarshaller um = jc.createUnmarshaller();
            FdmConfig cfg = (FdmConfig) um.unmarshal(file);

            System.out.println(cfg);
            System.out.println(cfg.getFileheader().getCopyright());
            System.out.println(cfg.getFileheader().getVersion());
            System.out.println(cfg.getAerodynamics().getAxis().get(0).getName());
            System.out.println(cfg.getAerodynamics().getAxis().get(0).getDocumentationOrFunction());
            System.out.println(cfg.getAerodynamics().getAxis().get(0).getClass());

            // Marshaller m = jc.createMarshaller();
            // m.setProperty("jaxb.formatted.output", true);
            // m.marshal(cfg, System.out);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
        //
        //
        //
        // end xml / jaxb example code
        //
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

        // Create Panel 1 and add components
        // JPanel panel1 = new JPanel();
        // panel1.setLayout(new BorderLayout()); // Use BorderLayout for panel1
        // panel1.add(new JLabel("Content of Tab 1", SwingConstants.CENTER),
        // BorderLayout.CENTER);
        // panel1.setBackground(new Color(200, 220, 255)); // Light blue background

        // Create Panel 2 and add components
        // JPanel panel2 = new JPanel();
        // panel2.setLayout(new FlowLayout()); // Use FlowLayout for panel2
        // panel2.add(new JLabel("Enter your name:"));
        // panel2.add(new JTextField(15));
        // panel2.setBackground(new Color(255, 220, 200)); // Light orange background

        // Create Panel 3 and add components
        // JPanel panel3 = new JPanel();
        // panel3.setLayout(new GridLayout(2, 1)); // Use GridLayout for panel3
        // panel3.add(new JButton("Click Me!"));
        // panel3.add(new JCheckBox("Enable Feature"));
        // panel3.setBackground(new Color(220, 255, 200)); // Light green background

        // Add panels to the JTabbedPane
        // tabbedPane.addTab("Tab 1", null, panel1, "This is the first tab.");
        // tabbedPane.setMnemonicAt(0, KeyEvent.VK_1); // Set mnemonic for Tab 1

        // tabbedPane.addTab("Tab 2", null, panel2, "This is the second tab.");
        // tabbedPane.setMnemonicAt(1, KeyEvent.VK_2); // Set mnemonic for Tab 2

        // tabbedPane.addTab("Tab 3", null, panel3, "This is the third tab.");
        // tabbedPane.setMnemonicAt(2, KeyEvent.VK_3); // Set mnemonic for Tab 3

        // Add the JTabbedPane to the frame
        frame.add(tabbedPane, BorderLayout.CENTER);

        // Make the frame visible
        frame.setVisible(true);

    }
}
