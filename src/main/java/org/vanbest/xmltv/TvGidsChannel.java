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

public class TvGidsChannel extends Channel {

	protected TvGidsChannel(String id) {
		super(CHANNEL_SOURCE_TVGIDS, id);
	}

	static Channel getChannel(String id, String name) {
		Channel c = new TvGidsChannel(id);
		c.names.add(name);
		return c;
	}

	static Channel getChannel(String id, String name, String iconUrl) {
		Channel c = new TvGidsChannel(id);
		c.names.add(name);
		c.icons.add(new Icon(iconUrl));
		return c;
	}

    public String getXmltvChannelId() {
    	return id+".tvgids.nl";
    }
    
}
