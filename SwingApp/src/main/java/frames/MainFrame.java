package frames;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

public class MainFrame extends JFrame {
    // Input fields
    private JTextField nameField  = new JTextField(15);
    private JTextField emailField = new JTextField(15);
    private JTextField ageField   = new JTextField(5);

    private JTextArea displayArea = new JTextArea(10, 35);
    private JButton saveBtn = new JButton("Save User");
    private JButton loadBtn = new JButton("Load Users");

    public MainFrame() {
        super("Swing XML Multi-User Demo");
        setLayout(new BorderLayout(10,10));

        // ===== Input Panel =====
        JPanel inputPanel = new JPanel(new GridLayout(4,2,5,5));
        inputPanel.add(new JLabel("Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Email:"));
        inputPanel.add(emailField);
        inputPanel.add(new JLabel("Age:"));
        inputPanel.add(ageField);

        inputPanel.add(saveBtn);
        inputPanel.add(loadBtn);

        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        // Actions
        saveBtn.addActionListener(e -> saveToXml());
        loadBtn.addActionListener(e -> loadFromXml());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    /** Append a new user to users.xml */
    private void saveToXml() {
        try {
            File xmlFile = new File("users.xml");
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc;

            if (xmlFile.exists()) {
                // Load existing document so we can append
                doc = db.parse(xmlFile);
                doc.getDocumentElement().normalize();
            } else {
                // Create a new one
                doc = db.newDocument();
                Element root = doc.createElement("users");
                doc.appendChild(root);
            }

            Element root = doc.getDocumentElement();

            // Build <user>
            Element user = doc.createElement("user");

            Element name = doc.createElement("name");
            name.setTextContent(nameField.getText());
            user.appendChild(name);

            Element email = doc.createElement("email");
            email.setTextContent(emailField.getText());
            user.appendChild(email);

            Element age = doc.createElement("age");
            age.setTextContent(ageField.getText());
            user.appendChild(age);

            root.appendChild(user);

            // Write back to file
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(doc), new StreamResult(xmlFile));

            JOptionPane.showMessageDialog(this, "User saved to users.xml");
            nameField.setText("");
            emailField.setText("");
            ageField.setText("");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    /** Read all users and display them */
    private void loadFromXml() {
        try {
            File xmlFile = new File("users.xml");
            if (!xmlFile.exists()) {
                JOptionPane.showMessageDialog(this, "users.xml not found");
                return;
            }
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(xmlFile);
            doc.getDocumentElement().normalize();

            NodeList list = doc.getElementsByTagName("user");
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < list.getLength(); i++) {
                Element u = (Element) list.item(i);
                String name  = u.getElementsByTagName("name").item(0).getTextContent();
                String email = u.getElementsByTagName("email").item(0).getTextContent();
                String age   = u.getElementsByTagName("age").item(0).getTextContent();
                sb.append(String.format("Name: %s | Email: %s | Age: %s%n", name, email, age));
            }
            displayArea.setText(sb.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
