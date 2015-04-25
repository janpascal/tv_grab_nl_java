package org.vanbest.xmltv;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.log4j.Logger;

public class Channel {
	String id; // Internal id, used by EPG provider
        String xmltv; // xmltv id such as '1.tvgids.nl' or 'Nederland1.horizon.tv'
	List<String> names; // at least one name is obligatory
	List<Icon> icons;
	List<String> urls;
	protected boolean enabled = true;
	String source;
	static Logger logger = Logger.getLogger(Channel.class);

	protected Channel(String source, String id, String xmltv) {
		this.id = id;
		this.source = source;
                this.xmltv = xmltv;
		names = new ArrayList<String>();
		icons = new ArrayList<Icon>();
		urls = new ArrayList<String>();
	}

	public String defaultName() {
		return names.get(0);
	}

	static Channel getChannel(String source, String id, String xmltv, String name) {
                Channel c = new Channel(source, id, xmltv);
		c.names.add(name);
		return c;
	}

        // Use default xmltvid with id+"."+sourceName
	static Channel getChannel(String source, String id, String name) {
		String xmltv = id + "." + source;
        Channel c = new Channel(source, id, xmltv);
		c.names.add(name);
		return c;
	}

	public String getXmltvChannelId() {
		return xmltv; // id + "." + getSourceName();
	}

	public String getSourceName() {
		return source;
	}

	public void serialize(XMLStreamWriter writer, boolean writeLogos) throws XMLStreamException {
		writer.writeStartElement("channel");
		writer.writeAttribute("id", getXmltvChannelId());
		for (String name : names) {
			writer.writeStartElement("display-name");
			writer.writeAttribute("lang", "nl");
			writer.writeCharacters(name);
			writer.writeEndElement();
		}
                if (writeLogos) {
                    for (Icon i : icons) {
                            i.serialize(writer);
                    }
                }
		for (String url : urls) {
			writer.writeStartElement("url");
			writer.writeCharacters(url);
			writer.writeEndElement();
		}
		writer.writeEndElement();
		writer.writeCharacters(System.getProperty("line.separator"));
	}

	// Convenience method
	public void addIcon(String url) {
		icons.add(new Icon(url));
	}

	public void setEnabled(boolean b) {
		this.enabled = b;
	}

        public String extraConfig() {
                return "";
        }

        // must start with channel: <sourcename>
        public void writeConfig(PrintWriter out) {
		// FIXME: handle multiple channels names, icons and urls
                out.print("channel: " + getSourceName() + ": "
                                + Config.escape(xmltv) + ": "
                                + id + ": "
                                + (enabled ? "enabled" : "disabled") + ": "
                                + Config.escape(defaultName()) + ": "
                                + Config.escape(extraConfig()));
                if (!icons.isEmpty()) {
                        out.print(" : " + Config.escape(icons.get(0).url));
                }
                out.println();
        }

        public static Channel parseConfig(int fileformat, List<String> parts) {
                Channel c = null;
                switch (fileformat) {
                case 0:
                        c = Channel.getChannel("tvgids.nl", parts.get(1),
                                        parts.get(2));
                        if (parts.size() > 3) {
                                c.addIcon(parts.get(3));
                        }
                        break;
                case 1:
                        c = Channel.getChannel("tvgids.nl", parts.get(1),
                                        parts.get(3));
                        if (parts.size() > 4) {
                                c.addIcon(parts.get(4));
                        }
                        String value = parts.get(2);
                        if (value.equals("enabled")) {
                                c.setEnabled(true);
                        } else if (value.equals("disabled")) {
                                c.setEnabled(false);
                        } else {
                                logger.error("Error in config file, unknown channel status \""
                                                + parts.get(2)
                                                + "\", should be enabled or disabled");
                        }
                        break;
                case 2:
                        System.err.println("This config file format is no longer supported...");
                        break;
                case 3:
                case 4:
                case 5: {
                        String source = parts.get(1);
                        String id = parts.get(2);
                        String enabled = parts.get(3);
                        String name = parts.get(4);
                        if(fileformat<5) {
                                c = Channel.getChannel(source, id, name);
                        } else {
                                String extra = parts.get(5);
                                if(extra.isEmpty()) {
                                    c = Channel.getChannel(source, id, name);
                                } else {
                                    // Horizon channel
                                    String xmltv=id+"."+source;
                                    String horizonId=extra;
                                    c = Channel.getChannel(source, horizonId, xmltv, name);
                                }
                        }
                        int iconPart = (fileformat<5?5:6);
                        if (parts.size() > iconPart) {
                                c.addIcon(parts.get(iconPart));
                        }
                        if (enabled.equals("enabled")) {
                                c.setEnabled(true);
                        } else if (enabled.equals("disabled")) {
                                c.setEnabled(false);
                        } else {
                                logger.error("Error in config file, unknown channel status \""
                                                + enabled 
                                                + "\", should be enabled or disabled");
                        }
                        break;
                }
                case 6: {
                        String source = parts.get(1);
                        String xmltv = parts.get(2);
                        String id = parts.get(3);
                        String enabled = parts.get(4);
                        String name = parts.get(5);
                        String extra = parts.get(6);
                        c = Channel.getChannel(source, id, xmltv, name);
                        if (parts.size() > 7) {
                                c.addIcon(parts.get(7));
                        }
                        if (enabled.equals("enabled")) {
                                c.setEnabled(true);
                        } else if (enabled.equals("disabled")) {
                                c.setEnabled(false);
                        } else {
                                logger.error("Error in config file, unknown channel status \""
                                                + enabled
                                                + "\", should be enabled or disabled");
                        }
                }
                }
                return c;
        }

	public String toString() {
		return "Channel " + source + "::" + id + " (" + defaultName() + ")";
	}
}
