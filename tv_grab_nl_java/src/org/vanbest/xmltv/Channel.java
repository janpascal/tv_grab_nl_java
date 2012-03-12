package org.vanbest.xmltv;

public class Channel {
	int id;
    String name;
    String shortName;
	String iconUrl;
    public Channel(int id, String name, String shortName) {
    	this.id = id;
    	this.name = name;
    	this.shortName = shortName;
	}
    public String toString() {
    	return "id: " + id + "; name: " + name + "; shortName: " + shortName;
    }
    
    public String getChannelId() {
    	return id+".tvgids.nl";
    }
    
    public void fixup() {
		 this.name = org.apache.commons.lang.StringEscapeUtils.unescapeHtml(name);
		 this.shortName = org.apache.commons.lang.StringEscapeUtils.unescapeHtml(shortName);
    }
	public void setIconUrl(String url) {
		this.iconUrl = url;
	}
}
