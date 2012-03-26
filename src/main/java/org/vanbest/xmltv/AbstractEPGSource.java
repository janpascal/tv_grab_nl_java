package org.vanbest.xmltv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
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

	public Set<TvGidsProgramme> getProgrammes(Channel channel, int day, boolean fetchDetails)
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

	protected String fetchURL(URL url) throws Exception {
		Thread.sleep(config.niceMilliseconds);
		StringBuffer buf = new StringBuffer();
		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader( url.openStream()));
			String s;
			while ((s = reader.readLine()) != null) buf.append(s);
		} catch (IOException e) {
			stats.fetchErrors++;
			throw new Exception("Error getting program data from url " + url, e);
		}
		return buf.toString();  
	}


}