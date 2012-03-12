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
		writer.writeAttribute("generator-info-url","http://www.vanbest.org/");
		writer.writeAttribute("source-info-url", "http://tvgids.nl/");
		writer.writeAttribute("source-info-name", "TvGids.nl");
		writer.writeAttribute("generator-info-name", "tv_grab_nl_java $VERSION");
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

	/* TODO: 
	 * 	  boolean is_highlight;
	 * 	  String highlight_afbeelding;
	 *    String highlight_content;
	 *    soort
	 *    artikel_id ???
	 */
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

				if (p.details != null) {
					if ( p.is_highlight) {
						//System.out.println("Highlight");
						//System.out.println("	" + p.highlight_afbeelding);
						//System.out.println("	" + p.highlight_content);
					} else {
						if (p.highlight_afbeelding!= null && !p.highlight_afbeelding.isEmpty()) {
							//System.out.println("highlight_afbeelding: " + p.highlight_afbeelding);
						}
						if (p.highlight_content!= null && !p.highlight_content.isEmpty()) {
							//System.out.println("highlight_content: " + p.highlight_content);
						}
					}
					if (!p.details.kijkwijzer.isEmpty() ||
							!p.details.presentatie.isEmpty() || 
							!p.details.presentatie.isEmpty() ||
							!p.details.acteursnamen_rolverdeling.isEmpty()
							) {
						writer.writeStartElement("credits");
						if (!p.details.kijkwijzer.isEmpty()) {
							writer.writeStartElement("rating");
							writer.writeAttribute("system", "kijkwijzer");
							writer.writeCharacters(p.details.kijkwijzer);
							writer.writeEndElement();
						}
						if (!p.details.presentatie.isEmpty()) {
							String[] parts = p.details.presentatie.split(",");
							for (String s: parts) {
								writer.writeStartElement("presenter");
								writer.writeCharacters(s.trim());
								writer.writeEndElement();
							}
						}
						if (!p.details.regisseur.isEmpty()) {
							String[] parts = p.details.regisseur.split(",");
							for (String s: parts) {
								writer.writeStartElement("director");
								writer.writeCharacters(s.trim());
								writer.writeEndElement();
							}
						}
						if (!p.details.acteursnamen_rolverdeling.isEmpty()) {
							String[] parts = p.details.acteursnamen_rolverdeling.split(",");
							for (String s: parts) {
								writer.writeStartElement("actor");
								writer.writeCharacters(s.trim());
								writer.writeEndElement();
							}
						}
						writer.writeEndElement();
					}
				}
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
