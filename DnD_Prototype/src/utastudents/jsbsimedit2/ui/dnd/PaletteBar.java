package utastudents.jsbsimedit2.ui.dnd;

import javax.swing.*;
import java.awt.*;

// PaletteBar:
// - Top shelf across the tab
// - Replace labels with draggable components when you implement DnD source (Issue #2)

public class PaletteBar extends JPanel {
    public PaletteBar() {
        super(new FlowLayout(FlowLayout.LEFT, 8, 6));
        setBorder(BorderFactory.createTitledBorder("Palette (coming soon)"));
        add(new JLabel("Source"));
        add(new JLabel("Destination"));
        add(new JLabel("Summer"));
        add(new JLabel("PID"));
        add(new JLabel("Gain"));
        add(new JLabel("Filter"));
        add(new JLabel("Dead Band"));
        add(new JLabel("Switch"));
        add(new JLabel("Kinemat"));
        add(new JLabel("FCSFunction"));
    }
}
