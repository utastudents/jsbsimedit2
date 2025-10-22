package utastudents.jsbsimedit2.io.xml;

import utastudents.jsbsimedit2.core.domain.CanvasModel;

// Placeholder interface for the XML integration.
//
// Keep UI layout positions in a separate buddy/sidecar file for now (LayoutIO).
// - We(DnD group) can add functionality to load positions in a deterministic way from the 
//   XML data as a fallback if there is no sidecar for the loaded XML file

public interface JsbsimXmlRepository {
CanvasModel loadLayout(File file) throws Exception;

    void saveLayout(CanvasModel model, File file) throws Exception;

}
