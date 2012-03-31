package org.vanbest.xmltv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class Channel {
	String id;
	List<String> names; // at least one name is obligatory
	List<Icon> icons;
	List<String> urls;
	protected boolean enabled = true;
	int source;
	
	public final static int CHANNEL_SOURCE_TVGIDS=1;
	public final static int CHANNEL_SOURCE_RTL=2;
	private final static String[] CHANNEL_SOURCE_NAMES={"tvgids.nl", "rtl.nl"};
	private static Map<String,Integer> channelSourceNameMap = new HashMap<String,Integer>();

	
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
		Channel c = new Channel(source, id);
		c.names.add(name);
		return c;
	}

	static Channel getChannel(int source, String id, String name, String iconUrl) {
		Channel c = new Channel(source, id);
		c.names.add(name);
		c.icons.add(new Icon(iconUrl));
		return c;
	}
	
	public String getXmltvChannelId() {
		switch (source) {
		case CHANNEL_SOURCE_TVGIDS:
		case CHANNEL_SOURCE_RTL:
		default:
			return id+"."+getSourceName();
		}
	}
	
	public static String getChannelSourceName(int id) {
		return CHANNEL_SOURCE_NAMES[id-1];
	}

	public String getSourceName() {
		return getChannelSourceName(source);
	}
	
	public static int getChannelSourceId(String name) {
		if (channelSourceNameMap.isEmpty()) {
			int i=1;
			for (String s: CHANNEL_SOURCE_NAMES) {
				channelSourceNameMap.put(s,  i);
				i++;
			}
		}
		return channelSourceNameMap.get(name);
		
	}
	 
	public void serialize(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("channel");
		writer.writeAttribute("id", getXmltvChannelId());
		for(String name: names) {
			writer.writeStartElement("display-name");
			writer.writeAttribute("lang", "nl");
			writer.writeCharacters(name);
			writer.writeEndElement();
		}
		for(Icon i: icons) {
			i.serialize(writer);
		}
		for(String url: urls) {
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
	
    public String toString() {
    	return "Channel " + source + "::" + id + " (" + defaultName() + ")";
    }
}