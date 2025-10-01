package utastudents.jsbsimedit2.core.domain;

// UI layout model for a single node.

import java.util.ArrayList;
import java.util.List;

public class NodeModel {
    public final String id;
    public final String type;
    public int x, y;

    // Each node can have ports (like input and output connections)
    public static class Port {
        public final String id;      // port id (unique name)
        public final boolean input;  // true = input, false = output
        public final float nx, ny;   // position inside the node (0 to 1)

        public Port(String id, boolean input, float nx, float ny) {
            this.id = id;
            this.input = input;
            this.nx = nx;
            this.ny = ny;
        }
    }

    // list that holds all the ports for this node
    public final List<Port> ports = new ArrayList<>();

    public NodeModel(String id, String type, int x, int y) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;

        // by default, each node will have one input port (left side)
        ports.add(new Port(id + ":in", true, 0.02f, 0.50f));

        // and one output port (right side)
        ports.add(new Port(id + ":out", false, 0.98f, 0.50f));
    }
}
