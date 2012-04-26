package org.vanbest.xmltv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vanbest.xmltv.EPGSource.Stats;

public abstract class AbstractEPGSource implements EPGSource {

	private int sourceId;
	protected Config config;
	protected ProgrammeCache cache;
	protected Stats stats = new Stats();
	static Logger logger = Logger.getLogger(AbstractEPGSource.class);
	
	public static final int MAX_FETCH_TRIES=5;

	public AbstractEPGSource(int sourceId, Config config) {
		this.config = config;
		this.sourceId = sourceId;
		cache = new ProgrammeCache(config);
	}

	public int getId() {
         return sourceId;
    }
	
	public void setId(int id) {
		sourceId = id;
	}

	public List<Programme> getProgrammes(Channel channel, int day)
			throws Exception {
				ArrayList<Channel> list = new ArrayList<Channel>(2);
				list.add(channel);
				return getProgrammes(list, day);
			}

	@Override
	public Stats getStats() {
		return stats;
	}

	@Override
	public void close() {
		cache.close();
	}

	protected String fetchURL(URL url) throws Exception {
		Thread.sleep(config.niceMilliseconds);
		StringBuffer buf = new StringBuffer();
		boolean done = false;
		for(int count = 0; !done; count++) {
			try {
				BufferedReader reader = new BufferedReader( new InputStreamReader( url.openStream()));
				String s;
				while ((s = reader.readLine()) != null) buf.append(s);
				done = true;
			} catch (IOException e) {
				if (!config.quiet) {
					logger.warn("Error fetching from url " + url + ", count="+count);
				}
				if (count>=MAX_FETCH_TRIES) { 
					stats.fetchErrors++;
					if (config.logLevel>=Config.LOG_DEBUG) e.printStackTrace();
					throw new Exception("Error getting program data from url " + url, e);
				}
			}
		}
		return buf.toString();  
	}
	
	public void clearCache() {
		cache.clear(sourceId);
	}
}