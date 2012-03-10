package org.vanbest.xmltv;

public class Channel {
	int id;
    String name;
    String shortName;
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
}
