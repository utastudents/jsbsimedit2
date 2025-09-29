package utastudents.jsbsimedit2.ui.dnd;

import utastudents.jsbsimedit2.core.domain.CanvasModel;
import utastudents.jsbsimedit2.core.domain.NodeModel;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

// CanvasPanel: scrollable canvas that owns NodeViews.
// - Single selection, Ctrl+click multi-select
// - Delete removes selected, Esc clears selection
// - Double-click blank space to add a demo node (until palette DnD is built)
// - F2 toggles mouse-coordinate debug overlay

public class CanvasPanel extends JPanel {
    public final CanvasModel model;
    public final Map<String, NodeView> viewById = new HashMap<>();

    // selection state
    final Set<NodeView> selection = new LinkedHashSet<>();

    // debug overlay
    private static boolean DEBUG_SHOW_MOUSE_POS = true; // toggle with F2
    private Point debugMousePoint = null;

    // fixed canvas size
    public static final int CANVAS_W = 3840;
    public static final int CANVAS_H = 2160;
    private static final Dimension FIXED_PREF = new Dimension(CANVAS_W, CANVAS_H);

    public CanvasPanel(CanvasModel model) {
        super(null);
        this.model = model;
        setBackground(Color.white);
        setBorder(new LineBorder(new Color(224,224,224)));
        setPreferredSize(FIXED_PREF);   // fixed area

        // deselect on blank click
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                requestFocusInWindow();
                if (getComponentAt(e.getPoint()) == CanvasPanel.this) {
                    clearSelection();
                }
            }
        });

        // double-click to add demo node for now
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    addNodeAt("Node", e.getX(), e.getY());
                }
            }
        });

        // keys: Delete selection, Esc clear, F2 toggle debug
        setFocusable(true);
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteNodes");
        getActionMap().put("deleteNodes", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (selection.isEmpty()) return;
                java.util.List<NodeView> toRemove = new ArrayList<>(selection);
                for (NodeView v : toRemove) {
                    remove(v);
                    viewById.remove(v.model.id);
                    model.removeById(v.model.id);
                }
                selection.clear();
                revalidate(); repaint();
            }
        });
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "clearSel");
        getActionMap().put("clearSel", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { clearSelection(); }
        });
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0), "toggleDebug");
        getActionMap().put("toggleDebug", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                DEBUG_SHOW_MOUSE_POS = !DEBUG_SHOW_MOUSE_POS;
                repaint();
            }
        });

        // mouse coordinate overlay updates
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseMoved(MouseEvent e) { if (DEBUG_SHOW_MOUSE_POS) { debugMousePoint = e.getPoint(); repaint(); } }
            @Override public void mouseDragged(MouseEvent e) { if (DEBUG_SHOW_MOUSE_POS) { debugMousePoint = e.getPoint(); repaint(); } }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseExited(MouseEvent e) { if (DEBUG_SHOW_MOUSE_POS) { debugMousePoint = null; repaint(); } }
        });
    }

    // selection helpers
    public void clearSelection() {
        for (NodeView v : selection) { v.selected = false; v.repaint(); }
        selection.clear();
    }
    public void selectOnly(NodeView v) {
        clearSelection();
        v.selected = true; v.repaint();
        selection.add(v);
    }
    public void toggleSelection(NodeView v) {
        if (selection.contains(v)) {
            v.selected = false; v.repaint();
            selection.remove(v);
        } else {
            v.selected = true; v.repaint();
            selection.add(v);
        }
    }
    private boolean isCtrl(MouseEvent e) {
        int metaMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        boolean shortcutDown = (e.getModifiersEx() & metaMask) != 0;
        return (e.isControlDown() || shortcutDown) && !e.isShiftDown() && !e.isAltDown();
    }
    public void handleNodeMousePressed(NodeView v, MouseEvent e) {
        requestFocusInWindow();
        if (isCtrl(e)) toggleSelection(v);
        else if (!selection.contains(v)) selectOnly(v);
    }

    // add / reload
    public void addNodeAt(String type, int cx, int cy) {
        int w = NodeView.NODE_WIDTH, h = NodeView.NODE_HEIGHT;
        NodeModel m = model.addNode(type, cx - w/2, cy - h/2);
        NodeView v = new NodeView(m, this);
        viewById.put(m.id, v);
        add(v);
        selectOnly(v);
        revalidate();
        repaint();
    }

    public void reloadFromModel() {
        removeAll(); viewById.clear();
        for (NodeModel n : model.nodes) {
            NodeView v = new NodeView(n, this);
            viewById.put(n.id, v);
            add(v);
        }
        clearSelection();
        revalidate(); repaint();
    }

    // debug overlay
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (DEBUG_SHOW_MOUSE_POS && debugMousePoint != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            String msg = "(" + debugMousePoint.x + ", " + debugMousePoint.y + ")";
            FontMetrics fm = g2.getFontMetrics();
            int pad = 4;
            int tx = debugMousePoint.x + 12;
            int ty = debugMousePoint.y - 12;
            int w = fm.stringWidth(msg) + pad * 2;
            int h = fm.getHeight() + pad * 2;
            if (tx + w > getWidth())  tx = getWidth() - w - 4;
            if (ty - h < 0)           ty = h + 4;
            g2.setColor(new Color(0,0,0,90));
            g2.fillRoundRect(tx, ty - fm.getAscent() - pad, w, h, 8, 8);
            g2.setColor(Color.WHITE);
            g2.drawString(msg, tx + pad, ty);
            g2.dispose();
        }
    }
}
