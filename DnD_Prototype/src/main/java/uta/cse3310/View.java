import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Path2D;
import java.util.Objects;

import javax.swing.JComponent;

public class View extends JComponent {
    private final Model model;

    // Visual constants
    public static final int PORT_SIZE = 12;
    public static final Stroke EDGE_STROKE = new BasicStroke(1f);
    public static final Stroke PREVIEW_STROKE =
        new BasicStroke(1f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, new float[]{8f, 8f}, 0f);

    // Connection preview (set by controller)
    private Model.Node previewFrom;
    private Point previewTo;

    public View(Model model) {
        this.model = Objects.requireNonNull(model);
        setOpaque(true);
        setBackground(new Color(0xFFFFFF));
        setPreferredSize(new Dimension(2560, 1440));
        setDoubleBuffered(true);
    }

    // Controller calls these during a drag-connection interaction
    public void setConnectionPreview(Model.Node from, Point to) {
        this.previewFrom = from;
        this.previewTo = to;
        repaint();
    }
    public void clearConnectionPreview() {
        this.previewFrom = null;
        this.previewTo = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();

        // FORCE a full clear each repaint
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        paintEdges(g2);
        paintNodes(g2);
        paintPreview(g2);

        g2.dispose();
    }

    private void paintNodes(Graphics2D g2) {
        for (Model.Node n : model.nodes) {
            // Body
            g2.setColor(new Color(255, 255, 255));
            g2.fillRoundRect(n.bounds.x, n.bounds.y, n.bounds.width, n.bounds.height, 0, 0);
            g2.setColor(new Color(0, 0, 0));
            g2.drawRoundRect(n.bounds.x, n.bounds.y, n.bounds.width, n.bounds.height, 0, 0);

            // Title
            g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
            FontMetrics fm = g2.getFontMetrics();
            String title = n.type.label;
            int tx = n.bounds.x + 10;
            int ty = n.bounds.y + fm.getAscent() + 8;
            g2.drawString(title, tx, ty);

            // Ports
            Rectangle inR = n.inputPortRect(PORT_SIZE);
            Rectangle outR = n.outputPortRect(PORT_SIZE);

            g2.setColor(new Color(90, 180, 255));
            g2.fillRect(inR.x, inR.y, inR.width, inR.height);
            g2.setColor(new Color(255, 120, 120));
            g2.fillRect(outR.x, outR.y, outR.width, outR.height);

            g2.setColor(new Color(255, 255, 255, 160));
            g2.drawRect(inR.x, inR.y, inR.width, inR.height);
            g2.drawRect(outR.x, outR.y, outR.width, outR.height);
        }
    }

    private void paintEdges(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0));
        g2.setStroke(EDGE_STROKE);
        for (Model.Edge e : model.edges) {
            Point a = e.from.outputPort();
            Point b = e.to.inputPort();
            drawOrth(g2, a, b);
        }
    }

    private void paintPreview(Graphics2D g2) {
        if (previewFrom != null && previewTo != null) {
            g2.setColor(new Color(255, 220, 120));
            g2.setStroke(PREVIEW_STROKE);
            drawOrth(g2, previewFrom.outputPort(), previewTo);
        }
    }

    // Orthogonal polyline using midpoint rule
    private void drawOrth(Graphics2D g2, Point a, Point b) {
        int xmid = a.x + (b.x - a.x) / 2;
        Path2D path = new Path2D.Double();
        path.moveTo(a.x, a.y);
        path.lineTo(xmid, a.y);
        path.lineTo(xmid, b.y);
        path.lineTo(b.x, b.y);
        g2.draw(path);
    }

    // ---- Small helpers used by the controller ----
    public Model.Node nodeAt(Point p) {
        for (int i = model.nodes.size() - 1; i >= 0; --i) { // top-most first
            Model.Node n = model.nodes.get(i);
            if (n.bounds.contains(p)) return n;
        }
        return null;
    }
    public Model.Node nodeWithOutputAt(Point p) {
        for (int i = model.nodes.size() - 1; i >= 0; --i) {
            Model.Node n = model.nodes.get(i);
            if (n.outputPortRect(PORT_SIZE).contains(p)) return n;
        }
        return null;
    }
    public Model.Node nodeWithInputAt(Point p) {
        for (int i = model.nodes.size() - 1; i >= 0; --i) {
            Model.Node n = model.nodes.get(i);
            if (n.inputPortRect(PORT_SIZE).contains(p)) return n;
        }
        return null;
    }
}
