package org.vanbest.xmltv;

/*
 Copyright (c) 2012-2015 Jan-Pascal van Best <janpascal@vanbest.org>

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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class TvGidsTv extends AbstractEPGSource implements EPGSource {

	static String BASE_URL = "http://www.tvgids.tv";
	static String CHANNEL_BASE_URL = BASE_URL + "/zenders";
	static String DETAIL_BASE_URL = BASE_URL + "/tv";

	private static final int MAX_PROGRAMMES_PER_DAY = 9999;
	private static final int MAX_DAYS_AHEAD_SUPPORTED_BY_TVGIDS = 3;
	public static final String NAME="tvgids.tv";

	static Logger logger = Logger.getLogger(TvGids.class);

	public TvGidsTv(Config config) {
        	super(config);
	}

	public String getName() {
	    return NAME;
	}
	
	public static String programmeUrl(Channel channel, int day)
			throws Exception 
	{
		return CHANNEL_BASE_URL + "/" + channel.id + "/" + day;
	}

	/*
	public static URL programmeUrl(List<Channel> channels, int day)
			throws Exception {
		StringBuilder s = new StringBuilder(programme_base_url);
		if (channels.size() < 1) {
			throw new Exception("should have at least one channel");
		}
		s.append("?channels=");
		boolean first = true;
		for (Channel i : channels) {
			if (first) {
				s.append(i.id);
				first = false;
			} else {
				s.append("," + i.id);
			}
		}
		s.append("&day=");
		s.append(day);

		return new URL(s.toString());
	}

	public static URL JSONDetailUrl(String id) throws Exception {
		StringBuilder s = new StringBuilder(detail_base_url);
		s.append("?id=");
		s.append(id);
		return new URL(s.toString());
	}

	public static URL HTMLDetailUrl(String id) throws Exception {
		StringBuilder s = new StringBuilder(html_detail_base_url);
		s.append(id);
		s.append("/");
		return new URL(s.toString());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.vanbest.xmltv.EPGSource#getChannels()
	 */
	@Override
	public List<Channel> getChannels() {
		List<Channel> result = new ArrayList<Channel>(10);

		Document doc;
		try {
			doc = Jsoup.connect(CHANNEL_BASE_URL).get();
		} catch (IOException e) {
			logger.error("Exception reading tvgids.tv channel list", e);
			return result;
		}

		Elements links = doc.select("div.channels a[href^=/zenders/]");
		for (Element link: links) {
			logger.debug(link.toString());
			String name = link.select("div.channel-name").text();
			String url = link.attr("href");
			String id = url.replace("/zenders/", "");
			Element iconElement = link.select("div.channel-icon").first();
			String iconUrl = null;
			if (iconElement != null) {
				Set<String> classNames = iconElement.classNames();
				for(String s: classNames) {
					if (s.startsWith("sprite-channel")) {
						String sprite = s.replace("sprite-channel-", "");
						iconUrl = "http://images.cdn.tvgids.tv/channels/channel_" + sprite + "_BIG@2x.png"; 
					}
				}
			}
			if (id != null) {
				Channel c = Channel.getChannel(getName(), id, name);
				if (iconUrl != null) c.addIcon(iconUrl);
				result.add(c);
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.vanbest.xmltv.EPGSource#getProgrammes(java.util.List, int,
	 * boolean)
	 */
	@Override
	public List<Programme> getProgrammes(List<Channel> channels, int day)
			throws Exception {
		List<Programme> result = new ArrayList<Programme>();

		for (Channel c : channels) {
			Document doc;
			try {
				logger.debug("Programme url: " + programmeUrl(c, day));
				doc = Jsoup.connect(programmeUrl(c, day)).get();
			} catch (IOException e) {
				logger.error("Exception reading tvgids.tv programme list for " + c.defaultName() + " @" + day, e);
				return result;
			}
	
			Elements links = doc.select("a.section-item");
			boolean afternoon = false;
			for (Element link: links) {
				// logger.debug(link.toString());
				String detailUrl = BASE_URL + link.attr("href");
				String programmeId = link.attr("href").replace("/tv/", "");
				String timeTitle = link.select(".section-item-title").text();
				String[] parts = timeTitle.trim().split(" ", 2);
				String time = parts[0];
				String title = parts[1];
				if (parts.length!=2) {
					logger.error("Programme time/title weird: \"" + timeTitle + "\"");
					continue;
				}
				Calendar cal = Calendar.getInstance(Locale.forLanguageTag("nl-NL"));
				//SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
				cal.setTimeZone(TimeZone.getTimeZone("Europe/Amsterdam"));
				//Date date = sdf.parse(time);
				String[] time_parts = time.split(":", 2);
				if (time_parts.length!=2) { 
					logger.error("Programme time weird: \"" + timeTitle + "\"");
					continue;
				}
				int hour = Integer.parseInt(time_parts[0]);
				int minute = Integer.parseInt(time_parts[1]);
				cal.set(Calendar.HOUR_OF_DAY, hour);
				cal.set(Calendar.MINUTE, minute);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				if (hour >= 15) afternoon = true;
				if (hour < 11 && afternoon) {
					// We've rolled into the night, so it's the next day
					// We're supposing that the programmes are time-ordered here
					cal.add(Calendar.DAY_OF_MONTH, 1);
				}
				
				Programme p = cache.get(getName(), programmeId);
				boolean cached = (p != null);
				if (p == null) {
					stats.cacheMisses++;
					p = new Programme();
					p.channel = c.getXmltvChannelId();
					// Do this here, because we can only add to these fields. Pity if
					// they're updated
					p.addTitle(title);
				} else {
					// System.out.println("From cache: " +
					// programme.getString("titel"));
					stats.cacheHits++;
				}
				p.startTime = cal.getTime();

				//logger.trace("    Programme \"" + title + "\" at " + time + " (" + cal.getTime().toString() + "); details " + detailUrl);

				if (config.fetchDetails && !cached) {
					// TODO also read details if those have not been cached
					fillDetails(detailUrl, p);
				}
				if (!cached) {
					// FIXME where to do this?
					cache.put(getName(), programmeId, p);
				}
				logger.debug(p.toString());
				result.add(p);
			}
		}

			/*
		if (day > MAX_DAYS_AHEAD_SUPPORTED_BY_TVGIDS) {
			return result; // empty list
		}

		URL url = programmeUrl(channels, day);t

		JSONObject jsonObject = fetchJSON(url);

		for (Channel c : channels) {
			JSON ps = (JSON) jsonObject.get(c.id);
			if (ps.isArray()) {
				JSONArray programs = (JSONArray) ps;
				for (int i = 0; i < programs.size()
						&& i < MAX_PROGRAMMES_PER_DAY; i++) {
					JSONObject programme = programs.getJSONObject(i);
					Programme p = programmeFromJSON(programme,
							config.fetchDetails);
					p.channel = c.getXmltvChannelId();
					result.add(p);
				}
			} else {
				JSONObject programs = (JSONObject) ps;
				int count = 0;
				for (Object o : programs.keySet()) {
					if (count > MAX_PROGRAMMES_PER_DAY)
						break;
					JSONObject programme = programs.getJSONObject(o.toString());
					Programme p = programmeFromJSON(programme,
							config.fetchDetails);
					p.channel = c.getXmltvChannelId();
					result.add(p);
					count++;
				}
			}
		}
*/
		return result;
	}
/*
	private Programme programmeFromJSON(JSONObject programme,
			boolean fetchDetails) throws Exception {
		String id = programme.getString("db_id");
		Programme result = cache.get(getName(), id);
		boolean cached = (result != null);
		if (result == null) {
			stats.cacheMisses++;
			result = new Programme();
			// Do this here, because we can only add to these fields. Pity if
			// they're updated
			result.addTitle(programme.getString("titel"));
			String genre = programme.getString("genre");
			if (genre != null && !genre.isEmpty())
				result.addCategory(config.translateCategory(genre));
			String kijkwijzer = programme.getString("kijkwijzer");
			if (kijkwijzer != null && !kijkwijzer.isEmpty()) {
				List<String> list = parseKijkwijzer(kijkwijzer);
				if (config.joinKijkwijzerRatings) {
					// mythtv doesn't understand multiple <rating> tags
					result.addRating("kijkwijzer", StringUtils.join(list, ","));
				} else {
					for (String rating : list) {
						result.addRating("kijkwijzer", rating);
					}
				}
				// TODO add icon from HTML detail page
			}
		} else {
			// System.out.println("From cache: " +
			// programme.getString("titel"));
			stats.cacheHits++;
		}
		logger.trace("      titel:" + programme.getString("titel"));
		logger.trace("datum_start:" + programme.getString("datum_start"));
		logger.trace("  datum_end:" + programme.getString("datum_end"));
		logger.trace("      genre:" + programme.getString("genre"));
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
				new Locale("nl"));
		result.startTime = df.parse(programme.getString("datum_start"));
		result.endTime = df.parse(programme.getString("datum_end"));
		// TODO other fields

		if (fetchDetails && !cached) {
			// TODO also read details if those have not been cached
			fillDetails(id, result);
		}
		if (!cached) {
			// FIXME where to do this?
			cache.put(getName(), id, result);
		}
		logger.debug(result);
		return result;
	}
*/

/*
	private void fillDetails(String id, Programme result) throws Exception {
		try {
			fillJSONDetails(id, result);
		} catch (Exception e) {
			logger.warn("Error fetching details for programme "
					+ result.toString());
		}
		try {
			fillScraperDetails(id, result);
		} catch (Exception e) {
			logger.warn("Error fetching details for programme "
					+ result.toString());
		}

		if ((result.secondaryTitles == null || result.secondaryTitles.isEmpty())
				&& (!result.hasCategory("movies") && !result
						.hasCategory("film"))) {
			for (Programme.Title t : result.titles) {
				String[] parts = t.title.split("\\s*:\\s*", 2);
				if (parts.length >= 2 && parts[0].length() >= 5) {
					logger.debug("Splitting title from \"" + t.title
							+ "\" to: \"" + parts[0].trim()
							+ "\"; sub-title: \"" + parts[1].trim() + "\"");
					t.title = parts[0].trim();
					result.addSecondaryTitle(parts[1].trim());
				}
			}
		}
	}
*/
	/*
	 * {"db_id":"12436404", "titel":"RTL Boulevard", "datum":"2012-03-30",
	 * "btijd":"23:45:00", "etijd":"00:40:00", "synop":
	 * "Amusementsprogramma Actualiteiten, vermaak en opinies met \u00e9\u00e9n of twee deskundigen, gasten of andere nieuwsmakers. In hoog tempo volgen afwisselende items en reportages elkaar op met de thema's showbizz, crime, royalty en lifestyle.<br><br>"
	 * , "kijkwijzer":"", "genre":"Amusement",
	 * "presentatie":"Winston Gerschtanowitz, Albert Verlinde",
	 * "acteursnamen_rolverdeling":"", "regisseur":"", "zender_id":"4"}
	 */
/*	
	private void fillJSONDetails(String id, Programme result) throws Exception {
		URL url = JSONDetailUrl(id);
		JSONObject json = fetchJSON(url);
		Set<String> keys = json.keySet();
		for (String key : keys) {
			String value = StringEscapeUtils.unescapeHtml(json.getString(key));
			if (value.isEmpty())
				continue;
			if (key.equals("synop")) {
				value = value.replaceAll("<br>", " ").replaceAll("<br />", " ")
						.replaceAll("<p[^>]*>", " ").replaceAll("</p>", " ")
						.replaceAll("<strong>", " ")
						.replaceAll("</strong>", " ").replaceAll("<em>", " ")
						.replaceAll("</em>", " ").trim();
				if (value.isEmpty())
					continue;
				result.addDescription(value);
			} else if (key.equals("presentatie")) {
				String[] parts = value.split(",");
				for (String s : parts) {
					result.addPresenter(s.trim());
				}
			} else if (key.equals("acteursnamen_rolverdeling")) {
				// TODO hoe zouden rollen kunnen worden aangegeven? Geen
				// voorbeelden van gezien.
				String[] parts = value.split(",");
				for (String s : parts) {
					result.addActor(s.trim());
				}
			} else if (key.equals("regisseur")) {
				String[] parts = value.split(",");
				for (String s : parts) {
					result.addDirector(s.trim());
				}
			} else if (key.equals("kijkwijzer")) {
				// TODO
			} else if (key.equals("db_id")) {
				// ignore
			} else if (key.equals("titel")) {
				// ignore
			} else if (key.equals("datum")) {
				// ignore
			} else if (key.equals("btijd")) {
				// ignore
			} else if (key.equals("etijd")) {
				// ignore
			} else if (key.equals("genre")) {
				// ignore
			} else if (key.equals("zender_id")) {
				// ignore
			} else {
				logger.warn("Unknown key in tvgids.nl json details: \"" + key
						+ "\"");
			}
		}
	}
*/
	private void fillDetails(String detailUrl, Programme result)
			throws Exception {
		Pattern progInfoPattern = Pattern.compile(
				"prog-info-content.*prog-info-footer", Pattern.DOTALL);
		Pattern infoLinePattern = Pattern
				.compile("<li><strong>(.*?):</strong>(.*?)</li>");
		Pattern HDPattern = Pattern.compile("HD \\d+[ip]?");
		Pattern kijkwijzerPattern = Pattern
				.compile("<img src=\"http://tvgidsassets.nl/img/kijkwijzer/.*?\" alt=\"(.*?)\" />");
		
		Document doc;
		try {
			doc = Jsoup.connect(detailUrl).get();
		} catch (IOException e) {
			logger.error("Exception reading tvgids.tv detail for programme " + detailUrl, e);
			return;
		}
		
		Elements details = doc.select(".program-details dt");
		for(Element element: details)
		{
			//logger.debug("    " + element.nodeName() + ": " + element.text());
			Element next = element.nextElementSibling();
			//logger.debug("     > " + next.nodeName() + ": " + next.text());
			String key = element.text().toLowerCase();
			String value = next.text();
			if (key.equals("datum")) {
			
			} else if (key.equals("tijd")) {
					
			} else if (key.equals("genre")) {
				
			} else if (key.equals("deel-url")) {
				result.addUrl(value);
				logger.trace(element.toString());
				logger.trace(next.toString());
			} else if (key.equals("presentatie")) {
				String[] presenters = value.split(",");
				for(String presenter: presenters) {
					result.addPresenter(presenter.trim());
				}
			} else if (key.equals("jaar")) {
				
			} else if (key.equals("acteurs")) {
				String[] actors = value.split(",");
				for(String actor: actors) {
					result.addActor(actor.trim());
				}
			} else if (key.equals("regisseur")) {
				result.addDirector(value);
			} else if (key.equals("officiële website")) {
				result.addUrl(value);
			} else if (key.equals("twitter hashtag")) {
				
			} else if (key.equals("officiële twitter")) {
				
			} else if (key.equals("uitzending gemist")) {
				//logger.debug("Uitzending gemist: \"" + value + "\"");
				//logger.trace(element.toString());
				//logger.trace(next.toString());
				//logger.debug("    gemist URL: " + next.select("a[href]").attr("href"));
				result.addUrl(next.select("a[href]").attr("href"));
			} else if (key.equals("imdb")) {
				logger.trace(element.toString());
				logger.trace(next.toString());
			} else {
				logger.warn("Unknown details element \"" + key + "\": \"" + value + "\"");
			}
		}

		Elements descElements = doc.select(".section-item p");
		//logger.debug("Description: " + descElements.text() );
		
/*
		URL url = HTMLDetailUrl(id);
		String clob = fetchURL(url);
		Matcher m = progInfoPattern.matcher(clob);
		if (m.find()) {
			String progInfo = m.group();
			Matcher m2 = infoLinePattern.matcher(progInfo);
			while (m2.find()) {
				logger.trace("    infoLine: " + m2.group());
				logger.trace("         key: " + m2.group(1));
				logger.trace("       value: " + m2.group(2));
				String key = m2.group(1).toLowerCase();
				String value = m2.group(2);
				if (key.equals("bijzonderheden")) {
					String[] list = value.split(",");
					for (String item : list) {
						if (item.toLowerCase().contains("teletekst")) {
							result.addSubtitle("teletext");
						} else if (item.toLowerCase().contains("breedbeeld")) {
							result.setVideoAspect("16:9");
						} else if (value.toLowerCase().contains("zwart")) {
							result.setVideoColour(false);
						} else if (value.toLowerCase().contains("stereo")) {
							result.setAudioStereo("stereo");
						} else if (value.toLowerCase().contains("herhaling")) {
							result.setPreviouslyShown();
						} else {
							Matcher m3 = HDPattern.matcher(value);
							if (m3.find()) {
								result.setVideoQuality(m3.group());
							} else {
								logger.warn("  Unknown value in 'bijzonderheden': "
										+ item);
							}
						}
					}
				} else {
					// ignore other keys for now
				}
				Matcher m3 = kijkwijzerPattern.matcher(progInfo);
				List<String> kijkwijzer = new ArrayList<String>();
				while (m3.find()) {
					kijkwijzer.add(m3.group(1));
				}
				if (!kijkwijzer.isEmpty()) {
					// logger.debug("    kijkwijzer: " + kijkwijzer);
				}
			}
		}
		*/
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger.getRootLogger().setLevel(Level.TRACE);
		Config config = Config.getDefaultConfig();
		TvGidsTv gids = new TvGidsTv(config);
		gids.clearCache();
		try {
			List<Channel> channels = gids.getChannels();
			System.out.println("Channels: " + channels);
			XMLStreamWriter writer = XMLOutputFactory.newInstance()
					.createXMLStreamWriter(new FileWriter("tvgids.tv.xml"));
			writer.writeStartDocument();
			writer.writeCharacters("\n");
			writer.writeDTD("<!DOCTYPE tv SYSTEM \"xmltv.dtd\">");
			writer.writeCharacters("\n");
			writer.writeStartElement("tv");
			// List<Channel> my_channels = channels;
			List<Channel> my_channels = channels.subList(0, 2);
			for (Channel c : channels) {
				c.serialize(writer, true);
			}
			writer.flush();
			List<Programme> programmes = gids.getProgrammes(my_channels, 2);
			for (Programme p : programmes) {
				p.serialize(writer);
			}
			writer.writeEndElement();
			writer.writeEndDocument();
			writer.flush();
			if (!config.quiet) {
				EPGSource.Stats stats = gids.getStats();
				System.out.println("Number of programmes from cache: "
						+ stats.cacheHits);
				System.out.println("Number of programmes fetched: "
						+ stats.cacheMisses);
				System.out.println("Number of fetch errors: "
						+ stats.fetchErrors);
			}
			gids.close();
		} catch (Exception e) {
			logger.error("Error in tvgids testing", e);
		}
	}

}
