package utastudents.jsbsimedit2.core.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Holds all NodeModels for the current layout (UI).

public class CanvasModel {
    public final List<NodeModel> nodes = new ArrayList<>();

    public void clear() { nodes.clear(); }

    public NodeModel addNode(String type, int x, int y) {
        String id = "N" + UUID.randomUUID().toString().substring(0, 8);
        NodeModel m = new NodeModel(id, type, x, y);
        nodes.add(m);
        return m;
    }

    public void removeById(String id) {
        nodes.removeIf(n -> n.id.equals(id));
    }
}
