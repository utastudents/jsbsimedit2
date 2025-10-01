package utastudents.jsbsimedit2.ui.dnd;

import utastudents.jsbsimedit2.core.domain.NodeModel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

// NodeView = the visual box for a node on the canvas.
// - It draws the rounded rectangle and the node's label.
// - It is draggable and updates model.x / model.y when moved.
// - Now also draws small "ports" from model.ports (left/right for now).

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

        // size and starting position come from the model
        setBounds(model.x, model.y, NODE_WIDTH, NODE_HEIGHT);
        setOpaque(false);
        setFocusable(true);
        setToolTipText(model.type + " • " + model.id);

        // mouse: select on press, remember where drag started
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                canvas.handleNodeMousePressed(NodeView.this, e);
                dragAnchor = e.getPoint();
            }
            @Override public void mouseReleased(MouseEvent e) {
                dragAnchor = null;
            }
        });

        // mouse: drag the node and update the model position
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                if (dragAnchor == null) return;
                int nx = getX() + e.getX() - dragAnchor.x;
                int ny = getY() + e.getY() - dragAnchor.y;
                setLocation(nx, ny);
                model.x = nx;
                model.y = ny;
                // repaint the parent so selection/ports look correct
                getParent().repaint();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        // use Graphics2D for nicer edges
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );

        // draw the rounded rectangle "body" of the node
        Shape body = new RoundRectangle2D.Float(
                0, 0, getWidth() - 1, getHeight() - 1, 12, 12
        );
        g2.setColor(new Color(246, 246, 246)); // light fill
        g2.fill(body);
        g2.setColor(selected ? new Color(66, 135, 245) : new Color(170, 170, 170)); // blue if selected
        g2.setStroke(new BasicStroke(selected ? 2.4f : 1.6f));
        g2.draw(body);

        // label in the middle (uses model.type)
        g2.setColor(Color.DARK_GRAY);
        g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
        String label = model.type;
        FontMetrics fm = g2.getFontMetrics();
        int tx = Math.max(8, (getWidth() - fm.stringWidth(label)) / 2);
        int ty = (getHeight() + fm.getAscent()) / 2 - 4;
        g2.drawString(label, tx, ty);

        // ----- draw ports (simple circles) -----
        // We expect model.ports to be filled (e.g., one IN on left, one OUT on right).
        // Each port has normalized position (0..1) inside the node.
        final int R = 6; // radius of the port circle
        if (model.ports != null) {
            for (NodeModel.Port p : model.ports) {
                // convert normalized (0..1) to pixels inside this component
                int px = Math.round(p.nx * (getWidth()  - 1));
                int py = Math.round(p.ny * (getHeight() - 1));

                // fill color: blue for input, green for output
                g2.setColor(p.input ? new Color(66, 135, 245) : new Color(46, 180, 98));
                g2.fillOval(px - R, py - R, 2 * R, 2 * R);

                // thin border so the circle edges are visible
                g2.setColor(new Color(40, 40, 40));
                g2.drawOval(px - R, py - R, 2 * R, 2 * R);
            }
        }
        // ----- end ports -----

        g2.dispose();
    }
}
