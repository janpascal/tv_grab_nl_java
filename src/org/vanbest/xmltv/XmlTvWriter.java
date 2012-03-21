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
	private Config config;
	
	public XmlTvWriter(OutputStream os, Config config) throws XMLStreamException, FactoryConfigurationError {
		this.config = config;
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
		writer.writeAttribute("generator-info-name", "tv_grab_nl_java release 0.4");
		writeln();
	}
	
	public void writeln() throws XMLStreamException {
		writer.writeCharacters(System.getProperty("line.separator"));
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
			writeln();		}
	}

	/* TODO: 
	 * 	  boolean is_highlight;
	 * 	  String highlight_afbeelding;
	 *    String highlight_content;
	 *    soort
	 *    artikel_id ???
	 *    		
	 */
	public void writePrograms(Collection<Programme> programs) throws XMLStreamException {
		DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss Z");
		for(Programme p: programs) {
			writer.writeStartElement("programme");
				writer.writeAttribute("start", df.format(p.datum_start));
				writer.writeAttribute("stop", df.format(p.datum_end));
				writer.writeAttribute("channel", ""+p.channel.getChannelId());
				writeln();				
				
				writer.writeStartElement("title");
					writer.writeAttribute("lang", "nl");
					writer.writeCharacters(p.titel);
				writer.writeEndElement();
				writeln();
				
				if(p.details.synop != null && ! p.details.synop.isEmpty()) {
					writer.writeStartElement("desc");
						writer.writeAttribute("lang", "nl");
						writer.writeCharacters(p.details.synop);
					writer.writeEndElement();
					writeln();
				}

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
					if (	(p.details.presentatie != null && !p.details.presentatie.isEmpty()) || 
							(p.details.regisseur != null && !p.details.regisseur.isEmpty()) ||
							(p.details.acteursnamen_rolverdeling != null && !p.details.acteursnamen_rolverdeling.isEmpty())
							) {
						writer.writeStartElement("credits");
						if (p.details.regisseur != null && !p.details.regisseur.isEmpty()) {
							String[] parts = p.details.regisseur.split(",");
							for (String s: parts) {
								writer.writeStartElement("director");
								writer.writeCharacters(s.trim());
								writer.writeEndElement();
							}
						}
						if (p.details.acteursnamen_rolverdeling != null && !p.details.acteursnamen_rolverdeling.isEmpty()) {
							String[] parts = p.details.acteursnamen_rolverdeling.split(",");
							for (String s: parts) {
								writer.writeStartElement("actor");
								writer.writeCharacters(s.trim());
								writer.writeEndElement();
							}
						}
						if (p.details.presentatie != null && !p.details.presentatie.isEmpty()) {
							String[] parts = p.details.presentatie.split(",");
							for (String s: parts) {
								writer.writeStartElement("presenter");
								writer.writeCharacters(s.trim());
								writer.writeEndElement();
							}
						}
						writer.writeEndElement();
						writeln();
					}
					writer.writeStartElement("category");
						writer.writeAttribute("lang", "en");
						writer.writeCharacters(p.genre); 
					writer.writeEndElement();
					writeln();
					
					if (p.details.blacknwhite || p.details.breedbeeld) {
						writer.writeStartElement("video");
					 	if (p.details.blacknwhite) {
							writer.writeStartElement("colour");
							writer.writeCharacters("no"); 
							writer.writeEndElement();
					 	}
					 	if (p.details.breedbeeld) {
							writer.writeStartElement("aspect");
							writer.writeCharacters("16x9"); 
							writer.writeEndElement();
					 	}
					 	if (p.details.quality != null) {
							writer.writeStartElement("quality");
							writer.writeCharacters(p.details.quality); 
							writer.writeEndElement();
					 	}
						writer.writeEndElement();
						writeln();
					}
					
					if (p.details.stereo) {
						writer.writeStartElement("audio");
							writer.writeStartElement("stereo");
							writer.writeCharacters("stereo"); 
							writer.writeEndElement();
						writer.writeEndElement();
						writeln();
					}
					
					if (p.details.herhaling) {
						writer.writeEmptyElement("previously-shown");
					}
					
					if (p.details.subtitle_teletekst) {
						writer.writeStartElement("subtitles");
						writer.writeAttribute("type", "teletext");
						writer.writeEndElement();
						writeln();
					}

					/* TODO: Icon attribuut gebruiken?
					 */
					if (p.details.kijkwijzer != null && !p.details.kijkwijzer.isEmpty()) {
						writer.writeStartElement("rating");
							writer.writeAttribute("system", "kijkwijzer");
							writer.writeStartElement("value");
							for (int i=0; i<p.details.kijkwijzer.length(); i++) {
								if (i!=0) writer.writeCharacters(", ");
								char c = p.details.kijkwijzer.charAt(i);
								switch(c) {
								case 'a':writer.writeCharacters("Angst"); break;
								case 'd':writer.writeCharacters("Discriminatie"); break;
								case 's':writer.writeCharacters("Seks"); break;
								case 'g':writer.writeCharacters("Geweld"); break;
								case 't':writer.writeCharacters("Grof taalgebruik"); break;
								case '1':writer.writeCharacters("Voor alle leeftijden"); break;
								case '2':writer.writeCharacters("Afgeraden voor kinderen jonger dan 6 jaar"); break;
								case '9':writer.writeCharacters("Afgeraden voor kinderen jonger dan 9 jaar"); break;
								case '3':writer.writeCharacters("Afgeraden voor kinderen jonger dan 12 jaar"); break;
								case '4':writer.writeCharacters("Afgeraden voor kinderen jonger dan 16 jaar"); break;
								default: if (!config.quiet) {
									System.out.println("Unknown kijkwijzer character: " + p.details.kijkwijzer);
									System.out.println("    for show " + p.titel + " at channel " + p.channel.name + " at " + p.datum_start);
								}
								}
							}
							//writer.writeStartElement("value");
							//	writer.writeCharacters(p.details.kijkwijzer);
							//writer.writeEndElement();
							writer.writeEndElement();
						writer.writeEndElement();
						writeln();
					}
				}
			writer.writeEndElement();
			writeln();
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
