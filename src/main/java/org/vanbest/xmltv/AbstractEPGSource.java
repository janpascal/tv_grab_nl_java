package org.vanbest.xmltv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public abstract class AbstractEPGSource implements EPGSource {

	private int sourceId;
	protected Config config;
	protected ProgrammeCache cache;
	protected Stats stats = new Stats();
	static Logger logger = Logger.getLogger(AbstractEPGSource.class);

	public static final int MAX_FETCH_TRIES = 5;

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
		StringBuffer buf = new StringBuffer();
		boolean done = false;
		for (int count = 0; !done; count++) {
			Thread.sleep(config.niceMilliseconds*(1<<count));
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(url.openStream()));
				String s;
				while ((s = reader.readLine()) != null)
					buf.append(s);
				done = true;
			} catch (IOException e) {
				if (!config.quiet) {
					logger.warn("Error fetching from url " + url + ", count="
							+ count);
				}
				if (count >= MAX_FETCH_TRIES) {
					stats.fetchErrors++;
					logger.debug("Error getting progrm data from url", e);
					throw new Exception("Error getting program data from url "
							+ url, e);
				}
			}
		}
		return buf.toString();
	}

	public void clearCache() {
		cache.clear(sourceId);
	}

	List<String> parseKijkwijzer(String s) {
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
			case 'a':
				result.add("Angst");
				break;
			case 'd':
				result.add("Discriminatie");
				break;
			case 's':
				result.add("Seks");
				break;
			case 'h':
				result.add("Drugs/Alcohol");
				break;
			case 'g':
				result.add("Geweld");
				break;
			case 't':
				result.add("Grof taalgebruik");
				break;
			case '1':
				result.add("Voor alle leeftijden");
				break;
			case '2':
				result.add("Afgeraden voor kinderen jonger dan 6 jaar");
				break;
			case '9':
				result.add("Afgeraden voor kinderen jonger dan 9 jaar");
				break;
			case '3':
				result.add("Afgeraden voor kinderen jonger dan 12 jaar");
				break;
			case '4':
				result.add("Afgeraden voor kinderen jonger dan 16 jaar");
				break;
			case '5':
				break; // Lijkt op een foutje van RTL, bedoeld wordt wrsch
						// "12 jaar en ouder". Wordt op RTL tvgids niet
						// weegegeven.
			default:
				if (!config.quiet) {
					logger.warn("Unknown kijkwijzer character: " + c
							+ " in string " + s);
				}
			}
		}
		return result;
	}

}
