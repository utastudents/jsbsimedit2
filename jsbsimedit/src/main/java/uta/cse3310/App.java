package uta.cse3310;

import uta.cse3310.ui.JaxbLoader;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.*;

/**
 * Option B: “JSBSim Configuration Explorer”
 * - Uses generated JAXB classes (package `generated`) to unmarshal fdm_config XML
 * - Reflection-based rendering, resilient to exact getter names
 */
public class App extends JFrame {

    private final JTabbedPane tabs = new JTabbedPane();
    private File loadedFile;
    private Object root; // generated.FdmConfig instance

    public App() {
        super("JSBSim Config Explorer (JAXB)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(buildToolbar(), BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);

        // blank state tabs
        showWelcomeTabs();
    }

    // ===== Toolbar =====

    private JComponent buildToolbar() {
        var bar = new JToolBar();
        bar.setFloatable(false);

        var btnLoad = new JButton("Load XML…");
        btnLoad.addActionListener(this::onLoadXml);
        bar.add(btnLoad);

        var btnReload = new JButton("Reload");
        btnReload.addActionListener(e -> {
            if (loadedFile != null) loadAndRender(loadedFile);
        });
        bar.add(btnReload);

        bar.add(Box.createHorizontalStrut(12));

        var lbl = new JLabel(" Tip: start with f16.xml");
        lbl.setForeground(new Color(0x555555));
        bar.add(lbl);

        return bar;
    }

    private void onLoadXml(ActionEvent e) {
        var chooser = new JFileChooser(new File("src/main/resources"));
        chooser.setDialogTitle("Select JSBSim fdm_config XML (e.g., f16.xml)");
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            loadAndRender(chooser.getSelectedFile());
        }
    }

    // ===== Rendering flow =====

    private void loadAndRender(File xml) {
        try {
            Object obj = JaxbLoader.load(xml, "generated"); // package of your generated classes
            if (obj == null) {
                JOptionPane.showMessageDialog(this, "No root object returned by JAXB (null).",
                        "Load error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            loadedFile = xml;
            root = obj;
            buildAllTabs();
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Failed to load XML:\n" + ex.getMessage(),
                    "Load error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showWelcomeTabs() {
        tabs.removeAll();
        var p = new JPanel(new BorderLayout());
        p.add(new JLabel("Load an fdm_config XML to begin (e.g., f16.xml).",
                SwingConstants.CENTER), BorderLayout.CENTER);
        tabs.addTab("Welcome", p);
    }

    private void buildAllTabs() {
        tabs.removeAll();

        // 1) Overview (tries to read common top-level props)
        tabs.addTab("Overview", buildOverviewTab());

        // 2) Structure (JTree explorer)
        tabs.addTab("Structure", buildStructureTab());

        // 3) Named / logical tabs — each tab tries multiple property names and
        // falls back gracefully if not present in this specific JAXB schema.

        tabs.addTab("File Header", buildBeanTab(findOne(root,
                "fileHeader", "fileheader", "header")));
        tabs.addTab("Metrics", buildBeanTab(findOne(root, "metrics")));

        // Mass balance (and CG/point masses inside)
        tabs.addTab("Mass / Balance", buildBeanTab(findOne(root,
                "massBalance", "massbalance", "mass_and_balance", "mass_properties")));

        // Ground reactions (contacts list)
        tabs.addTab("Ground Reactions", buildBeanOrListTab(findOne(root,
                "groundReactions", "ground_reactions", "ground")));

        // External reactions (forces)
        tabs.addTab("External Reactions", buildBeanOrListTab(findOne(root,
                "externalReactions", "external_reactions", "external")));

        // Propulsion (engines, tanks, thrusters)
        tabs.addTab("Propulsion", buildBeanOrListTab(findOne(root,
                "propulsion", "enginesAndTanks", "engineSystem")));

        // Flight control (channels, gains, pid, switches, etc.)
        tabs.addTab("Flight Control", buildBeanOrListTab(findOne(root,
                "flightControl", "flight_control", "fcs", "fc")));

        // Aerodynamics (axes, functions, tables, etc.)
        tabs.addTab("Aerodynamics", buildBeanOrListTab(findOne(root,
                "aerodynamics", "aero")));

        // Raw XML (read file text)
        tabs.addTab("Raw XML", buildRawXmlTab(loadedFile));
    }

    // ===== Specific tabs =====

    private JComponent buildOverviewTab() {
        var text = new JTextArea();
        text.setEditable(false);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));

        StringBuilder sb = new StringBuilder();
        sb.append("== Overview ==\n\n");
        sb.append("File : ").append(loadedFile != null ? loadedFile.getAbsolutePath() : "-").append("\n");

        // Common root fields (best-effort)
        Object name = getAny(root, "name");
        Object version = getAny(root, "version", "versionAttr");
        Object release = getAny(root, "release");
        if (name != null || version != null || release != null) {
            sb.append("Root: name=").append(str(name))
                    .append("  version=").append(str(version))
                    .append("  release=").append(str(release))
                    .append("\n");
        }

        // Quick counts
        Object propulsion = findOne(root, "propulsion", "engineSystem");
        int engineCount = countIn(propulsion, "engines", "engine");
        int tankCount   = countIn(propulsion, "tanks", "tank");

        Object ground = findOne(root, "groundReactions", "ground_reactions");
        int contactCount = countIn(ground, "contacts", "contact");

        Object ext = findOne(root, "externalReactions", "external_reactions");
        int forceCount = countIn(ext, "forces", "force");

        Object fcs = findOne(root, "flightControl", "flight_control", "fcs");
        int channelCount = countIn(fcs, "channels", "channel");

        Object aero = findOne(root, "aerodynamics", "aero");
        int axisCount = countIn(aero, "axes", "axis");
        int funcCount = countIn(aero, "functions", "function");

        sb.append("\n== Quick Counts ==\n");
        sb.append("Engines: ").append(engineCount).append("   Tanks: ").append(tankCount).append("\n");
        sb.append("Contacts: ").append(contactCount).append("   External Forces: ").append(forceCount).append("\n");
        sb.append("FCS Channels: ").append(channelCount).append("\n");
        sb.append("Aero Axes: ").append(axisCount).append("   Aero Functions: ").append(funcCount).append("\n");

        text.setText(sb.toString());
        return new JScrollPane(text);
    }

    private JComponent buildStructureTab() {
        var rootNode = toTreeNode("fdm_config", root, new IdentityHashMap<>());
        var tree = new JTree(new DefaultTreeModel(rootNode));
        tree.setRootVisible(true);

        var beanPanel = new JPanel(new BorderLayout());
        var table = new JTable();
        var tip = new JLabel("Select a node to inspect its bean properties", SwingConstants.CENTER);
        beanPanel.add(tip, BorderLayout.CENTER);

        tree.addTreeSelectionListener((TreeSelectionEvent e) -> {
            var n = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (n == null) return;
            Object val = n.getUserObject();
            if (val instanceof NodeBean nb && nb.bean() != null) {
                var model = new BeanTableModel(nb.bean());
                table.setModel(model);
                beanPanel.removeAll();
                beanPanel.add(new JScrollPane(table), BorderLayout.CENTER);
                beanPanel.revalidate();
                beanPanel.repaint();
            }
        });

        var split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                new JScrollPane(tree), beanPanel);
        split.setResizeWeight(0.35);
        return split;
    }

    private JComponent buildBeanTab(Object bean) {
        if (bean == null) return emptyTab("Section not present in this model.");
        var table = new JTable(new BeanTableModel(bean));
        return new JScrollPane(table);
    }

    private JComponent buildBeanOrListTab(Object beanOrContainer) {
        if (beanOrContainer == null) return emptyTab("Section not present in this model.");

        // Try to extract obvious child collections (e.g., contacts, forces, engines, tanks, channels, axes, functions)
        List<ListItem> lists = new ArrayList<>();
        lists.add(listOf(beanOrContainer, "contacts", "contact"));
        lists.add(listOf(beanOrContainer, "forces", "force"));
        lists.add(listOf(beanOrContainer, "engines", "engine"));
        lists.add(listOf(beanOrContainer, "tanks", "tank"));
        lists.add(listOf(beanOrContainer, "channels", "channel"));
        lists.add(listOf(beanOrContainer, "axes", "axis"));
        lists.add(listOf(beanOrContainer, "functions", "function"));

        // Filter non-empty
        lists.removeIf(Objects::isNull);

        if (lists.isEmpty()) {
            // Just render the bean
            return buildBeanTab(beanOrContainer);
        }

        var tabsLocal = new JTabbedPane();
        tabsLocal.addTab("Summary", buildBeanTab(beanOrContainer));
        for (ListItem li : lists) {
            tabsLocal.addTab(li.title(), buildListTab(li.title(), li.items()));
        }
        return tabsLocal;
    }

    private JComponent buildListTab(String label, List<?> items) {
        if (items == null || items.isEmpty()) return emptyTab("No " + label + " found.");
        var list = new JList<>(items.toArray());
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> l, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(l, value, index, isSelected, cellHasFocus);
                setText(summary(value));
                setToolTipText(value != null ? value.getClass().getName() : null);
                return c;
            }
        });

        var table = new JTable();
        var right = new JPanel(new BorderLayout());
        right.add(new JLabel("Details"), BorderLayout.NORTH);
        right.add(new JScrollPane(table), BorderLayout.CENTER);

        list.addListSelectionListener(ev -> {
            Object sel = list.getSelectedValue();
            if (sel != null) table.setModel(new BeanTableModel(sel));
        });

        var split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(list), right);
        split.setResizeWeight(0.40);
        return split;
    }

    private JComponent buildRawXmlTab(File file) {
        var text = new JTextArea();
        text.setEditable(false);
        text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        if (file != null && file.exists()) {
            try {
                text.setText(Files.readString(file.toPath(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                text.setText("Failed to read file:\n" + e.getMessage());
            }
        } else {
            text.setText("No file loaded.");
        }
        return new JScrollPane(text);
    }

    private JComponent emptyTab(String msg) {
        var p = new JPanel(new BorderLayout());
        p.add(new JLabel(msg, SwingConstants.CENTER), BorderLayout.CENTER);
        return p;
    }

    // ===== Tree building =====

    private record NodeBean(String label, Object bean) {
        @Override public String toString() { return label; }
    }

    private DefaultMutableTreeNode toTreeNode(String label, Object bean, IdentityHashMap<Object, Boolean> seen) {
        var node = new DefaultMutableTreeNode(new NodeBean(label, bean));
        if (bean == null) return node;
        if (isLeafType(bean.getClass())) return node;

        if (seen.containsKey(bean)) return node; // avoid cycles
        seen.put(bean, Boolean.TRUE);

        if (bean instanceof Collection<?> col) {
            int i = 0;
            for (Object it : col) {
                node.add(toTreeNode(label + "[" + i + "]", it, seen));
                i++;
            }
            return node;
        }

        // Inspect bean properties
        Map<String, Object> props = readProperties(bean);
        for (var entry : props.entrySet()) {
            Object val = entry.getValue();
            String childLabel = entry.getKey();
            if (val == null) continue;

            if (val instanceof Collection<?> c) {
                var parent = new DefaultMutableTreeNode(new NodeBean(childLabel + " (" + c.size() + ")", c));
                node.add(parent);
                int i = 0;
                for (Object it : c) {
                    parent.add(toTreeNode(childLabel + "[" + i + "]", it, seen));
                    i++;
                }
            } else if (isLeafType(val.getClass())) {
                node.add(new DefaultMutableTreeNode(new NodeBean(childLabel + " = " + str(val), val)));
            } else {
                node.add(toTreeNode(childLabel, val, seen));
            }
        }
        return node;
    }

    // ===== Reflection helpers & models =====

    private static Object getAny(Object bean, String... candidates) {
        if (bean == null) return null;
        Class<?> cls = bean.getClass();
        for (String name : candidates) {
            // Getter
            for (String prefix : List.of("get", "is")) {
                String m = prefix + camel(name);
                try {
                    Method mt = cls.getMethod(m);
                    return mt.invoke(bean);
                } catch (NoSuchMethodException ignored) {
                } catch (Exception ex) { return null; }
            }
            // Field
            try {
                Field f = cls.getDeclaredField(name);
                f.setAccessible(true);
                return f.get(bean);
            } catch (NoSuchFieldException ignored) {
            } catch (Exception ex) { return null; }
        }
        return null;
    }

    private static Object findOne(Object bean, String... candidates) {
        return getAny(bean, candidates);
    }

    private static ListItem listOf(Object bean, String plural, String singular) {
        if (bean == null) return null;

        Object list = getAny(bean, plural);
        if (list instanceof Collection<?> c && !c.isEmpty())
            return new ListItem(cap(plural), new ArrayList<>(c));

        // Some JAXB gens only expose singular with @XmlElementWrapper or direct repeated props
        list = getAny(bean, singular);
        if (list instanceof Collection<?> c2 && !c2.isEmpty())
            return new ListItem(cap(plural), new ArrayList<>(c2));

        // If singular returns a single bean (not a list), wrap into list
        Object single = getAny(bean, singular);
        if (single != null && !(single instanceof Collection)) {
            return new ListItem(cap(plural), List.of(single));
        }
        return null;
    }

    private static class ListItem {
        private final String title;
        private final List<?> items;
        ListItem(String title, List<?> items) { this.title = title; this.items = items; }
        String title() { return title; }
        List<?> items() { return items; }
    }

    private static String camel(String name) {
        if (name == null || name.isEmpty()) return name;
        // e.g. release_attr → ReleaseAttr, file_header → FileHeader
        String[] parts = name.replace('-', '_').split("_");
        StringBuilder sb = new StringBuilder();
        for (String p : parts) {
            if (p.isEmpty()) continue;
            sb.append(Character.toUpperCase(p.charAt(0)));
            if (p.length() > 1) sb.append(p.substring(1));
        }
        return sb.toString();
    }

    private static String cap(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String summary(Object bean) {
        if (bean == null) return "(null)";
        // try 'name' first, then 'type', then 'file', else class@hash
        Object name = getAny(bean, "name");
        Object type = getAny(bean, "type");
        Object file = getAny(bean, "file");
        String base = bean.getClass().getSimpleName();
        List<String> bits = new ArrayList<>();
        if (name != null) bits.add("name=" + str(name));
        if (type != null) bits.add("type=" + str(type));
        if (file != null) bits.add("file=" + str(file));
        return bits.isEmpty() ? base : base + " (" + String.join(", ", bits) + ")";
    }

    private static int countIn(Object bean, String plural, String singular) {
        if (bean == null) return 0;
        Object list = getAny(bean, plural);
        if (list instanceof Collection<?> c) return c.size();
        Object single = getAny(bean, singular);
        if (single instanceof Collection<?> c2) return c2.size();
        return single != null ? 1 : 0;
    }

    private static boolean isLeafType(Class<?> t) {
        return t.isPrimitive()
                || Number.class.isAssignableFrom(t)
                || CharSequence.class.isAssignableFrom(t)
                || Boolean.class.isAssignableFrom(t)
                || Enum.class.isAssignableFrom(t)
                || t.getName().startsWith("java.time.")
                || t.getName().startsWith("java.lang.");
    }

    private static Map<String, Object> readProperties(Object bean) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (bean == null) return map;
        Class<?> cls = bean.getClass();

        // Methods (getX/isX)
        for (Method m : cls.getMethods()) {
            if (m.getParameterCount() == 0 && Modifier.isPublic(m.getModifiers())) {
                String n = m.getName();
                if (n.equals("getClass")) continue;
                if ((n.startsWith("get") && n.length() > 3) || (n.startsWith("is") && n.length() > 2)) {
                    String prop = n.startsWith("get") ? n.substring(3) : n.substring(2);
                    prop = Character.toLowerCase(prop.charAt(0)) + prop.substring(1);
                    try {
                        Object val = m.invoke(bean);
                        // Skip empty collections to reduce noise — still visible under Structure tree
                        if (val instanceof Collection<?> c && c.isEmpty()) continue;
                        map.put(prop, val);
                    } catch (Exception ignored) {}
                }
            }
        }
        return map;
    }

    private static String str(Object v) {
        if (v == null) return "-";
        if (v instanceof Double d) {
            if (d.isNaN()) return "NaN";
            return String.format(Locale.US, "%.6f", d);
        }
        return v.toString();
    }

    // ===== Table model that reflects bean getters =====

    private static class BeanTableModel extends AbstractTableModel {
        private final List<Map.Entry<String, Object>> rows;

        BeanTableModel(Object bean) {
            Map<String, Object> props = readProperties(bean);
            rows = new ArrayList<>(props.entrySet());
        }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return 2; }
        @Override public String getColumnName(int col) { return col == 0 ? "Property" : "Value"; }

        @Override public Object getValueAt(int rowIndex, int columnIndex) {
            var e = rows.get(rowIndex);
            return columnIndex == 0 ? e.getKey() : valueToString(e.getValue());
        }

        private String valueToString(Object v) {
            if (v == null) return "-";
            if (v instanceof Collection<?> c) {
                return "[" + c.size() + " item(s)]";
            }
            if (v.getClass().isArray()) {
                return Arrays.toString((Object[]) v);
            }
            if (isLeafType(v.getClass())) return str(v);
            // show a compact summary for beans
            return summary(v);
        }
    }

    // ===== main =====
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new App().setVisible(true));
    }
}
