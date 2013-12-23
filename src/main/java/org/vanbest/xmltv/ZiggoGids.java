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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Calendar;
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
import org.apache.log4j.Level;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity; 
import org.apache.http.client.methods.HttpPost;

public class ZiggoGids extends AbstractEPGSource implements EPGSource {

	static String base_url = "http://www.ziggogids.nl";
	static String channels_url = "http://www.ziggogids.nl/nl/zenders";
	static String programme_base_url = "http://www.ziggogids.nl/nl";
	static String detail_base_url = "http://www.ziggogids.nl/module/ajax/nl/program_popinfo";

	private static final int MAX_PROGRAMMES_PER_DAY = 9999;
	private static final int MAX_DAYS_AHEAD_SUPPORTED_BY_ZIGGOGIDS = 3;
        private static final int MAX_CHANNELS_PER_REQUEST = 25;

	public static String NAME = "ziggogids.nl";

	static Logger logger = Logger.getLogger(ZiggoGids.class);

	public ZiggoGids(int sourceId, Config config) {
		super(sourceId, config);
	}

	public String getName() {
		return NAME;
	}

	public static String programmeUrl(int day, int hour)
			throws Exception {
		StringBuilder s = new StringBuilder(programme_base_url);
		s.append("/");
                GregorianCalendar cal = new GregorianCalendar();
                cal.add(Calendar.DAY_OF_MONTH, day);
                cal.set(Calendar.HOUR_OF_DAY, hour);
                cal.set(Calendar.MINUTE, 0);
                String date = new SimpleDateFormat("yyyyMMdd'T'HHmm").format(cal.getTime());
		s.append(date);

		return s.toString();
	}

	public static String detailUrl(String id) {
		StringBuilder s = new StringBuilder(detail_base_url);
		s.append("/typefav=false?progid=");
		s.append(id);
		return s.toString();
	}


        private Document fetchJsoup(CloseableHttpClient client, String url) throws IOException {
            Document doc = null;

            HttpGet httpGet = new HttpGet(url);
            CloseableHttpResponse response = client.execute(httpGet);
            try {
                //logger.debug(response.getStatusLine());
                HttpEntity entity = response.getEntity();
                doc = Jsoup.parse(entity.getContent(), null, url);
                EntityUtils.consume(entity);
            } finally {
                response.close();
            }
            return doc;
        }

        private void setActiveChannel(CloseableHttpClient client, String channel) throws IOException {
            setActiveChannels(client, Collections.singletonList(channel));
        }

        private void setActiveChannels(CloseableHttpClient client, List<String> channels) throws IOException {
            Document doc;
            try { 
                HttpPost post = new HttpPost(channels_url);
                List <NameValuePair> nvps = new ArrayList <NameValuePair>();
                for(String ch: channels) {
                    nvps.add(new BasicNameValuePair("channel_selection[]", ch));
                }
                post.setEntity(new UrlEncodedFormEntity(nvps));
                CloseableHttpResponse response = client.execute(post);
                try {
                    // logger.debug(response.getStatusLine());
                    HttpEntity entity = response.getEntity();
                    doc = Jsoup.parse(entity.getContent(), null, channels_url);
                    EntityUtils.consume(entity);
                } finally {
                        response.close();
                }
            } catch (IOException e) {
                logger.error("IO Exception trying to get ziggo channel list from "+channels_url, e);
                throw e;
            }
            //logger.debug("ziggogids POST result: " + doc.outerHtml());
        }

        private String fetchIconUrl(CloseableHttpClient client, String channel) throws IOException
        {
            setActiveChannel(client, channel);

            String url = programme_base_url+"/";
            Document doc = fetchJsoup(client, url);

            // logger.debug("ziggogids programme: " + doc.outerHtml());

            Elements logos = doc.select(".gids-row-label .gids-row-channellogo");
            if (logos.size()!=1) {
                logger.error("number of channel logos for channel "+channel+" is "+logos.size()+"; should be 1");
                for(Element e: logos) {
                    String name = e.attr("title");
                    String logo_url = e.select("img").first().attr("src");
                    logger.debug("    \""+name+"\": "+logo_url);
                }
            }
            return base_url + logos.first().select("img").first().attr("src");
        }
	/*
	 * (non-Javadoc)
	 *
	 * @see org.vanbest.xmltv.EPGSource#getChannels()
	 */
	@Override
	public List<Channel> getChannels() {
		List<Channel> result = new ArrayList<Channel>(100);

                Document doc;
                CloseableHttpClient httpclient = HttpClients.createDefault();
                try {
                    doc = fetchJsoup(httpclient, channels_url);
                } catch (IOException e) {
                    logger.error("IO Exception trying to get ziggo channel list from "+channels_url, e);
                    return result;
                }

                String title = doc.title();

		// logger.debug("ziggogids channels html: " + doc.outerHtml());

                Elements fields = doc.select(".channel_field");
                for(Element e: fields) {
                    String index = e.select("input").first().attr("value");
                    String name = e.select("label").first().text();
                    logger.debug("    "+index+": \""+name+"\"");
		    Channel c = Channel.getChannel(getId(), index, name);
                    /* Too slow for now
                    try {
                        String icon = fetchIconUrl(httpclient, index);
                        logger.debug("    "+icon);
                        c.addIcon(icon);
                    } catch (IOException e2) {
                        logger.error("IO Exception trying to get channel log for channel "+index, e2);
                    }
                    */
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
		if (day > MAX_DAYS_AHEAD_SUPPORTED_BY_ZIGGOGIDS) {
			return result; // empty list
		}

                CloseableHttpClient httpclient = HttpClients.createDefault();

		for (Channel c : channels) {
                    setActiveChannel(httpclient, c.id);

                    String url = programmeUrl(day, 20); // hour
                    logger.debug("url: "+url);

                    Document doc;
                    try {
                        doc = fetchJsoup(httpclient, url);
                    } catch (IOException e) {
                        logger.error("IO Exception trying to get ziggo channel list from "+url, e);
                        return result;
                    }

//                    logger.debug("ziggogids programme: " + doc.outerHtml());

                    Elements rows = doc.select(".gids-item-row");
                    for(Element row: rows) {
                        logger.debug("*** row ***");
                        for(Element item: row.select(".gids-row-item")) {
                            Programme p = programmeFromElement(httpclient, item);
                            p.channel = c.getXmltvChannelId();
                            result.add(p);
                            logger.debug(p.toString());
                        }
                    }
                }
		return result;
	}

        private Programme programmeFromElement(CloseableHttpClient httpclient, Element item) {
            String progid = item.attr("popup-id");
            long start = Long.parseLong(item.attr("pr-start")); // unix time

            String id = Long.toString(start)+"_"+progid;
            Programme p = cache.get(getId(), id);
            boolean cached = (p != null);
            if (p == null) {
                stats.cacheMisses++;
                p = new Programme();
                String description = item.select(".gids-row-item-title").text();
                p.addTitle(description);
            } else {
                // System.out.println("From cache: " +
                // programme.getString("titel"));
                stats.cacheHits++;
            }
            p.startTime = new Date(1000L*start);
            long duration = Integer.parseInt(item.attr("pr-duration")); // minutes
            p.endTime = new Date(1000L*(start+60*duration));
            if (config.fetchDetails && ( !cached || !p.hasDescription() ) ) {
                fillDetails(httpclient, p, progid);
            }
            if (!cached) {
                // FIXME where to do this?
                cache.put(getId(), id, p);
            }
            return p;
        }

        private void fillDetails(CloseableHttpClient httpclient, Programme p, String progid) {
            Document doc;
            String url = detailUrl(progid);
            try {
                doc = fetchJsoup(httpclient, url);
            } catch (IOException e) {
                logger.error("IO Exception trying to get ziggo detail info from "+url, e);
                return;
            }
            //logger.debug("ziggogids detail: " + doc.outerHtml());
            Element desc = doc.select(".progpop_descr").first();
            if(desc!=null) p.addDescription(desc.text());
            
            Element kijkwijzer = doc.select(".progpop_kijkwijzer").first();
            if(kijkwijzer!=null) {
                // TODO
            }
            Element time = doc.select(".progpop_time").first();
            if(time!=null) {
                logger.debug("progpop_time: "+time.text());
                String genre = time.text().replaceFirst("^[^,]+,","").trim();
                logger.debug("Genre: " + genre);
                p.addCategory(config.translateCategory(genre));
            }
        }

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Config config = Config.getDefaultConfig();
                logger.setLevel(Level.TRACE);
		ZiggoGids gids = new ZiggoGids(4, config);
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
			// List<Channel> my_channels = channels;
			//List<Channel> my_channels = channels.subList(0, 15);
			List<Channel> my_channels = channels.subList(0, 3);
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
