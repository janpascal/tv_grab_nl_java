package org.vanbest.xmltv;

import java.io.OutputStream;
import java.util.List;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLStreamException;

/*
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE tv SYSTEM "xmltv.dtd">
<tv generator-info-url="http://www.caliban.org/ruby/xmltv_upc.shtml" source-info-url="http://tvgids.upc.nl/TV/" source-info-name="UPC TV Guide" generator-info-name="tv_grab_nl_upc $Id: tv_grab_nl_upc,v 1.228 2011/04/05 07:33:12 ianmacd Exp $">

<channel id="16.chello.nl">
<display-name lang="nl">
Comedy C.\Kindernet
</display-name>
</channel>

*/

public class XmlTvWriter {

	private XMLStreamWriter writer;
	private XMLEventFactory eventFactory;
	
	public XmlTvWriter(OutputStream os) throws XMLStreamException, FactoryConfigurationError {
		this.writer = XMLOutputFactory.newInstance().createXMLStreamWriter(os);
		this.eventFactory = XMLEventFactory.newInstance();
		
		writer.writeStartDocument();
		writer.writeCharacters("\n");
		writer.writeDTD("<!DOCTYPE tv SYSTEM \"xmltv.dtd\">");
		writer.writeCharacters("\n");
		writer.writeStartElement("tv");
		writer.writeAttribute("generator-info-url","http://www.caliban.org/ruby/xmltv_upc.shtml");
		writer.writeAttribute("source-info-url", "http://tvgids.nl/");
		writer.writeAttribute("source-info-name", "TvGids.nl");
		writer.writeAttribute("generator-info-name", "tv_grab_nl_java $Id: tv_grab_nl_java,v 1.228 2011/04/05 07:33:12 ianmacd Exp $");
		writer.writeCharacters("\n");
	}
	
	public void writeChannels(List<Channel> channels) throws XMLStreamException {
		for(Channel c: channels) {
			writer.writeStartElement("channel");
			writer.writeAttribute("id", ""+c.id);
			writer.writeStartElement("display-name");
			writer.writeAttribute("lang", "nl");
			writer.writeCharacters(c.name);
			writer.writeEndElement();
			writer.writeEndElement();
			writer.writeCharacters("\n");
		}
	}
	
	public void close() throws XMLStreamException {
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();
	}
}
