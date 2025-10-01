package frames.jackson;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import frames.AbstractMainFrame;
import model.Users;

import java.io.File;

public class MainFrameJackson extends AbstractMainFrame {
    private final File xmlFile = new File("users-jackson.xml");
    private final XmlMapper mapper = new XmlMapper();

    public MainFrameJackson() {
        super("Swing + Jackson XML");
    }

    @Override
    protected void saveUsers(Users users) throws Exception {
        Users current = xmlFile.exists() ? mapper.readValue(xmlFile, Users.class)
                : new Users();
        current.getUser().addAll(users.getUser());
        mapper.writerWithDefaultPrettyPrinter().writeValue(xmlFile, current);
    }

    @Override
    protected Users loadUsers() throws Exception {
        return xmlFile.exists() ? mapper.readValue(xmlFile, Users.class) : new Users();
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new MainFrameJackson().setVisible(true));
    }
}

