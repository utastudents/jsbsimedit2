package frames.jaxp;

import frames.AbstractMainFrame;
import model.User;
import model.Users;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

public class MainFrameJaxp extends AbstractMainFrame {
    private final File xmlFile = new File("users-jaxp.xml");

    public MainFrameJaxp() {
        super("Swing + JAXP");
    }

    @Override
    protected void saveUsers(Users users) throws Exception {
        DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = xmlFile.exists()
                ? db.parse(xmlFile)
                : db.newDocument();

        Element root;
        if (doc.getDocumentElement() == null) {
            root = doc.createElement("users");
            doc.appendChild(root);
        } else {
            root = doc.getDocumentElement();
        }

        for (User u : users.getUser()) {
            Element userEl = doc.createElement("user");
            Element n = doc.createElement("name"); n.setTextContent(u.getName());
            Element e = doc.createElement("email"); e.setTextContent(u.getEmail());
            Element a = doc.createElement("age"); a.setTextContent(u.getAge());
            userEl.appendChild(n); userEl.appendChild(e); userEl.appendChild(a);
            root.appendChild(userEl);
        }
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.INDENT, "yes");
        t.transform(new DOMSource(doc), new StreamResult(xmlFile));
    }

    @Override
    protected Users loadUsers() throws Exception {
        Users out = new Users();
        if (!xmlFile.exists()) return out;

        Document doc = DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(xmlFile);
        doc.getDocumentElement().normalize();

        NodeList list = doc.getElementsByTagName("user");
        for (int i = 0; i < list.getLength(); i++) {
            Element u = (Element) list.item(i);
            out.getUser().add(new User(
                    u.getElementsByTagName("name").item(0).getTextContent(),
                    u.getElementsByTagName("email").item(0).getTextContent(),
                    u.getElementsByTagName("age").item(0).getTextContent()
            ));
        }
        return out;
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new MainFrameJaxp().setVisible(true));
    }
}
