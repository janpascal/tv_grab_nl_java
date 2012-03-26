package org.vanbest.xmltv;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.vanbest.xmltv.EPGSource.Stats;

public abstract class AbstractEPGSource implements EPGSource {

	protected Config config;
	protected ProgrammeCache cache;
	protected Stats stats = new Stats();

	public AbstractEPGSource(Config config) {
		this.config = config;
		cache = new ProgrammeCache(config.cacheFile);
	}

	public Set<Programme> getProgrammes(Channel channel, int day, boolean fetchDetails)
			throws Exception {
				ArrayList<Channel> list = new ArrayList<Channel>(2);
				list.add(channel);
				return getProgrammes(list, day, fetchDetails);
			}

	@Override
	public Stats getStats() {
		return stats;
	}

	@Override
	public void close() throws FileNotFoundException, IOException {
		cache.close();
	}

}