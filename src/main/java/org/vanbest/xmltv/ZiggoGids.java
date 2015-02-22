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
import java.util.Collections;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ZiggoGids extends AbstractEPGSource implements EPGSource {

    private static final String base_data_root="https://api2.ziggo-apps.nl/base_data";
    private static final String epg_data_root="https://api2.ziggo-apps.nl/programs";
    private static final String programs_data_root="https://api2.ziggo-apps.nl/program_details";
    private static final String channel_image_root="https://static.ziggo-apps.nl/images/channels/";
    private static final String program_image_root="https://static.ziggo-apps.nl/images/programs/";

    // NOTE: 
    // the base_data json object also contains information about program Genres
    // IDs, icon base urls, and kijkwijzer IDs

	private static final int MAX_PROGRAMMES_PER_DAY = 9999;
	private static final int MAX_DAYS_AHEAD_SUPPORTED_BY_ZIGGOGIDS = 7;
        //private static final int MAX_CHANNELS_PER_REQUEST = 25;
	public final static String NAME="ziggogids.nl";

	static Logger logger = Logger.getLogger(ZiggoGids.class);

        
        class Statics {
            Map<String,String> genre; // id => name
            Map<String,String> kijkwijzer; // id => description; FIXME: also contains icons
        }

        private Statics statics = null;

	public ZiggoGids(Config config) {
		super(config);
	}

	public String getName() {
		return NAME;
	}

        // https://api2.ziggo-apps.nl/programs?channelIDs=1&date=2015-02-20+8&period=3
        // period: number of hours ahead to fetch program data
        // TODO: multiple channels, "channelIDs=1,2,3"
	public static URL programmeUrl(Channel channel, int day, int hour, int period) throws Exception {
		StringBuilder s = new StringBuilder(epg_data_root);
		s.append("?channelIDs=");
		s.append(channel.id);
                GregorianCalendar cal = new GregorianCalendar();
                cal.add(Calendar.DAY_OF_MONTH, day);
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, 0);
                String date = new SimpleDateFormat("yyyy-MM-dd+HH").format(cal.getTime());
		s.append("&date="+date);
		s.append("&period="+period);

		return new URL(s.toString());
	}

    // https://api2.ziggo-apps.nl/program_details?programID=1011424477400760329465668
	public static URL detailUrl(String id) throws Exception {
		StringBuilder s = new StringBuilder(programs_data_root);
		s.append("?programID=");
		s.append(id);
		return new URL(s.toString());
	}

        public void fetchStatics() {
            if (statics != null) return;


            URL url = null;
            try {
                    url = new URL(base_data_root);
            } catch (MalformedURLException e) {
                    logger.error("Exception creating ziggo base data url", e);
            }

            JSONObject base_data;
            try {
                base_data = fetchJSON(url);
            } catch (Exception e) {
                logger.error("IO Exception trying to get ziggo base data from "+base_data_root, e);
                return;
            }

            statics = new Statics();
            statics.genre = new HashMap<String,String>();
            statics.kijkwijzer = new HashMap<String,String>();

            JSONArray genres = base_data.getJSONArray("Genres");
            for(int i=0; i<genres.size(); i++) {
                JSONObject genre = genres.getJSONObject(i);
                statics.genre.put(genre.getString("id"), genre.getString("name"));
            }

            JSONArray parentals = base_data.getJSONArray("ParentalGuidances");
            for(int i=0; i<parentals.size(); i++) {
                JSONObject parental = parentals.getJSONObject(i);
                String rating =
                parental.getString("name").replace("Kijkwijzer","").trim();
                statics.kijkwijzer.put(parental.getString("id"), rating);
            }
        }

	/*
	 * (non-Javadoc)
	 *
	 * @see org.vanbest.xmltv.EPGSource#getChannels()
	 */
	@Override
	public List<Channel> getChannels() {
		List<Channel> result = new ArrayList<Channel>(100);

                URL url = null;
		try {
			url = new URL(base_data_root);
		} catch (MalformedURLException e) {
			logger.error("Exception creating horizon channel list url", e);
		}

                JSONObject base_data;
                try {
                    base_data = fetchJSON(url);
                } catch (Exception e) {
                    logger.error("IO Exception trying to get ziggo channel list from "+base_data_root, e);
                    return result;
                }

		logger.debug("ziggogids channels json: " + base_data.toString());

                JSONArray channels = base_data.getJSONArray("Channels");
                for(int i=0; i < channels.size(); i++) {
                    JSONObject zender = channels.getJSONObject(i);
                    String name = zender.getString("name");
                    String id = zender.getString("id");
                    String xmltv = id + "." + getName();
                    String icon = channel_image_root + zender.getString("icon");
		    Channel c = Channel.getChannel(getName(), id, xmltv, name);
                    c.addIcon(icon);
		    result.add(c);
                }
		return result;
	}

	private void fillDetails(String id, Programme result) throws Exception {
		URL url = detailUrl(id);
		JSONObject json = fetchJSON(url);
                logger.debug(json.toString());
                JSONArray programs = json.getJSONArray("Program");
                JSONObject program = programs.getJSONObject(0);
                if (program.has("genre")) {
                    String genre = statics.genre.get("" + program.getInt("genre"));
    		    result.addCategory(config.translateCategory(genre));
                    // logger.debug("    FIXME genre: " + program.getInt("genre"));
                }

                JSONArray detail_list = json.getJSONArray("ProgramDetails");
                JSONObject details = detail_list.getJSONObject(0);
                if (details.has("description")) {
                    result.addDescription(details.getString("description"));
                }
                if (details.has("parentalGuidances")) {
                    // logger.debug("    FIXME kijkwijzer " + details.getJSONArray("parentalGuidances").toString());
                    JSONArray guidances = details.getJSONArray("parentalGuidances");
                    List<String> kijkwijzers = new ArrayList<String>(guidances.size());
                    for(int i=0; i<guidances.size(); i++) {
                        kijkwijzers.add(statics.kijkwijzer.get("" + guidances.getInt(i)));
                    }

                    result.addRating("kijkwijzer", StringUtils.join(kijkwijzers, ","));
                }
                if (details.has("rerun")) {
                    // TODO
                    // logger.debug("    FIXME rerun: " + details.getString("rerun"));
                    boolean rerun = details.getString("rerun").equals("true");
                    if (rerun) {
                        result.setPreviouslyShown();
                    }
                }
                if (details.has("infoUrl")) {
                    String info = details.getString("infoUrl");
                    if (info != null && ! info.isEmpty()) {
                        result.addUrl(info);
                    }
                }
                /*
                ppe: some kind of pay-per-view
                if (details.has("ppeUrl")) {
                    String ppe = details.getString("ppeUrl");
                    if (ppe != null && ! ppe.isEmpty()) {
                        logger.debug("    FIXME ppe URL: " + ppe);
                    }
                }
                */
        }
        /*
          {
             "title" : "NOS Journaal",
             "inHome" : "1",
             "endDateTime" : "2015-02-20 19:30:00",
             "id" : "1011424458800760329485668",
             "startDate" : "2015-02-20",
             "startDateTime" : "2015-02-20 19:00:00",
             "outOfCountry" : "0",
             "genre" : 12,
             "series_key" : "1_NOS Journaal",
             "outOfHome" : "1",
             "channel" : "1"
          },
        */
	private Programme programmeFromJSON(JSONObject json,
			boolean fetchDetails) throws Exception {
		String id = json.getString("id");
		Programme result = cache.get(getName(), id);
		boolean cached = (result != null);
		boolean doNotCache = false;
		if (result == null) {
			stats.cacheMisses++;
			result = new Programme();
                        if (json.has("title")){
                                result.addTitle(json.getString("title"));
                        } 
                } else {
			stats.cacheHits++;
                }

                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",
                                new Locale("nl"));
                //Calendar cal = df.getCalendar();
                //cal.setTimeZone(TimeZone.getTimeZone("UTC"));
                //df.setCalendar(cal);
                df.setTimeZone(TimeZone.getTimeZone("UTC"));

                result.startTime = df.parse(json.getString("startDateTime"));
                result.endTime = df.parse(json.getString("endDateTime"));

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
	/*
	 * (non-Javadoc)
	 *
	 * @see org.vanbest.xmltv.EPGSource#getProgrammes(java.util.List, int,
	 * boolean)
	 */
	@Override
	public List<Programme> getProgrammes(List<Channel> channels, int day)
			throws Exception {

                fetchStatics();

		List<Programme> result = new ArrayList<Programme>();
		if (day > MAX_DAYS_AHEAD_SUPPORTED_BY_ZIGGOGIDS) {
			return result; // empty list
		}

                // TODO fetch multiple channels in one go
		for (Channel c : channels) {
                        // start day hour=0 with 24 hours ahead
                        URL url = programmeUrl(c, day, 0, 24); 
                        logger.debug("url: "+url);

			JSONObject json = fetchJSON(url);
			logger.debug(json.toString());

                        JSONArray programs = json.getJSONArray("Programs");
			for (int i = 0; i < programs.size(); i++) {
				JSONObject program = programs.getJSONObject(i);
				Programme p = programmeFromJSON(program,
						config.fetchDetails);
				p.channel = c.getXmltvChannelId();
				result.add(p);
			}
                }
		return result;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Config config = Config.getDefaultConfig();
                logger.setLevel(Level.TRACE);
		ZiggoGids gids = new ZiggoGids(config);
		try {
			List<Channel> channels = gids.getChannels();
			System.out.println("Channels: " + channels);
                        
			XMLStreamWriter writer = XMLOutputFactory.newInstance()
					.createXMLStreamWriter(new FileWriter("ziggogids.xml"));
			writer.writeStartDocument();
			writer.writeCharacters("\n");
			writer.writeDTD("<!DOCTYPE tv SYSTEM \"xmltv.dtd\">");
			writer.writeCharacters("\n");
			writer.writeStartElement("tv");
			//List<Channel> my_channels = channels;
			//List<Channel> my_channels = channels.subList(0, 15);
			List<Channel> my_channels = channels.subList(0, 4);
			for (Channel c : my_channels) {
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
			logger.error("Error in ziggogids testing", e);
		}
	}

}
