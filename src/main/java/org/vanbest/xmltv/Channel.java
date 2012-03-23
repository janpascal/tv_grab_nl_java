package org.vanbest.xmltv;

/*
  Copyright (c) 2012 Jan-Pascal van Best <janpascal@vanbest.org>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  The full license text can be found in the LICENSE file.
*/

public class Channel {
	int id;
    String name;
    String shortName;
	String iconUrl;
	boolean selected;
	
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
