package org.vanbest.xmltv;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface EPGSource {
	public class Stats {
		int fetchErrors = 0;
		int cacheHits = 0;
		int cacheMisses = 0;
	}

	public int getId();
	public void setId(int id);
    public String getName();

	public List<Channel> getChannels();
	// Convenience method
	public List<Programme> getProgrammes(Channel channel, int day) throws Exception;
	public List<Programme> getProgrammes(List<Channel> channels, int day) throws Exception;
	
	public Stats getStats();
	
	public void clearCache();
	public void close() throws FileNotFoundException, IOException;
}
