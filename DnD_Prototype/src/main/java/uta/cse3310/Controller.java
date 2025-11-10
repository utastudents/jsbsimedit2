import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JFileChooser;



public class Controller {

    public static void start() {
        Model model = new Model();
        View view = new View(model);

        JScrollPane scrollPane = new JScrollPane(view);
        scrollPane.getVerticalScrollBar().setUnitIncrement(20);
        scrollPane.getVerticalScrollBar().setBlockIncrement(100);

        JFrame f = new JFrame("JSBSim Edit - DnD Prototype");
        f.setJMenuBar(buildMenuBar(f, model, view));

        f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        f.setLayout(new BorderLayout());

        // ---- Palette (Top) ----
        JPanel palette = buildPalette(view);
        f.add(palette, BorderLayout.NORTH);

        // ---- Canvas ----
        attachMouseControllers(view, model);
        view.setTransferHandler(new CanvasDropHandler(model, view));
        f.add(scrollPane, BorderLayout.CENTER);

        f.setSize(1200, 800);
        f.setLocationByPlatform(true);
        f.setVisible(true);
    }

    // ---------------- Palette ----------------
    private static JPanel buildPalette(View view) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8));
        p.setBackground(new Color(245, 245, 245));
        p.setBorder(new EmptyBorder(6, 8, 6, 8));

        String[] names = {
                "Source","Destination","Summer","PID","Gain",
                "Filter","Dead Band","Switch","Kinemat","FCSFunction"
        };

        for (String name : names) {
            JLabel tag = new JLabel(name);
            tag.setForeground(new Color(0, 0, 0));
            tag.setOpaque(true);
            tag.setBackground(new Color(245, 245, 245));
            tag.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            tag.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));

            // Make each label draggable as a string payload
            tag.setTransferHandler(new TransferHandler("text") {
                @Override
                protected Transferable createTransferable(JComponent c) {
                    return new StringSelection(((JLabel) c).getText());
                }
                @Override
                public int getSourceActions(JComponent c) {
                    return COPY;
                }
            });
            tag.addMouseListener(new MouseAdapter() {
                @Override public void mousePressed(MouseEvent e) {
                    JComponent c = (JComponent) e.getSource();
                    c.getTransferHandler().exportAsDrag(c, e, TransferHandler.COPY);
                }
            });

            p.add(tag);
        }
        return p;
    }

    // ---------------- Canvas mouse/controller logic ----------------
    private static void attachMouseControllers(View view, Model model) {
        MouseAdapter ma = new MouseAdapter() {
            private Model.Node draggingNode = null;
            private Point dragOffset = null;

            private Model.Node connectFrom = null; // node whose OUTPUT we grabbed

            @Override
            public void mousePressed(MouseEvent e) {
                Point p = e.getPoint();

                // Start connection if press on an output port
                Model.Node onOut = view.nodeWithOutputAt(p);
                if (onOut != null) {
                    connectFrom = onOut;
                    view.setConnectionPreview(connectFrom, p);
                    return;
                }

                // Otherwise maybe start dragging a node
                Model.Node n = view.nodeAt(p);
                if (n != null) {
                    draggingNode = n;
                    dragOffset = new Point(p.x - n.bounds.x, p.y - n.bounds.y);
                    // Bring node to front (simple z-order)
                    model.nodes.remove(n);
                    model.nodes.add(n);
                    view.repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point p = e.getPoint();

                if (connectFrom != null) {
                    view.setConnectionPreview(connectFrom, p);
                    return;
                }

                if (draggingNode != null && dragOffset != null) {
                    draggingNode.bounds.x = p.x - dragOffset.x;
                    draggingNode.bounds.y = p.y - dragOffset.y;
                    view.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                Point p = e.getPoint();

                if (connectFrom != null) {
                    // Can we connect to an INPUT port?
                    Model.Node to = view.nodeWithInputAt(p);
                    if (to != null && to != connectFrom) {
                        model.addEdge(connectFrom, to);
                    }
                    connectFrom = null;
                    view.clearConnectionPreview();
                    return;
                }

                draggingNode = null;
                dragOffset = null;
            }
        };

        view.addMouseListener(ma);
        view.addMouseMotionListener(ma);
    }

    // ---------------- DnD: drop nodes onto canvas ----------------
    private static class CanvasDropHandler extends TransferHandler {
        private final Model model;
        private final View view;

        CanvasDropHandler(Model model, View view) {
            this.model = model;
            this.view = view;
        }

        @Override
        public boolean canImport(TransferSupport s) {
            return s.isDataFlavorSupported(DataFlavor.stringFlavor);
        }

        @Override
        public boolean importData(TransferSupport s) {
            if (!canImport(s)) return false;
            try {
                String label = (String) s.getTransferable().getTransferData(DataFlavor.stringFlavor);
                Model.NodeType type = Model.NodeType.fromLabel(label);

                // Where was it dropped?
                TransferHandler.DropLocation dl = (TransferHandler.DropLocation) s.getDropLocation();
                Point drop = dl.getDropPoint();

                // Create node with its top-left near the drop point
                int x = drop.x - 70;
                int y = drop.y - 35;
                model.addNode(type, x, y);
                view.repaint();
                return true;
            } catch (Exception ex) {
                ex.printStackTrace();
                return false;
            }
        }
 
    }


// ---------------- Menu Bar: Save / Load XML ----------------
private static JMenuBar buildMenuBar(JFrame frame, Model model, View view) {
    JMenuBar bar = new JMenuBar();

    JMenu file = new JMenu("File");

    JMenuItem save = new JMenuItem("Save XML");
    save.addActionListener(e -> {
        try {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                model.saveToXML(chooser.getSelectedFile());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    });

    JMenuItem load = new JMenuItem("Load XML");
    load.addActionListener(e -> {
        try {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
                model.loadFromXML(chooser.getSelectedFile());
                view.repaint();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    });

    file.add(save);
    file.add(load);
    bar.add(file);

    return bar;
}





}
