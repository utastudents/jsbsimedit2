package utastudents.jsbsimedit2.ui.shell;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import utastudents.jsbsimedit2.core.domain.CanvasModel;
import utastudents.jsbsimedit2.ui.dnd.CanvasPanel;
import utastudents.jsbsimedit2.ui.dnd.PaletteBar;

// ShellFrame: main window for the whole app.
// - Hosts a JTabbedPane
// - Mounts the DnD subsystem as the "Flight Control (DnD)" tab

public class ShellFrame extends JFrame {

    public ShellFrame() {
        super("JSBSim Edit - Prototype Shell");

        // Tabs
        JTabbedPane tabs = new JTabbedPane();

        // DnD subsystem tab
        CanvasModel model = new CanvasModel();
        CanvasPanel canvas = new CanvasPanel(model);
        JPanel dndTab = new JPanel(new BorderLayout());
        
        // top shelf 
        dndTab.add(new PaletteBar(), BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(canvas);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getHorizontalScrollBar().setUnitIncrement(16);
        // scrollable canvas
        dndTab.add(sp, BorderLayout.CENTER);

        // DnD tab
        tabs.addTab("Flight Control (DnD)", dndTab);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tabs, BorderLayout.CENTER);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    private JPanel stubPanel(String msg) {
        JPanel p = new JPanel(new GridBagLayout());
        p.add(new JLabel(msg));
        return p;
    }
}
