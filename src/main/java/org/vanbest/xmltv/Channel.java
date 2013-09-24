package org.vanbest.xmltv;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.log4j.Logger;

public class Channel {
	String id;
	List<String> names; // at least one name is obligatory
	List<Icon> icons;
	List<String> urls;
	protected boolean enabled = true;
	int source;
	static Logger logger = Logger.getLogger(Channel.class);

	protected Channel(int source, String id) {
		this.id = id;
		this.source = source;
		names = new ArrayList<String>();
		icons = new ArrayList<Icon>();
		urls = new ArrayList<String>();
	}

	public String defaultName() {
		return names.get(0);
	}

	static Channel getChannel(int source, String id, String name) {
                Channel c;
                if(EPGSourceFactory.getChannelSourceName(source).equals(Horizon.NAME)) {
                        long horizonId=Long.parseLong(id);
		        c = new Horizon.HorizonChannel(source, id, horizonId);
                } else {
		        c = new Channel(source, id);
                }
		c.names.add(name);
		return c;
	}

	static Channel getChannel(int source, String id, String name, String extraConfig) {
                Channel c;
                if(EPGSourceFactory.getChannelSourceName(source).equals(Horizon.NAME)) {
                        long horizonId=Long.parseLong(extraConfig);
		        c = new Horizon.HorizonChannel(source, id, horizonId);
                } else {
		        c = new Channel(source, id);
                }
		c.names.add(name);
		return c;
	}

	public String getXmltvChannelId() {
		return id + "." + getSourceName();
	}

	public String getSourceName() {
		return EPGSourceFactory.newInstance().getChannelSourceName(source);
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
                        c = Channel.getChannel(EPGSourceFactory.newInstance()
                                        .getChannelSourceId("tvgids.nl"), parts.get(1),
                                        parts.get(2));
                        if (parts.size() > 3) {
                                c.addIcon(parts.get(3));
                        }
                        break;
                case 1:
                        c = Channel.getChannel(EPGSourceFactory.newInstance()
                                        .getChannelSourceId("tvgids.nl"), parts.get(1),
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
                case 3:
                case 4:
                case 5:
                        int source;
                        if (fileformat == 2) {
                                source = Integer.parseInt(parts.get(1));
                        } else {
                                source = EPGSourceFactory.newInstance()
                                                .getChannelSourceId(parts.get(1));
                        }
                        if(fileformat<5) {
                                c = Channel.getChannel(source, parts.get(2),
                                        parts.get(4));
                        } else {
                                c = Channel.getChannel(source, parts.get(2),
                                        parts.get(4), parts.get(5));
                        }
                        int iconPart = (fileformat<5?5:6);
                        if (parts.size() > iconPart) {
                                c.addIcon(parts.get(iconPart));
                        }
                        value = parts.get(3);
                        if (value.equals("enabled")) {
                                c.setEnabled(true);
                        } else if (value.equals("disabled")) {
                                c.setEnabled(false);
                        } else {
                                logger.error("Error in config file, unknown channel status \""
                                                + parts.get(3)
                                                + "\", should be enabled or disabled");
                        }
                }
                return c;
        }

	public String toString() {
		return "Channel " + source + "::" + id + " (" + defaultName() + ")";
	}
}
