package org.vanbest.xmltv;

/*
 Copyright (c) 2013 Jan-Pascal van Best <janpascal@vanbest.org>

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Horizon extends AbstractEPGSource implements EPGSource {

	static String channels_url = "https://www.horizon.tv/oesp/api/NL/nld/web/channels/";
	static String listings_url = "https://www.horizon.tv/oesp/api/NL/nld/web/listings";
	//  ?byStationId=28070126&sort=startTime&range=1-100&byStartTime=1362000000000~1362100000000";

	private static final int MAX_DAYS_AHEAD_SUPPORTED_BY_HORIZON = 3;

	public static String NAME = "horizon.tv";

	static Logger logger = Logger.getLogger(Horizon.class);

	public Horizon(int sourceId, Config config) {
		super(sourceId, config);
	}

	public String getName() {
		return NAME;
	}

	public static URL programmeUrl(Channel channel, int day)
			throws Exception {
		StringBuilder s = new StringBuilder(listings_url);
		s.append("?byStationId=");
		s.append(channel.id);
		s.append("&sort=startTime&range=1-100");
		Calendar startTime=Calendar.getInstance();
		startTime.set(Calendar.HOUR_OF_DAY, 0);
		startTime.set(Calendar.MINUTE, 0);
		startTime.set(Calendar.SECOND, 0);
		startTime.set(Calendar.MILLISECOND, 0);
		startTime.add(Calendar.DAY_OF_MONTH,  1);
		Calendar endTime = (Calendar) startTime.clone();
		endTime.add(Calendar.DAY_OF_MONTH,  1);
		s.append("&byStartTime=");
		s.append(startTime.getTimeInMillis());
		s.append("~");
		s.append(endTime.getTimeInMillis());

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
		URL url = null;
		try {
			url = new URL(channels_url);
		} catch (MalformedURLException e) {
			logger.error("Exception creating horizon channel list url", e);
		}
		JSONObject channels;
		try {
			channels = fetchJSON(url);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return result;
		}
		logger.debug("horizon channels json: " + channels.toString());

		int numChannels = Integer.parseInt(channels.getString("totalResults"));
		JSONArray jsonArray = channels.getJSONArray("channels");
		for (int i = 0; i < jsonArray.size(); i++) {
			JSONObject zender = jsonArray.getJSONObject(i);
			
			// System.out.println( "id: " + zender.getString("id"));
			// System.out.println( "name: " + zender.getString("name"));
			JSONArray stationSchedules=zender.getJSONArray("stationSchedules");
			assert(1 == stationSchedules.size());
			
			JSONObject firstSchedule = stationSchedules.getJSONObject(0);
			JSONObject station = firstSchedule.getJSONObject("station");
			logger.info("firstschedule: " + firstSchedule.toString());
			int id = station.getInt("id");
			String name = station.getString("title");
			JSONArray images = station.getJSONArray("images");
			String icon = "";
			Channel c = Channel.getChannel(getId(), Integer.toString(id), name,
					icon);
			result.add(c);
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
		if (day > MAX_DAYS_AHEAD_SUPPORTED_BY_HORIZON) {
			return result; // empty list
		}

		for (Channel c : channels) {
			URL url = programmeUrl(c, day);
			logger.info("Programme url:" + url);
			JSONObject jsonObject = fetchJSON(url);
			logger.info(jsonObject.toString());
			JSONArray listings = jsonObject.getJSONArray("listings");
			for (int i = 0; i < listings.size(); i++) {
				JSONObject listing = listings.getJSONObject(i);
				Programme p = programmeFromJSON(listing,
						config.fetchDetails);
				p.channel = c.getXmltvChannelId();
				result.add(p);
			}
		}

		return result;
	}

	/*
	 * {"id":"crid:~~2F~~2Feventis.nl~~2F00000000-0000-1000-0004-00000189B7F0-imi:001017B90000FCD2",
	 * "countryCode":"NL",
	 * "languageCode":"nld",
	 * "deviceCode":"web",
	 * "locationId":"15332128",
	 * "startTime":1362399000000,
	 * "endTime":1362399300000,
	 * "stationId":"28070126",
	 * "imi":"imi:001017B90000FCD2",
	 * "program":{"id":"crid:~~2F~~2Feventis.nl~~2F00000000-0000-1000-0004-00000189B7F0",
	 *             "mediaGroupId":"crid:~~2F~~2Feventis.nl~~2F00000000-0000-1000-0008-000000007784",
	 *             "title":"NOS Sportjournaal (Ned1) - 13:10",
	 *             "secondaryTitle":"NOS Sportjournaal",
	 *             "description":"Aandacht voor het actuele sportnieuws.",
	 *             "shortDescription":"Aandacht voor het actuele sportnieuws.",
	 *             "longDescription":"Aandacht voor het actuele sportnieuws.",
	 *             "countryCode":"NL",
	 *             "languageCode":"nld",
	 *             "deviceCode":"web",
	 *             "medium":"TV",
	 *             "categories":[{"id":"13946291","title":"sports","scheme":"urn:tva:metadata:cs:UPCEventGenreCS:2009"},
	 *                           {"id":"13946352","title":"sports/sports","scheme":"urn:tva:metadata:cs:UPCEventGenreCS:2009"}],
	 *             "mediaType":"Episode",
	 *             "isAdult":false,
	 *             "seriesEpisodeNumber":"50108216",
	 *             "cast":[],
	 *             "directors":[],
	 *             "videos":[],
	 *             "images":[{"assetType":"tva-boxcover","width":180,"height":260,"url":"https://www.horizon.tv/static-images/926/511/36654624.p.jpg"},
	 *                       {"assetType":"boxart-xlarge","width":210,"height":303,"url":"https://www.horizon.tv/static-images/926/511/36654624.p_210x303_34273348255.jpg"},
	 *                       {"assetType":"boxart-large","width":180,"height":260,"url":"https://www.horizon.tv/static-images/926/511/36654624.p_180x260_34273348256.jpg"},
	 *                       {"assetType":"boxart-medium","width":110,"height":159,"url":"https://www.horizon.tv/static-images/926/511/36654624.p_110x159_34273348257.jpg"},
	 *                       {"assetType":"boxart-small","width":75,"height":108,"url":"https://www.horizon.tv/static-images/926/511/36654624.p_75x108_34273348258.jpg"}]}}
	 */
	private Programme programmeFromJSON(JSONObject json,
			boolean fetchDetails) throws Exception {
		String id = json.getString("id");
		Programme result = cache.get(getId(), id);
		boolean cached = (result != null);
		if (result == null) {
			stats.cacheMisses++;
			result = new Programme();
			result.startTime = new Date(json.getLong("startTime"));
			result.endTime = new Date(json.getLong("endTime"));
			JSONObject prog = json.getJSONObject("program");
			result.addTitle(prog.getString("title"));
			/*
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
			*/
		} else {
			// System.out.println("From cache: " +
			// programme.getString("titel"));
			stats.cacheHits++;
		}
		// TODO other fields

		if (!cached) {
			// FIXME where to do this?
			cache.put(getId(), id, result);
		}
		logger.debug(result);
		return result;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Config config = Config.getDefaultConfig();
		Horizon horizon = new Horizon(1, config);
		try {
			List<Channel> channels = horizon.getChannels();
			System.out.println("Channels: " + channels);
			XMLStreamWriter writer = XMLOutputFactory.newInstance()
					.createXMLStreamWriter(new FileWriter("horizon.xml"));
			writer.writeStartDocument();
			writer.writeCharacters("\n");
			writer.writeDTD("<!DOCTYPE tv SYSTEM \"xmltv.dtd\">");
			writer.writeCharacters("\n");
			writer.writeStartElement("tv");
			// List<Channel> my_channels = channels;
			List<Channel> my_channels = channels.subList(0, 5);
			for (Channel c : my_channels) {
				c.serialize(writer);
			}
			writer.flush();
			List<Programme> programmes = horizon.getProgrammes(my_channels, 2);
			for (Programme p : programmes) {
				p.serialize(writer);
			}
			writer.writeEndElement();
			writer.writeEndDocument();
			writer.flush();
			if (!config.quiet) {
				EPGSource.Stats stats = horizon.getStats();
				System.out.println("Number of programmes from cache: "
						+ stats.cacheHits);
				System.out.println("Number of programmes fetched: "
						+ stats.cacheMisses);
				System.out.println("Number of fetch errors: "
						+ stats.fetchErrors);
			}
			horizon.close();
		} catch (Exception e) {
			logger.error("Error in horizon testing", e);
		}
	}

}
