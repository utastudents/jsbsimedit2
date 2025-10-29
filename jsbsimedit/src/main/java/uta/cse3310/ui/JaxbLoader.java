package uta.cse3310.ui;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;

import java.io.File;

/** Loads any JAXB-generated root in the given package (e.g., "generated"). */
public final class JaxbLoader {
    private JaxbLoader(){}

    public static Object load(File xml, String jaxbPackage) throws Exception {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        JAXBContext ctx = JAXBContext.newInstance(jaxbPackage, cl);
        Unmarshaller u = ctx.createUnmarshaller();
        Object o = u.unmarshal(xml);
        if (o instanceof JAXBElement<?> je) return je.getValue(); // some gens wrap root
        return o;
    }

    public static Object load(String xmlPath, String jaxbPackage) throws Exception {
        return load(new File(xmlPath), jaxbPackage);
    }
}

