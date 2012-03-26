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

	public abstract void close() throws FileNotFoundException, IOException;

	public abstract List<Channel> getChannels();

	// Convenience method
	public abstract Set<TvGidsProgramme> getProgrammes(Channel channel, int day,
			boolean fetchDetails) throws Exception;

	public abstract Set<TvGidsProgramme> getProgrammes(List<Channel> channels,
			int day, boolean fetchDetails) throws Exception;
	
	public abstract Stats getStats();

}