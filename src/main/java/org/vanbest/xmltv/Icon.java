package org.vanbest.xmltv;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class Icon {
	final static int UNKNOWN_DIMENSION=-1;
	String url;
	int width = UNKNOWN_DIMENSION;
	int height = UNKNOWN_DIMENSION;

	public Icon(String url) {
		this.url = url;
	}

	public void serialize(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("icon");
		writer.writeAttribute("src", url);
		if(width != UNKNOWN_DIMENSION) {
			writer.writeAttribute("width", Integer.toString(width));
		}
		if(height!= UNKNOWN_DIMENSION) {
			writer.writeAttribute("height", Integer.toString(height));
		}
		writer.writeEndElement();
	}
}
