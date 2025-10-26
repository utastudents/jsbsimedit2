package uta.cse3310.tab.concreteTabs;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;

import uta.cse3310.tab.simpleTab;

public class oneTab extends simpleTab{
    // attributes that only pertain to the 'concrete' tab called oneTab

    public oneTab(String label){
        super(label);
        System.out.println("in oneTab constructor");
         
        // this is what is unique about 1 tab
        panel.add(new JLabel("Content of Tab 1", SwingConstants.CENTER), BorderLayout.CENTER);
        
    }

}
