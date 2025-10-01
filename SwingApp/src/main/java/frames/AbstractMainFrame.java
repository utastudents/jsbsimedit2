package frames;

import model.User;
import model.Users;

import javax.swing.*;
import java.awt.*;

public abstract class AbstractMainFrame extends JFrame {
    protected JTextField nameField  = new JTextField(15);
    protected JTextField emailField = new JTextField(15);
    protected JTextField ageField   = new JTextField(5);
    protected JTextArea  displayArea= new JTextArea(10, 35);
    protected JButton saveBtn = new JButton("Save User");
    protected JButton loadBtn = new JButton("Load Users");

    protected abstract void saveUsers(Users users) throws Exception;
    protected abstract Users loadUsers() throws Exception;

    public AbstractMainFrame(String title) {
        super(title);
        setLayout(new BorderLayout(10,10));

        JPanel inputPanel = new JPanel(new GridLayout(4,2,5,5));
        inputPanel.add(new JLabel("Name:"));  inputPanel.add(nameField);
        inputPanel.add(new JLabel("Email:")); inputPanel.add(emailField);
        inputPanel.add(new JLabel("Age:"));   inputPanel.add(ageField);
        inputPanel.add(saveBtn); inputPanel.add(loadBtn);
        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(displayArea), BorderLayout.CENTER);

        saveBtn.addActionListener(e -> onSave());
        loadBtn.addActionListener(e -> onLoad());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void onSave() {
        try {
            Users u = new Users();
            u.getUser().add(new User(
                    nameField.getText(),
                    emailField.getText(),
                    ageField.getText()));
            saveUsers(u);
            JOptionPane.showMessageDialog(this, "User saved!");
            nameField.setText(""); emailField.setText(""); ageField.setText("");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void onLoad() {
        try {
            Users users = loadUsers();
            StringBuilder sb = new StringBuilder();
            for (var u : users.getUser()) {
                sb.append(String.format("Name: %s | Email: %s | Age: %s%n",
                        u.getName(), u.getEmail(), u.getAge()));
            }
            displayArea.setText(sb.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }
}

