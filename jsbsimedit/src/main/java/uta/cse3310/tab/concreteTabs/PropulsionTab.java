package uta.cse3310.tab.concreteTabs;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Stack;
import java.util.ArrayList;
import uta.cse3310.tab.simpleTab;
import uta.cse3310.dataStore;
import uta.cse3310.tabFrame;

import generated.Engine;
import generated.Mass;
import generated.Tank;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;



public class PropulsionTab extends simpleTab 
{
    private JTabbedPane tabbedPane;
    private JList<String> availableEnginesList;
    private JList<String> availableThrustersList;
    private DefaultListModel<String> enginesModel;
    private DefaultListModel<String> thrustersModel;
    private DefaultListModel<String> subscribedModel;
    private DefaultListModel<String> tanksModel;
    private JList<String> tanksList;
    private Stack<Runnable> undoStack = new Stack<>();

    public PropulsionTab(tabFrame tf, dataStore ds, String label) 
    {
        super(ds, label);
        TF = tf;

        panel.setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();

        // Available Engines tab
        enginesModel = new DefaultListModel<>();
        availableEnginesList = new JList<>(enginesModel);
        JScrollPane enginesScroll = new JScrollPane(availableEnginesList);
        tabbedPane.addTab("Available Engines", enginesScroll);

        // Available Thrusters tab
        thrustersModel = new DefaultListModel<>();
        availableThrustersList = new JList<>(thrustersModel);
        JScrollPane thrustersScroll = new JScrollPane(availableThrustersList);
        tabbedPane.addTab("Available Thrusters", thrustersScroll);

        // Subscribed Engines tab
        subscribedModel = new DefaultListModel<>();
        JList<String> subscribedList = new JList<>(subscribedModel);
        JScrollPane subscribedScroll = new JScrollPane(subscribedList);
        JPanel subscribedPanel = new JPanel(new BorderLayout());
        subscribedPanel.add(subscribedScroll, BorderLayout.CENTER);

        JPanel subscribedButtons = new JPanel();
        JButton newPair = new JButton("New Pair");
        JButton deletePair = new JButton("Delete Pair");
        JButton detailPair = new JButton("Detail Pair");
        subscribedButtons.add(newPair);
        subscribedButtons.add(deletePair);
        subscribedButtons.add(detailPair);
        subscribedPanel.add(subscribedButtons, BorderLayout.SOUTH);

        tabbedPane.addTab("Subscribed Engine(s)(*)", subscribedPanel);

        // Tanks tab
        tanksModel = new DefaultListModel<>();
        tanksList = new JList<>(tanksModel);
        JScrollPane tanksScroll = new JScrollPane(tanksList);
        JPanel tanksPanel = new JPanel(new BorderLayout());
        tanksPanel.add(tanksScroll, BorderLayout.CENTER);

        JPanel tankButtons = new JPanel();
        JButton newTank = new JButton("New Tank");
        JButton deleteTank = new JButton("Delete Tank");
        JButton detailTank = new JButton("Detail Tank");
        tankButtons.add(newTank);
        tankButtons.add(deleteTank);
        tankButtons.add(detailTank);
        tanksPanel.add(tankButtons, BorderLayout.SOUTH);

        tabbedPane.addTab("Tanks", tanksPanel);

        panel.add(tabbedPane, BorderLayout.CENTER);
    }


    private void savePropulsionToXML() 
    {
        try 
            {
                // Make a clean list for JAXB marshalling
                List<Object> originalList = DS.cfg.getPropulsion().getDocumentationOrPropertyOrFunction();
                List<Object> jaxbSafeList = new ArrayList<>();

                // Only JAXB-annotated objects (Tank, Engine, etc.) are allowed
                for (Object obj : originalList) 
                    {
                        if (obj instanceof Tank || obj instanceof Engine) 
                            {
                                jaxbSafeList.add(obj);
                            }   
                    }

                // Temporarily replace the list with JAXB-safe objects
                List<Object> backupList = new ArrayList<>(originalList);
                originalList.clear();
                originalList.addAll(jaxbSafeList);

                // Marshal to XML
                String filePath = DS.fileName;
                JAXBContext jaxbContext = JAXBContext.newInstance(DS.cfg.getClass());
                Marshaller marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                marshaller.marshal(DS.cfg, new java.io.File(filePath));

                // Restore original list including PAIR strings
                originalList.clear();
                originalList.addAll(backupList);

                JOptionPane.showMessageDialog(panel,
                        "Propulsion data successfully saved back to XML:\n" + filePath,
                        "Save Successful",
                        JOptionPane.INFORMATION_MESSAGE);

            } 
                catch (Exception ex) 
                {
                    JOptionPane.showMessageDialog
                    (
                            panel,
                            "Failed to save XML:\n" + ex.getMessage(),
                            "Save Error",
                            JOptionPane.ERROR_MESSAGE
                    );
                    ex.printStackTrace();
                }
    }

    private void undoLastAction() {
        if (!undoStack.isEmpty()) {
            undoStack.pop().run();
            loadData(); // refresh UI
        } else {
            JOptionPane.showMessageDialog(panel, "Nothing to undo.");
        }
    }




    @Override
    public void loadData() 
    {
            
            panel.removeAll();
            panel.setLayout(null);

            // Clear existing models instead of redeclaring new ones
            enginesModel.clear();
            thrustersModel.clear();
            subscribedModel.clear();
            tanksModel.clear();

            List<String> engineErrors = new ArrayList<>();
            // Load engines and tanks from XML
            for (Object obj : DS.cfg.getPropulsion().getDocumentationOrPropertyOrFunction()) 
                {
                    if (obj instanceof Engine) 
                        {
                            Engine eng = (Engine) obj;

                            String name = eng.getName();
                            String file = eng.getFile();

                            boolean ok = true;
                            StringBuilder err = new StringBuilder("Engine import error: ");

                            if (file == null || file.trim().isEmpty()) 
                                {
                                    ok = false;
                                    err.append("[missing file] ");
                                }

                            if (!ok) 
                                {
                                    // Record error and skip adding this engine to the lists
                                    engineErrors.add(err.toString());
                                    continue;
                                }
                            if (name == null || name.trim().isEmpty()) 
                                {
                                    name = file;
                                }

                            String display = name + "  (" + file + ")";
                            enginesModel.addElement(display);
                            thrustersModel.addElement(display);
                        }

                    else if (obj instanceof String && ((String) obj).startsWith("PAIR:")) 
                    {
                        String pair = ((String) obj).substring(5).trim(); // remove "PAIR:" prefix
                        subscribedModel.addElement(pair);
                        
                    }

                    else if (obj instanceof Tank)  
                    {
                        Tank t = (Tank) obj;
                        Mass cap = t.getCapacity();
                        String capStr = cap != null ? cap.getValue() + " " + cap.getUnit() : "0 LBS";
                        String text = "Tank (" + t.getType() + ") capacity=" + capStr;
                        tanksModel.addElement(text);
                    }
                }

            if (!engineErrors.isEmpty()) 
                {
                    StringBuilder msg = new StringBuilder();
                    msg.append("Some engine definitions in the XML are invalid:\n\n");
                    for (String s : engineErrors) 
                        {
                            msg.append("• ").append(s).append("\n");
                        }
                    JOptionPane.showMessageDialog
                    (
                            panel,
                            msg.toString(),
                            "Engine Import Warnings",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

            // Engines Tab
            JLabel lblEng = new JLabel("Available Engines:");
            lblEng.setBounds(10, 10, 200, 20);
            panel.add(lblEng);

            JList<String> enginesList = new JList<>(enginesModel);
            JScrollPane scrollEng = new JScrollPane(enginesList);
            scrollEng.setBounds(10, 35, 300, 150);
            panel.add(scrollEng);

            // Thrusters Tab
            JLabel lblThr = new JLabel("Available Thrusters:");
            lblThr.setBounds(330, 10, 200, 20);
            panel.add(lblThr);

            JList<String> thrustersList = new JList<>(thrustersModel);
            JScrollPane scrollThr = new JScrollPane(thrustersList);
            scrollThr.setBounds(330, 35, 300, 150);
            panel.add(scrollThr);

            // Subscribed Engines Tab
            JLabel lblSub = new JLabel("Subscribed Engine(s)(*):");
            lblSub.setBounds(10, 200, 300, 20);
            panel.add(lblSub);

            JList<String> subList = new JList<>(subscribedModel);
            JScrollPane scrollSub = new JScrollPane(subList);
            scrollSub.setBounds(10, 225, 300, 150);
            panel.add(scrollSub);

            JButton btnNewPair = new JButton("New Pair");
            btnNewPair.setBounds(10, 385, 100, 25);
            panel.add(btnNewPair);

            JButton btnDelPair = new JButton("Delete Pair");
            btnDelPair.setBounds(120, 385, 120, 25);
            panel.add(btnDelPair);

            JButton btnDetailPair = new JButton("Detail Pair");
            btnDetailPair.setBounds(250, 385, 120, 25);
            panel.add(btnDetailPair);

            // Tanks Tab
            JLabel lblTanks = new JLabel("Tanks:");
            lblTanks.setBounds(330, 200, 200, 20);
            panel.add(lblTanks);

            // Use the class-level tanksList (do NOT redeclare)
            tanksList = new JList<>(tanksModel);
            JScrollPane scrollTanks = new JScrollPane(tanksList);
            scrollTanks.setBounds(330, 225, 300, 150);
            panel.add(scrollTanks);

            JButton btnNewTank = new JButton("New Tank");
            btnNewTank.setBounds(380, 385, 100, 25);
            panel.add(btnNewTank);

            JButton btnDelTank = new JButton("Delete Tank");
            btnDelTank.setBounds(490, 385, 120, 25);
            panel.add(btnDelTank);

            JButton btnDetailTank = new JButton("Detail Tank");
            btnDetailTank.setBounds(620, 385, 120, 25);
            panel.add(btnDetailTank);

            // undo button

            JButton btnUndo = new JButton("Undo");
            btnUndo.setBounds(750, 385, 100, 25);
            panel.add(btnUndo);

            btnUndo.addActionListener(e -> undoLastAction());


            // SAVE BUTTON
            JButton btnSave = new JButton("Save");
            btnSave.setBounds(860, 385, 100, 25);
            panel.add(btnSave);

            btnSave.addActionListener(e -> savePropulsionToXML());


            // Populate tanksModel
            tanksModel.clear();

            for (Object obj : DS.cfg.getPropulsion().getDocumentationOrPropertyOrFunction()) 
                {
                    if (obj instanceof Tank) 
                        {
                            Tank t = (Tank) obj;

                            String locStr = "(0,0,0) IN";
                            if (t.getLocation() != null) 
                                {
                                    locStr = "(" + t.getLocation().getX() + ", "
                                            + t.getLocation().getY() + ", "
                                            + t.getLocation().getZ() + ") "
                                            + t.getLocation().getUnit();
                                }

                            String capStr = "0 LBS";
                            if (t.getCapacity() != null) 
                                {
                                    capStr = t.getCapacity().getValue() + " " + t.getCapacity().getUnit();
                                }

                            String text = "Tank (" + t.getType() + ") location=" + locStr + " capacity=" + capStr;
                            tanksModel.addElement(text);
                        }
                }

            // BUTTON FUNCTIONALITY

            // New Pair
            btnNewPair.addActionListener
            (e -> 
                {
                    String eName = enginesList.getSelectedValue() != null ? enginesList.getSelectedValue() : "";
                    String tName = thrustersList.getSelectedValue() != null ? thrustersList.getSelectedValue() : "";

                    if (!eName.isEmpty() && !tName.isEmpty()) 
                        {
                        String pair = eName + " + " + tName;
                        subscribedModel.addElement(pair);
                        undoStack.push(() -> subscribedModel.removeElement(pair));
                    } else {
                        JOptionPane.showMessageDialog(panel, "Select both an engine and a thruster to create a pair.");
                    }
                }
            );

            btnDelPair.addActionListener
            (e -> 
                {
                    String selected = subList.getSelectedValue();
                    if (selected != null) {
                        undoStack.push(() -> subscribedModel.addElement(selected));
                        subscribedModel.removeElement(selected);
                    }
                });

                btnDetailPair.addActionListener(e -> {
                    String selected = subList.getSelectedValue();
                    if (selected != null) {
                        showPairEditDialog(selected);
                    }
                });

                // Tanks
            btnNewTank.addActionListener
            (e -> 
                {
            JTextField typeField = new JTextField();
            JTextField xField = new JTextField("0");
            JTextField yField = new JTextField("0");
            JTextField zField = new JTextField("0");
            JTextField locUnitField = new JTextField("IN");
            JTextField capField = new JTextField("0");
            JTextField capUnitField = new JTextField("LBS");

            JPanel inputPanel = new JPanel(new GridLayout(0, 2));
            inputPanel.add(new JLabel("Tank Type:")); inputPanel.add(typeField);
            inputPanel.add(new JLabel("Location X:")); inputPanel.add(xField);
            inputPanel.add(new JLabel("Location Y:")); inputPanel.add(yField);
            inputPanel.add(new JLabel("Location Z:")); inputPanel.add(zField);
            inputPanel.add(new JLabel("Location Unit:")); inputPanel.add(locUnitField);
            inputPanel.add(new JLabel("Capacity:")); inputPanel.add(capField);
            inputPanel.add(new JLabel("Capacity Unit:")); inputPanel.add(capUnitField);

            int result = JOptionPane.showConfirmDialog(panel, inputPanel, "New Tank", JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                String type = typeField.getText().trim();
                String locUnit = locUnitField.getText().trim().toUpperCase();
                String capUnit = capUnitField.getText().trim().toUpperCase();

                if (type.isEmpty() || tankTypeExists(type)) {
                    JOptionPane.showMessageDialog(panel, "Invalid or duplicate tank type.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Tank newTank = new Tank();
                newTank.setType(type);

                generated.Location loc = new generated.Location();
                loc.setX(Double.parseDouble(xField.getText().trim()));
                loc.setY(Double.parseDouble(yField.getText().trim()));
                loc.setZ(Double.parseDouble(zField.getText().trim()));
                loc.setUnit(generated.LengthUnit.fromValue(locUnit));
                newTank.setLocation(loc);

                Mass capacity = new Mass();
                capacity.setValue(Double.parseDouble(capField.getText().trim()));
                capacity.setUnit(generated.MassUnit.fromValue(capUnit));
                newTank.setCapacity(capacity);

                DS.cfg.getPropulsion().getDocumentationOrPropertyOrFunction().add(newTank);
                undoStack.push(() -> {
                    DS.cfg.getPropulsion().getDocumentationOrPropertyOrFunction().remove(newTank);
                });

                String locStr = "(" + xField.getText().trim() + ", " + yField.getText().trim() + ", " + zField.getText().trim() + ") " + locUnit;
                String text = "Tank (" + type + ") location=" + locStr + " capacity=" + capField.getText().trim() + " " + capUnit;
                tanksModel.addElement(text);
            }
        });

            btnDelTank.addActionListener(e -> {
                    String selected = tanksList.getSelectedValue();
                    int idx = tanksList.getSelectedIndex();
                    if (idx == -1) return;

                    // find Tank object in DS.cfg
                    Tank deletedTank = null;
                    int counter = 0;
                    for (Object obj : DS.cfg.getPropulsion().getDocumentationOrPropertyOrFunction()) {
                        if (obj instanceof Tank) {
                            if (counter == idx) {
                                deletedTank = (Tank) obj;
                                break;
                            }
                            counter++;
                        }
                    }

                    if (deletedTank != null) {

                        Tank finalTank = deletedTank;

                        // UNDO: add tank back
                        undoStack.push(() -> {
                            DS.cfg.getPropulsion().getDocumentationOrPropertyOrFunction().add(finalTank);
                        });

                        // remove from XML data
                        DS.cfg.getPropulsion().getDocumentationOrPropertyOrFunction().remove(deletedTank);

                        // remove from list model
                        tanksModel.remove(idx);
                    }

                    });

            btnDetailTank.addActionListener(e -> {
            int selectedIndex = tanksList.getSelectedIndex();
            if (selectedIndex == -1) {
                JOptionPane.showMessageDialog(panel, "Please select a tank to edit.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Find the Tank object in the XML
            Tank tank = null;
            int index = 0;
            for (Object obj : DS.cfg.getPropulsion().getDocumentationOrPropertyOrFunction()) {
                if (obj instanceof Tank) {
                    if (index == selectedIndex) {
                        tank = (Tank) obj;
                        break;
                    }
                    index++;
                }
            }

            if (tank == null) {
                JOptionPane.showMessageDialog(panel, "Tank not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            showTankEditDialog(tank, selectedIndex);
        }
        );
    }



    private void showTankEditDialog(Tank tank, int tankIndex) 
        {
            JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(panel), "Edit Tank", true);
            dialog.setSize(500, 400);
            dialog.setLocationRelativeTo(panel);

            JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
            formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            // Tank Type
            JTextField typeField = new JTextField(tank.getType() != null ? tank.getType() : "");
            formPanel.add(new JLabel("Tank Type:"));
            formPanel.add(typeField);

            // Location
            double locX = tank.getLocation() != null ? tank.getLocation().getX() : 0;
            double locY = tank.getLocation() != null ? tank.getLocation().getY() : 0;
            double locZ = tank.getLocation() != null ? tank.getLocation().getZ() : 0;
            String locUnit = tank.getLocation() != null && tank.getLocation().getUnit() != null
                    ? tank.getLocation().getUnit().value()
                    : "IN";

            JTextField xField = new JTextField(String.valueOf(locX));
            JTextField yField = new JTextField(String.valueOf(locY));
            JTextField zField = new JTextField(String.valueOf(locZ));
            JComboBox<String> locUnitCombo = new JComboBox<>(new String[]{"IN","FT","M"});
            locUnitCombo.setSelectedItem(locUnit);

            formPanel.add(new JLabel("Location X:")); formPanel.add(xField);
            formPanel.add(new JLabel("Location Y:")); formPanel.add(yField);
            formPanel.add(new JLabel("Location Z:")); formPanel.add(zField);
            formPanel.add(new JLabel("Location Unit:")); formPanel.add(locUnitCombo);

            // Capacity
            double capValue = tank.getCapacity() != null ? tank.getCapacity().getValue() : 0;
            String capUnit = tank.getCapacity() != null && tank.getCapacity().getUnit() != null
                    ? tank.getCapacity().getUnit().value()
                    : "LBS";
            JTextField capField = new JTextField(String.valueOf(capValue));
            JComboBox<String> capUnitCombo = new JComboBox<>(new String[]{"LBS","KG","GAL"});
            capUnitCombo.setSelectedItem(capUnit);

            formPanel.add(new JLabel("Capacity:")); formPanel.add(capField);
            formPanel.add(new JLabel("Capacity Unit:")); formPanel.add(capUnitCombo);

            // Contents
            double contentsValue = tank.getContents() != null ? tank.getContents().getValue() : 0;
            String contentsUnit = tank.getContents() != null && tank.getContents().getUnit() != null
                    ? tank.getContents().getUnit().value()
                    : "LBS";
            JTextField contentsField = new JTextField(String.valueOf(contentsValue));
            JComboBox<String> contentsUnitCombo = new JComboBox<>(new String[]{"LBS","KG","GAL"});
            contentsUnitCombo.setSelectedItem(contentsUnit);

            formPanel.add(new JLabel("Contents:")); formPanel.add(contentsField);
            formPanel.add(new JLabel("Contents Unit:")); formPanel.add(contentsUnitCombo);

            // Buttons
            JPanel buttonPanel = new JPanel();
            JButton okButton = new JButton("OK");
            JButton cancelButton = new JButton("Cancel");
            buttonPanel.add(okButton);
            buttonPanel.add(cancelButton);

            okButton.addActionListener
            (ev -> 
                {
                    try 
                        {
                            Tank oldCopy = new Tank();
                            oldCopy.setType(tank.getType());

                            generated.Location oldLoc = new generated.Location();
                            oldLoc.setX(tank.getLocation().getX());
                            oldLoc.setY(tank.getLocation().getY());
                            oldLoc.setZ(tank.getLocation().getZ());
                            oldLoc.setUnit(tank.getLocation().getUnit());
                            oldCopy.setLocation(oldLoc);

                            Mass oldCap = new Mass();
                            oldCap.setValue(tank.getCapacity().getValue());
                            oldCap.setUnit(tank.getCapacity().getUnit());
                            oldCopy.setCapacity(oldCap);

                            undoStack.push(() -> {
                                tank.setType(oldCopy.getType());
                                tank.getLocation().setX(oldCopy.getLocation().getX());
                                tank.getLocation().setY(oldCopy.getLocation().getY());
                                tank.getLocation().setZ(oldCopy.getLocation().getZ());
                                tank.getLocation().setUnit(oldCopy.getLocation().getUnit());
                                tank.getCapacity().setValue(oldCopy.getCapacity().getValue());
                                tank.getCapacity().setUnit(oldCopy.getCapacity().getUnit());
                            });
                            tank.setType(typeField.getText());

                            if (tank.getLocation() == null) tank.setLocation(new generated.Location());
                            tank.getLocation().setX(Double.parseDouble(xField.getText()));
                            tank.getLocation().setY(Double.parseDouble(yField.getText()));
                            tank.getLocation().setZ(Double.parseDouble(zField.getText()));
                            tank.getLocation().setUnit(generated.LengthUnit.fromValue((String) locUnitCombo.getSelectedItem()));

                            if (tank.getCapacity() == null) tank.setCapacity(new Mass());
                            tank.getCapacity().setValue(Double.parseDouble(capField.getText()));
                            tank.getCapacity().setUnit(generated.MassUnit.fromValue((String) capUnitCombo.getSelectedItem()));

                            if (tank.getContents() == null) tank.setContents(new Mass());
                            tank.getContents().setValue(Double.parseDouble(contentsField.getText()));
                            tank.getContents().setUnit(generated.MassUnit.fromValue((String) contentsUnitCombo.getSelectedItem()));

                            // --- Refresh ONLY the tanksModel entry ---
                            String locStr = "(" + tank.getLocation().getX() + ", "
                                    + tank.getLocation().getY() + ", "
                                    + tank.getLocation().getZ() + ") "
                                    + tank.getLocation().getUnit().value();
                            String capStr = tank.getCapacity().getValue() + " " + tank.getCapacity().getUnit().value();
                            String text = "Tank (" + tank.getType() + ") location=" + locStr + " capacity=" + capStr;

                            tanksModel.set(tankIndex, text);  // update the display

                            dialog.dispose();
                        }

                        catch (NumberFormatException ex) 
                            {
                                JOptionPane.showMessageDialog(dialog, "Invalid number format.", "Error", JOptionPane.ERROR_MESSAGE);
                            }
                }
            );

            cancelButton.addActionListener(ev -> dialog.dispose());

            dialog.setLayout(new BorderLayout());
            dialog.add(formPanel, BorderLayout.CENTER);
            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.setVisible(true);
        }   

    private void showPairEditDialog(String pair) 
    {
        // Example pair format: {F100-PW-229} + {F100-PW-229}
        String[] parts = pair.split("\\+"); // Split at "+"
        String engine = parts.length > 0 ? parts[0].trim().replace("{", "").replace("}", "") : "";
        String thruster = parts.length > 1 ? parts[1].trim().replace("{", "").replace("}", "") : "";

        JTextField engineField = new JTextField(engine);
        JTextField thrusterField = new JTextField(thruster);

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Engine:"));
        panel.add(engineField);
        panel.add(new JLabel("Thruster:"));
        panel.add(thrusterField);

        int result = JOptionPane.showConfirmDialog(panel, panel, "Edit Pair", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) 
            {
                String newPair = "{" + engineField.getText() + "} + {" + thrusterField.getText() + "}";
                int idx = subscribedModel.indexOf(pair);
                if (idx >= 0) 
                    {
                        undoStack.push(() -> subscribedModel.set(idx, pair));  // pair = old one
                        subscribedModel.set(idx, newPair);
                    }
            }
    }

    // Returns true if a tank with the given type already exists in the list model
    private boolean tankTypeExists(String type) 
    {
        if (type == null) 
            {
                return false;
            }
        String target = type.trim().toLowerCase();
        for (int i = 0; i < tanksModel.size(); i++) 
            {
                String item = tanksModel.get(i);
                // Each entry looks like: "Tank: <type> location: ... capacity: ..."
                if (item.startsWith("Tank:")) 
                    {
                        // Extract the type between "Tank: " and " location:"
                        int start = "Tank: ".length();
                        int locIndex = item.indexOf(" location:");
                        if (locIndex > start) 
                        {
                                String existingType = item.substring(start, locIndex).trim().toLowerCase();
                                if (existingType.equals(target)) 
                                    {
                                        return true;
                                }
                    }
                }
            }
        return false;
    }
}
