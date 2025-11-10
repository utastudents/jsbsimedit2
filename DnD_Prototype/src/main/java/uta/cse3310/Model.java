import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.HashMap;





public class Model {

    // ---- Node types shown in the palette ----
    public enum NodeType {
        SOURCE("Source", 0, 1),
        DESTINATION("Destination", 1, 0),
        SUMMER("Summer", 2, 1),
        PID("PID", 1, 1),
        GAIN("Gain", 1, 1),
        FILTER("Filter", 1, 1),
        DEAD_BAND("Dead Band", 1, 1),
        SWITCH("Switch", 2, 1),
        KINEMAT("Kinemat", 1, 1),
        FCSFUNCTION("FCSFunction", 1, 1);

        public final String label;
        public final int inPorts;
        public final int outPorts;

        NodeType(String label, int inPorts, int outPorts) {
            this.label = label;
            this.inPorts = inPorts;
            this.outPorts = outPorts;
        }
        @Override public String toString() { return label; }

        public static NodeType fromLabel(String s) {
            for (NodeType t : values()) {
                if (t.label.equalsIgnoreCase(s)) return t;
            }
            return GAIN;
        }
    }

    // ---- Graph primitives ----
    public static final class Node {
        public final int id;
        public NodeType type;
        public Rectangle bounds; // x,y,width,height

        public Node(int id, NodeType type, int x, int y) {
            this.id = id;
            this.type = type;
            this.bounds = new Rectangle(x, y, 140, 70);
        }

        // Single input on left-center; single output on right-center (prototype simplicity)
        public Point inputPort() {
            return new Point(bounds.x, bounds.y + bounds.height / 2);
        }
        public Point outputPort() {
            return new Point(bounds.x + bounds.width, bounds.y + bounds.height / 2);
        }

        // Hit rectangles for ports
        public Rectangle inputPortRect(int size) {
            Point p = inputPort();
            return new Rectangle(p.x - size/2, p.y - size/2, size, size);
        }
        public Rectangle outputPortRect(int size) {
            Point p = outputPort();
            return new Rectangle(p.x - size/2, p.y - size/2, size, size);
        }
    }

    public static final class Edge {
        public final Node from; // uses output port
        public final Node to;   // uses input port
        public Edge(Node from, Node to) {
            this.from = from;
            this.to = to;
        }
    }

    // ---- The graph model ----
    private int nextId = 1;
    public final List<Node> nodes = new ArrayList<>();
    public final List<Edge> edges = new ArrayList<>();

    public Node addNode(NodeType type, int x, int y) {
        Node n = new Node(nextId++, type, x, y);
        nodes.add(n);
        return n;
    }
    public void addEdge(Node from, Node to) {
        if (from != null && to != null && from != to) {
            edges.add(new Edge(from, to));
        }
    }
    //  Depth first search
    public List<Node> dfs(Node start) {
        List<Node> visited = new ArrayList<>();
        dfsVisit(start, visited);
        return visited;
    }

    private void dfsVisit(Node n, List<Node> visited) {
        if (n == null || visited.contains(n))
            return;

        visited.add(n);

        // Follow outgoing edges
        for (Edge e : edges) {
            if (e.from == n) {
                dfsVisit(e.to, visited);
            }
        }
    }


        // SAVE GRAPH TO XML 
    public void saveToXML(File file) throws Exception {
        // Pick a root node: first node if nothing specified
        Node root = nodes.isEmpty() ? null : nodes.get(0);
        List<Node> ordered = root == null ? new ArrayList<>() : dfs(root);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element graph = doc.createElement("Graph");
        doc.appendChild(graph);

        // Save nodes in DFS order
        for (Node n : ordered) {
            Element nodeEl = doc.createElement("Node");
            nodeEl.setAttribute("id", String.valueOf(n.id));
            nodeEl.setAttribute("type", n.type.label);
            nodeEl.setAttribute("x", String.valueOf(n.bounds.x));
            nodeEl.setAttribute("y", String.valueOf(n.bounds.y));
            graph.appendChild(nodeEl);
        }

        // Save edges separately
        for (Edge e : edges) {
            Element edgeEl = doc.createElement("Edge");
            edgeEl.setAttribute("from", String.valueOf(e.from.id));
            edgeEl.setAttribute("to", String.valueOf(e.to.id));
            graph.appendChild(edgeEl);
        }

        // Write file
        Transformer tf = TransformerFactory.newInstance().newTransformer();
        tf.setOutputProperty(OutputKeys.INDENT, "yes");
        tf.transform(new DOMSource(doc), new StreamResult(file));
    }


        // LOAD GRAPH FROM XML
    public void loadFromXML(File file) throws Exception {
        nodes.clear();
        edges.clear();
        nextId = 1;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(file);

        NodeList nodeList = doc.getElementsByTagName("Node");

        // 1. Load nodes
        for (int i = 0; i < nodeList.getLength(); i++) {
            Element el = (Element) nodeList.item(i);

            int id = Integer.parseInt(el.getAttribute("id"));
            String type = el.getAttribute("type");
            int x = Integer.parseInt(el.getAttribute("x"));
            int y = Integer.parseInt(el.getAttribute("y"));

            NodeType t = NodeType.fromLabel(type);
            Node n = new Node(id, t, x, y);

            nodes.add(n);
            nextId = Math.max(nextId, id + 1);
        }

        // Build a lookup table for edges
        Map<Integer, Node> map = new HashMap<>();
        for (Node n : nodes)
            map.put(n.id, n);

        // 2. Load edges
        NodeList edgeList = doc.getElementsByTagName("Edge");
        for (int i = 0; i < edgeList.getLength(); i++) {
            Element el = (Element) edgeList.item(i);

            int from = Integer.parseInt(el.getAttribute("from"));
            int to = Integer.parseInt(el.getAttribute("to"));

            Node nf = map.get(from);
            Node nt = map.get(to);

            if (nf != null && nt != null)
                edges.add(new Edge(nf, nt));
        }
    }




}
