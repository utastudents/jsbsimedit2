package uta.cse3310.tab.concreteTabs;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import uta.cse3310.tab.simpleTab;
import uta.cse3310.dataStore;
import uta.cse3310.tabFrame;

import generated.Engine;
import generated.Mass;
import generated.Tank;


public class PropulsionTab extends simpleTab 
{
    private JTabbedPane tabbedPane;
    private JList<String> availableEnginesList;
    private JList<String> availableThrustersList;
    private DefaultListModel<String> enginesModel;
    private DefaultListModel<String> thrustersModel;
    private DefaultListModel<String> subscribedModel;
    private DefaultListModel<String> tanksModel;

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
        JList<String> tanksList = new JList<>(tanksModel);
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

            if (file == null || file.trim().isEmpty()) {
                ok = false;
                err.append("[missing file] ");
            }

            if (!ok) {
                // Record error and skip adding this engine to the lists
                engineErrors.add(err.toString());
                continue;
            }
            if (name == null || name.trim().isEmpty()) {
                name = file;
            }
            
        String display = name + "  (" + file + ")";
        enginesModel.addElement(display);
        thrustersModel.addElement(display);
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

    if (!engineErrors.isEmpty()) {
        StringBuilder msg = new StringBuilder();
        msg.append("Some engine definitions in the XML are invalid:\n\n");
        for (String s : engineErrors) {
            msg.append("• ").append(s).append("\n");
        }
        JOptionPane.showMessageDialog(
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

    JList<String> tanksList = new JList<>(tanksModel);
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
    btnNewPair.addActionListener(e -> 
    {
        String eName = enginesList.getSelectedValue() != null ? enginesList.getSelectedValue() : "";
        String tName = thrustersList.getSelectedValue() != null ? thrustersList.getSelectedValue() : "";

        if (!eName.isEmpty() && !tName.isEmpty())
        {
            String pair = eName + " + " + tName;
            subscribedModel.addElement(pair);
        }
        else 
        {
            JOptionPane.showMessageDialog(panel,"Select both an engine and a thruster to create a pair.");
        }
    });

    btnDelPair.addActionListener(e -> 
    {
        String selected = subList.getSelectedValue();
        if (selected != null)
        {
            subscribedModel.removeElement(selected);
        }
    });

    btnDetailPair.addActionListener(e -> 
    {
        String selected = subList.getSelectedValue();
        if (selected != null)
        {
            showPairEditDialog(selected);
        }
    });

    // Tanks
    btnNewTank.addActionListener(e -> 
    {
        JTextField typeField = new JTextField();
        JTextField xField = new JTextField("0");
        JTextField yField = new JTextField("0");
        JTextField zField = new JTextField("0");
        JTextField locUnitField = new JTextField("IN");
        JTextField capField = new JTextField("0");
        JTextField capUnitField = new JTextField("LBS");

        JPanel inputPanel = new JPanel(new GridLayout(0,2));
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

         // Basic empty type check
        if (type.isEmpty()) {
            JOptionPane.showMessageDialog(
                    panel,
                    "Tank type cannot be empty.",
                    "Invalid Tank Name",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // duplicate tank name check
        if (tankTypeExists(type)) {
            JOptionPane.showMessageDialog(
                    panel,
                    "A tank named \"" + type + "\" already exists.\nPlease choose a different name.",
                    "Duplicate Tank Name",
                    JOptionPane.WARNING_MESSAGE
            );
            return; // do not add duplicate
        }
            //validate location unit
            java.util.Set<String> allowedLocUnits = java.util.Set.of("IN", "FT", "M");
            if (!allowedLocUnits.contains(locUnit)) {
                JOptionPane.showMessageDialog(
                        panel,
                        "Unsupported location unit: \"" + locUnit + "\".\nAllowed units: IN, FT, M.",
                        "Invalid Location Unit",
                        JOptionPane.ERROR_MESSAGE
                );
                return; // do not create the tank
            }
            
            // validate capacity unit
            java.util.Set<String> allowedCapUnits = java.util.Set.of("LBS", "KG", "GAL");
            if (!allowedCapUnits.contains(capUnit)) {
                JOptionPane.showMessageDialog(
                        panel,
                        "Unsupported capacity unit: \"" + capUnit + "\".\nAllowed units: LBS, KG, GAL.",
                        "Invalid Capacity Unit",
                        JOptionPane.ERROR_MESSAGE
                );
                return;
            }
        
            String locStr = xField.getText().trim() + ", " +
                            yField.getText().trim() + ", " +
                            zField.getText().trim() + " " + locUnit;
            String capStr = capField.getText().trim() + " " + capUnit;

            String newTankStr = "Tank: " + type + " location: " + locStr + " capacity: " + capStr;
            tanksModel.addElement(newTankStr);
        }

    });

    btnDelTank.addActionListener(e -> 
    {
        String selected = tanksList.getSelectedValue();
        if (selected != null)
        {
            tanksModel.removeElement(selected);
        }
    });

    btnDetailTank.addActionListener(e -> 
    {
        int selectedIndex = tanksList.getSelectedIndex();
        if (selectedIndex == -1)
        {
            JOptionPane.showMessageDialog(panel, "Please select a tank to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Find the Tank object in the XML
        Tank tank = null;
        int index = 0;
        for (Object obj : DS.cfg.getPropulsion().getDocumentationOrPropertyOrFunction()) 
        {
            if (obj instanceof Tank) 
            {
                if (index == selectedIndex)
                {
                    tank = (Tank) obj;
                    break;
                }
                index++;
            }
        }

        if (tank == null)
        {
            JOptionPane.showMessageDialog(panel, "Tank not found.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create editable dialog for tank properties
        showTankEditDialog(tank, selectedIndex);
    });

    panel.revalidate();
    panel.repaint();
}


private void showTankEditDialog(Tank tank, int tankIndex)
{
    // Create dialog
    JDialog dialog = new JDialog((JFrame) SwingUtilities.getWindowAncestor(panel), "Edit Tank", true);
    dialog.setSize(500, 400);
    dialog.setLocationRelativeTo(panel);

    // Form panel
    JPanel formPanel = new JPanel(new GridLayout(0, 2, 10, 10));
    formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    // Tank Type
    JTextField typeField = new JTextField(tank.getType() != null ? tank.getType() : "");
    formPanel.add(new JLabel("Tank Type:"));
    formPanel.add(typeField);

    // Location fields
    double locX = 0, locY = 0, locZ = 0;
    String locUnit = "IN";
    if (tank.getLocation() != null)
    {
        locX = tank.getLocation().getX();
        locY = tank.getLocation().getY();
        locZ = tank.getLocation().getZ();
        locUnit = tank.getLocation().getUnit() != null ? tank.getLocation().getUnit().value() : "IN";
    }

    JTextField xField = new JTextField(String.valueOf(locX));
    JTextField yField = new JTextField(String.valueOf(locY));
    JTextField zField = new JTextField(String.valueOf(locZ));
    
    String[] locationUnits = {"IN", "FT", "M"};
    JComboBox<String> locUnitCombo = new JComboBox<>(locationUnits);
    locUnitCombo.setSelectedItem(locUnit);

    formPanel.add(new JLabel("Location X:"));
    formPanel.add(xField);
    formPanel.add(new JLabel("Location Y:"));
    formPanel.add(yField);
    formPanel.add(new JLabel("Location Z:"));
    formPanel.add(zField);
    formPanel.add(new JLabel("Location Unit:"));
    formPanel.add(locUnitCombo);

    // Capacity fields
    double capValue = 0;
    String capUnit = "LBS";
    if (tank.getCapacity() != null)
    {
        capValue = tank.getCapacity().getValue();
        capUnit = tank.getCapacity().getUnit() != null ? tank.getCapacity().getUnit().value() : "LBS";
    }

    JTextField capField = new JTextField(String.valueOf(capValue));
    String[] capacityUnits = {"LBS", "KG", "GAL"};
    JComboBox<String> capUnitCombo = new JComboBox<>(capacityUnits);
    capUnitCombo.setSelectedItem(capUnit);

    formPanel.add(new JLabel("Capacity:"));
    formPanel.add(capField);
    formPanel.add(new JLabel("Capacity Unit:"));
    formPanel.add(capUnitCombo);

    // Contents field (Mass object)
    double contentsValue = 0;
    String contentsUnit = "LBS";
    if (tank.getContents() != null)
    {
        contentsValue = tank.getContents().getValue();
        contentsUnit = tank.getContents().getUnit() != null ? tank.getContents().getUnit().value() : "LBS";
    }
    JTextField contentsField = new JTextField(String.valueOf(contentsValue));
    JComboBox<String> contentsUnitCombo = new JComboBox<>(capacityUnits);
    contentsUnitCombo.setSelectedItem(contentsUnit);
    
    formPanel.add(new JLabel("Contents:"));
    formPanel.add(contentsField);
    formPanel.add(new JLabel("Contents Unit:"));
    formPanel.add(contentsUnitCombo);

    // Priority field (optional)
    if (tank.getPriority() != null)
    {
        JTextField priorityField = new JTextField(String.valueOf(tank.getPriority()));
        formPanel.add(new JLabel("Priority:"));
        formPanel.add(priorityField);
    }

    // Button panel
    JPanel buttonPanel = new JPanel();
    JButton okButton = new JButton("OK");
    JButton cancelButton = new JButton("Cancel");

    okButton.addActionListener(ev -> 
    {
        try
        {
            // Update tank properties
            tank.setType(typeField.getText());

            // Update location
            if (tank.getLocation() == null)
            {
                tank.setLocation(new generated.Location());
            }
            tank.getLocation().setX(Double.parseDouble(xField.getText()));
            tank.getLocation().setY(Double.parseDouble(yField.getText()));
            tank.getLocation().setZ(Double.parseDouble(zField.getText()));
            tank.getLocation().setUnit(generated.LengthUnit.fromValue((String) locUnitCombo.getSelectedItem()));

            // Update capacity
            if (tank.getCapacity() == null)
            {
                tank.setCapacity(new Mass());
            }
            tank.getCapacity().setValue(Double.parseDouble(capField.getText()));
            tank.getCapacity().setUnit(generated.MassUnit.fromValue((String) capUnitCombo.getSelectedItem()));

            // Update contents
            if (tank.getContents() == null)
            {
                tank.setContents(new Mass());
            }
            tank.getContents().setValue(Double.parseDouble(contentsField.getText()));
            tank.getContents().setUnit(generated.MassUnit.fromValue((String) contentsUnitCombo.getSelectedItem()));

            // Refresh the display
            
            String locStr = "(" + tank.getLocation().getX() + ", " +
                      tank.getLocation().getY() + ", " +
                      tank.getLocation().getZ() + ") " +
                      tank.getLocation().getUnit();

            String capStr = tank.getCapacity().getValue() + " " + tank.getCapacity().getUnit();

            String updatedText = "Tank (" + tank.getType() + ") location=" +
                                locStr + " capacity=" + capStr;

            tanksModel.set(tankIndex, updatedText);

            dialog.dispose();
        }
        catch (NumberFormatException ex)
        {
            JOptionPane.showMessageDialog(dialog, "Invalid number format. Please check your inputs.", "Input Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    cancelButton.addActionListener(ev -> dialog.dispose());

    buttonPanel.add(okButton);
    buttonPanel.add(cancelButton);

    // Layout
    dialog.setLayout(new BorderLayout());
    dialog.add(formPanel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    dialog.setVisible(true);
}

private void showPairEditDialog(String pair) {
    // Example pair format: {F100-PW-229} + {F100-PW-229}
    String[] parts = pair.split("\\+");  // Split at "+"
    String engine = parts.length > 0 ? parts[0].trim().replace("{","").replace("}","") : "";
    String thruster = parts.length > 1 ? parts[1].trim().replace("{","").replace("}","") : "";

    JTextField engineField = new JTextField(engine);
    JTextField thrusterField = new JTextField(thruster);

    JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
    panel.add(new JLabel("Engine:"));
    panel.add(engineField);
    panel.add(new JLabel("Thruster:"));
    panel.add(thrusterField);

    int result = JOptionPane.showConfirmDialog(panel, panel, "Edit Pair", JOptionPane.OK_CANCEL_OPTION);

    if (result == JOptionPane.OK_OPTION) {
        String newPair = "{" + engineField.getText() + "} + {" + thrusterField.getText() + "}";
        int idx = subscribedModel.indexOf(pair);
        if (idx >= 0) {
            subscribedModel.set(idx, newPair);
        }
    }
}
// Returns true if a tank with the given type already exists in the list model
private boolean tankTypeExists(String type) {
    if (type == null) {
        return false;
    }
    String target = type.trim().toLowerCase();
    for (int i = 0; i < tanksModel.size(); i++) {
        String item = tanksModel.get(i);
        // Each entry looks like: "Tank: <type> location: ... capacity: ..."
        if (item.startsWith("Tank:")) {
            // Extract the type between "Tank: " and " location:"
            int start = "Tank: ".length();
            int locIndex = item.indexOf(" location:");
            if (locIndex > start) {
                String existingType = item.substring(start, locIndex).trim().toLowerCase();
                if (existingType.equals(target)) {
                    return true;
                }
            }
        }
    }
    return false;
}
}








