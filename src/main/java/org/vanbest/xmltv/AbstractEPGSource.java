package org.vanbest.xmltv;

/*
Copyright (c) 2012-2013 Jan-Pascal van Best <janpascal@vanbest.org>

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;

public abstract class AbstractEPGSource implements EPGSource {

	protected Config config;
	protected ProgrammeCache cache;
	protected Stats stats = new Stats();
	static Logger logger = Logger.getLogger(AbstractEPGSource.class);

	public static final int MAX_FETCH_TRIES = 5;

	public AbstractEPGSource(Config config) {
		this.config = config;
		cache = new ProgrammeCache(config);
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
		return fetchURL(url, Charset.defaultCharset().name());
	}
	
	protected String fetchURL(URL url, String charset) throws Exception {
		StringBuffer buf = new StringBuffer();
		boolean done = false;
		for (int count = 0; !done; count++) {
			Thread.sleep(config.niceMilliseconds*(1<<count));
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(url.openStream(), charset));
				String s;
				while ((s = reader.readLine()) != null) {
					buf.append(s);
                                        buf.append("\n");
                                }
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

	protected JSONObject fetchJSON(URL url, String charset) throws Exception {
		String json = fetchURL(url, charset);
		logger.debug(json);
		return JSONObject.fromObject(json);
	}

	protected JSONObject fetchJSON(URL url) throws Exception {
		return fetchJSON(url, Charset.defaultCharset().name());
	}

	public void clearCache() {
		cache.clear(getName());
	}

        String kijkwijzerCategorie(char c) {
            switch (c) {
            case 'a':
                    return("Angst");
            case 'd':
                    return("Discriminatie");
            case 's':
                    return("Seks");
            case 'h':
                    return("Drugs/Alcohol");
            case 'g':
                    return("Geweld");
            case 't':
                    return("Grof taalgebruik");
            default:
                    return null;
            }
        }


	List<String> parseKijkwijzer(String s) {
		List<String> result = new ArrayList<String>();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
                        String tekst = kijkwijzerCategorie(c);
                        if(tekst!=null) {
                            result.add(tekst);
                        } else {
                            switch (c) {
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
		}
		return result;
	}

}
