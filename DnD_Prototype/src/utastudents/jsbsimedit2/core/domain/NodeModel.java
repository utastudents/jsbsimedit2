package utastudents.jsbsimedit2.core.domain;

// UI layout model for a single node.

public class NodeModel {
    public final String id;
    public final String type;
    public int x, y;

    public NodeModel(String id, String type, int x, int y) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
    }
}
