package utastudents.jsbsimedit2.ui.shell;

import utastudents.jsbsimedit2.core.domain.CanvasModel;
import utastudents.jsbsimedit2.ui.dnd.CanvasPanel;
import utastudents.jsbsimedit2.ui.dnd.PaletteBar;
import utastudents.jsbsimedit2.io.layout.LayoutIO;

import javax.swing.*;
import java.awt.*;

// ShellFrame: main window for the whole app.
// - Hosts a JTabbedPane
// - Mounts the DnD subsystem as the "Flight Control (DnD)" tab
// - Leaves blank/stub tabs for other teams

public class ShellFrame extends JFrame {

    public ShellFrame() {
        super("JSBSimCommander — Prototype Shell");

        // Menu Bar (Save/Load are disabled for now)
        JMenuBar mb = new JMenuBar();
        JMenu file = new JMenu("File");
        JMenuItem save = new JMenuItem("Save Layout…"), load = new JMenuItem("Load Layout…");
        save.setEnabled(false); load.setEnabled(false);
        save.addActionListener(e -> LayoutIO.save(this));
        load.addActionListener(e -> LayoutIO.load(this));
        file.add(save); file.add(load);
        mb.add(file); setJMenuBar(mb);

        JTabbedPane tabs = new JTabbedPane();

        // DnD subsystem tab
        CanvasModel model = new CanvasModel();
        CanvasPanel canvas = new CanvasPanel(model);
        JPanel dndTab = new JPanel(new BorderLayout());

        dndTab.add(new PaletteBar(), BorderLayout.NORTH);       // top shelf 
        JScrollPane sp = new JScrollPane(canvas);
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getHorizontalScrollBar().setUnitIncrement(16);
        dndTab.add(sp, BorderLayout.CENTER);                    // scrollable canvas

        // All tabs
        tabs.addTab("General Information", stubPanel("General Information here"));
        tabs.addTab("Metrics", stubPanel("Metrics here"));
        tabs.addTab("Mass Balance", stubPanel("Mass Balance here"));
        tabs.addTab("Ground Reactions", stubPanel("Ground Reaction here"));
        tabs.addTab("Propulsion", stubPanel("Propulsion here"));
        tabs.addTab("Flight Control (DnD)", dndTab);
        tabs.addTab("Aerodynamics", stubPanel("Aerodynamics here"));
        tabs.addTab("Output", stubPanel("Output here"));

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
