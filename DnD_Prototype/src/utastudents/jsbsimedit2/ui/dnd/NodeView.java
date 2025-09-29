package utastudents.jsbsimedit2.ui.dnd;

import utastudents.jsbsimedit2.core.domain.NodeModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

// NodeView is a visual/interactive box for a node on the canvas.
// - Draggable - updates the underlying NodeModel.x/y
// - Leaves selection behavior to CanvasPanel

public class NodeView extends JComponent {
    public static final int NODE_WIDTH  = 120;
    public static final int NODE_HEIGHT = 50;

    public final NodeModel model;
    public final CanvasPanel canvas;
    public boolean selected = false;

    private Point dragAnchor;

    public NodeView(NodeModel model, CanvasPanel canvas) {
        this.model = model;
        this.canvas = canvas;
        setBounds(model.x, model.y, NODE_WIDTH, NODE_HEIGHT);
        setOpaque(false);
        setFocusable(true);
        setToolTipText(model.type + " • " + model.id);

        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                canvas.handleNodeMousePressed(NodeView.this, e);
                dragAnchor = e.getPoint();
            }
            @Override public void mouseReleased(MouseEvent e) { dragAnchor = null; }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (dragAnchor == null) return;
                int nx = getX() + e.getX() - dragAnchor.x;
                int ny = getY() + e.getY() - dragAnchor.y;
                setLocation(nx, ny);
                model.x = nx; model.y = ny;
                
                getParent().repaint();
            }
        });
    }

    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Shape r = new RoundRectangle2D.Float(0, 0, getWidth()-1, getHeight()-1, 12, 12);
        g2.setColor(new Color(246,246,246));
        g2.fill(r);
        g2.setColor(selected ? new Color(66,135,245) : new Color(170,170,170));
        g2.setStroke(new BasicStroke(selected ? 2.4f : 1.6f));
        g2.draw(r);

        g2.setColor(Color.DARK_GRAY);
        g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
        String label = model.type;
        FontMetrics fm = g2.getFontMetrics();
        int tx = Math.max(8, (getWidth() - fm.stringWidth(label)) / 2);
        int ty = (getHeight() + fm.getAscent()) / 2 - 4;
        g2.drawString(label, tx, ty);
        g2.dispose();
    }
}
