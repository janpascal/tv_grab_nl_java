package org.vanbest.xmltv;

import java.util.HashMap;
import java.util.Map;

public class EPGSourceFactory {
	
	public final static int CHANNEL_SOURCE_TVGIDS=1;
	public final static int CHANNEL_SOURCE_RTL=2;
	
	private final static int[] CHANNEL_IDS={CHANNEL_SOURCE_TVGIDS, CHANNEL_SOURCE_RTL};
	private final static String[] CHANNEL_SOURCE_NAMES={"tvgids.nl", "rtl.nl"};
	private static Map<String,Integer> channelSourceNameMap = new HashMap<String,Integer>();
	
	private EPGSourceFactory() {
	}

	public static EPGSourceFactory newInstance() {
		return new EPGSourceFactory();
	}
	
	public EPGSource createEPGSource(int source, Config config) {
		switch(source) {
		case EPGSourceFactory.CHANNEL_SOURCE_RTL:
			return new RTL(config, false);
		case EPGSourceFactory.CHANNEL_SOURCE_TVGIDS:
			return new TvGids(config);
		default:
			return null;
		}
	}
	
	public EPGSource createEPGSource(String source, Config config) {
		int sourceId = EPGSourceFactory.getChannelSourceId(source);
		return createEPGSource(sourceId, config);
	}
	
	public static String getChannelSourceName(int id) {
		return CHANNEL_SOURCE_NAMES[id-1];
	}
	
	public static int getChannelSourceId(String name) {
		if (channelSourceNameMap.isEmpty()) {
			int i=1;
			for (String s: EPGSourceFactory.CHANNEL_SOURCE_NAMES) {
				EPGSourceFactory.channelSourceNameMap.put(s,  i);
				i++;
			}
		}
		return EPGSourceFactory.channelSourceNameMap.get(name);
	}
	
	public int[] getAll() {
		return CHANNEL_IDS;
	}
}
