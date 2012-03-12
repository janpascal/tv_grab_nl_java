package org.vanbest.xmltv;

import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
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
			writer.writeAttribute("id", c.getChannelId());
				writer.writeStartElement("display-name");
				writer.writeAttribute("lang", "nl");
				writer.writeCharacters(c.name);
				writer.writeEndElement();

				if (c.iconUrl != null) {
					writer.writeStartElement("icon");
					writer.writeAttribute("src", c.iconUrl);
					writer.writeEndElement();
				}
				
			writer.writeEndElement();
			writer.writeCharacters("\n");
		}
	}
	
	public void writePrograms(Collection<Programme> programs) throws XMLStreamException {
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss Z");
		for(Programme p: programs) {
			writer.writeStartElement("programme");
				writer.writeAttribute("start", df.format(p.datum_start));
				writer.writeAttribute("stop", df.format(p.datum_end));
				writer.writeAttribute("channel", ""+p.channel.getChannelId());
				writer.writeCharacters("\n");
				
				writer.writeStartElement("title");
					writer.writeAttribute("lang", "nl");
					writer.writeCharacters(p.titel);
				writer.writeEndElement();
				writer.writeCharacters("\n");

				if(p.details.synop != null && ! p.details.synop.isEmpty()) {
					writer.writeStartElement("desc");
						writer.writeAttribute("lang", "nl");
						writer.writeCharacters(p.details.synop);
					writer.writeEndElement();
					writer.writeCharacters("\n");
				}

				writer.writeStartElement("category");
					writer.writeAttribute("lang", "en");
					writer.writeCharacters(p.genre); // soort? FIXME translation to mythtv categories
				writer.writeEndElement();
				writer.writeCharacters("\n");

			writer.writeEndElement();
			writer.writeCharacters("\n");
		}
	}
	
	public void close() throws XMLStreamException {
		writer.writeEndElement();
		writer.writeEndDocument();
		writer.flush();
	}

	public void flush() throws XMLStreamException {
		writer.flush();
	}
}
