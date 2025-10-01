package frames.jaxb;

import frames.AbstractMainFrame;
import jakarta.xml.bind.*;
import model.Users;

import java.io.File;

public class MainFrameJaxb extends AbstractMainFrame {
    private final File xmlFile = new File("users-jaxb.xml");

    public MainFrameJaxb() {
        super("Swing + JAXB");
    }

    @Override
    protected void saveUsers(Users users) throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(Users.class);
        Marshaller marshaller = ctx.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        // If file exists, merge new users with existing
        Users existing = xmlFile.exists()
                ? (Users) ctx.createUnmarshaller().unmarshal(xmlFile)
                : new Users();
        existing.getUser().addAll(users.getUser());

        marshaller.marshal(existing, xmlFile);
    }

    @Override
    protected Users loadUsers() throws Exception {
        if (!xmlFile.exists()) {
            return new Users();
        }
        JAXBContext ctx = JAXBContext.newInstance(Users.class);
        return (Users) ctx.createUnmarshaller().unmarshal(xmlFile);
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new MainFrameJaxb().setVisible(true));
    }
}
