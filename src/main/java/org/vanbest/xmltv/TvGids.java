package org.vanbest.xmltv;

/*
  Copyright (c) 2012 Jan-Pascal van Best <janpascal@vanbest.org>

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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.ezmorph.MorpherRegistry;
import net.sf.ezmorph.ObjectMorpher;
import net.sf.ezmorph.object.DateMorpher;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

public class TvGids extends AbstractEPGSource implements EPGSource {

	static String channels_url="http://www.tvgids.nl/json/lists/channels.php";
	static String programme_base_url="http://www.tvgids.nl/json/lists/programs.php";
	static String detail_base_url = "http://www.tvgids.nl/json/lists/program.php";
	static String html_detail_base_url = "http://www.tvgids.nl/programma/";

	static boolean initialised = false;
	
	private ProgrammeCache cache;

	public TvGids(Config config) {
		super(config);
		cache = new ProgrammeCache(config);
		if ( ! initialised ) {
			init();
			initialised = true;
		}
	}
	
	public static void init() {
		String[] formats = {"yyyy-MM-dd HH:mm:ss"};
		MorpherRegistry registry = JSONUtils.getMorpherRegistry();
		registry.registerMorpher( new DateMorpher(formats, new Locale("nl")));
		registry.registerMorpher( new ObjectMorpher() {
			 public Object morph(Object value) {
				 String s = (String) value;
				 return org.apache.commons.lang.StringEscapeUtils.unescapeHtml(s);
			 }
			 public Class morphsTo() {
				 return String.class;
			 }
			 public boolean supports(Class clazz) {
				 return clazz == String.class;
			 }
		}, true);
	}
	
	public static URL programmeUrl(List<Channel> channels, int day) throws Exception {
		StringBuilder s = new StringBuilder(programme_base_url);
		if (channels.size() < 1) {
			throw new Exception("should have at least one channel");
		}
		s.append("?channels=");
		boolean first = true;
		for(Channel i: channels) {
			if (first) {
				s.append(i.id);
				first = false;
			} else {
				s.append(","+i.id);
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

	/* (non-Javadoc)
	 * @see org.vanbest.xmltv.EPGSource#getChannels()
	 */
	@Override
	public List<Channel> getChannels() {
		List<Channel> result = new ArrayList<Channel>(10);
		URL url = null;
		try {
			url = new URL(channels_url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		StringBuffer json = new StringBuffer();
		try {

			BufferedReader reader = new BufferedReader( new InputStreamReader( url.openStream()));

			String s;
			while ((s = reader.readLine()) != null) json.append(s);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (config.logJSON()) System.out.println(json.toString());
		JSONArray jsonArray = JSONArray.fromObject( json.toString() );  
		// System.out.println( jsonArray );  
		
		for( int i=0; i<jsonArray.size(); i++ ) {
			JSONObject zender = jsonArray.getJSONObject(i);
			//System.out.println( "id: " + zender.getString("id"));
			//System.out.println( "name: " + zender.getString("name"));
			//TvGidsChannel c = new TvGidsChannel(zender.getInt("id"), zender.getString("name"), zender.getString("name_short"));
			//c.setIconUrl("http://tvgidsassets.nl/img/channels/53x27/" + c.id + ".png");
			int id = zender.getInt("id");
			String name = org.apache.commons.lang.StringEscapeUtils.unescapeHtml(zender.getString("name"));
			String icon = "http://tvgidsassets.nl/img/channels/53x27/" + id + ".png"; 
			Channel c = Channel.getChannel(Channel.CHANNEL_SOURCE_TVGIDS, Integer.toString(id), name, icon); 
			result.add(c);
		}

		return result;		
	}
	
	private JSONObject fetchJSON(URL url) throws Exception {
		String json = fetchURL(url);
		if (config.logJSON()) System.out.println(json);
		return JSONObject.fromObject( json );  
	}
	
	/* (non-Javadoc)
	 * @see org.vanbest.xmltv.EPGSource#getProgrammes(java.util.List, int, boolean)
	 */
	//@Override
	public Set<Programme> getProgrammes1(List<Channel> channels, int day, boolean fetchDetails) throws Exception {
		Set<Programme> result = new HashSet<Programme>();
		URL url = programmeUrl(channels, day);

		JSONObject jsonObject = fetchJSON(url);  
		//System.out.println( jsonObject );  
		
		for(Channel c: channels) {
			JSON ps = (JSON) jsonObject.get(c.id);
			if ( ps.isArray() ) {
				JSONArray programs = (JSONArray) ps;
				for( int i=0; i<programs.size(); i++ ) {
					JSONObject programme = programs.getJSONObject(i);
					Programme p = programmeFromJSON(programme, fetchDetails);
					p.channel = c.getXmltvChannelId();
					result.add( p );
				}
			} else { 
				JSONObject programs = (JSONObject) ps;
				for( Object o: programs.keySet() ) {
					JSONObject programme = programs.getJSONObject(o.toString());
					Programme p = programmeFromJSON(programme, fetchDetails);
					p.channel = c.getXmltvChannelId();
					result.add( p );
				}
			}
		}

		return result;
	}
	
	/*
	 * {"4":
	 * 		[{"db_id":"12436404",
	 * 			"titel":"RTL Boulevard",
	 * 			"genre":"Amusement",
	 * 			"soort":"Amusementsprogramma",
	 * 			"kijkwijzer":"",
	 * 			"artikel_id":null,
	 * 			"datum_start":"2012-03-30 23:45:00",
	 * 			"datum_end":"2012-03-31 00:40:00"},
	 * 		 {"db_id":"12436397","titel":"Teleshop 4","genre":"Overige","soort":"Homeshopping","kijkwijzer":"","artikel_id":null,"datum_start":"2012-03-31 00:40:00","datum_end":"2012-03-31 00:41:00"},
	 * 		 {"db_id":"12436398","titel":"Cupido TV","genre":"Overige","soort":"","kijkwijzer":"","artikel_id":null,"datum_start":"2012-03-31 00:41:00","datum_end":"2012-03-31 04:30:00"},
	 * 		 {"db_id":"12436399","titel":"Morning chat","genre":"Overige","soort":"","kijkwijzer":"","artikel_id":null,"datum_start":"2012-03-31 04:30:00","datum_end":"2012-03-31 06:00:00"},
	 *       ....... ]}
	 */
	private Programme programmeFromJSON(JSONObject programme, boolean fetchDetails) throws Exception {
		String id = programme.getString("db_id");
		Programme result = cache.get(id);
		boolean cached = (result != null);
		if (result == null) {
			stats.cacheMisses++;
			result = new Programme();
			// TODO other fields
			result.addTitle(programme.getString("title"));
		} else {
			stats.cacheHits++;
		}
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("nl"));
		result.startTime = df.parse(programme.getString("datum_start"));
		result.endTime =  df.parse(programme.getString("datum_end"));
		
		// p.fixup(config);
	
		if (fetchDetails && !cached) {
			fillDetails(id, result);
		}
		if (!cached) {
			// FIXME where to do this?
			cache.put(id, result);
		}
		if(config.logProgrammes()) {
			System.out.println(result.toString());
		}
		return result;
	}

	/*
	 * {"db_id":"12436404",
	 * "titel":"RTL Boulevard",
	 * "datum":"2012-03-30",
	 * "btijd":"23:45:00",
	 * "etijd":"00:40:00",
	 * "synop":"Amusementsprogramma Actualiteiten, vermaak en opinies met \u00e9\u00e9n of twee deskundigen, gasten of andere nieuwsmakers. In hoog tempo volgen afwisselende items en reportages elkaar op met de thema's showbizz, crime, royalty en lifestyle.<br><br>",
	 * "kijkwijzer":"",
	 * "genre":"Amusement",
	 * "presentatie":"Winston Gerschtanowitz, Albert Verlinde",
	 * "acteursnamen_rolverdeling":"",
	 * "regisseur":"",
	 * "zender_id":"4"}
	 */
	private void fillDetails(String id, Programme result) throws Exception {
		Pattern progInfoPattern = Pattern.compile("prog-info-content.*prog-info-footer", Pattern.DOTALL);
		Pattern infoLinePattern = Pattern.compile("<li><strong>(.*?):</strong>(.*?)</li>");
		Pattern HDPattern = Pattern.compile("HD \\d+[ip]?");
		Pattern kijkwijzerPattern = Pattern.compile("<img src=\"http://tvgidsassets.nl/img/kijkwijzer/.*?\" alt=\"(.*?)\" />");

			
		URL url = JSONDetailUrl(id);
		JSONObject json = fetchJSON(url);
		//result.details = (TvGidsProgrammeDetails) JSONObject.toBean(json, TvGidsProgrammeDetails.class);
		
		//TODO fill result objecy from json object
		
		url = HTMLDetailUrl(id);
		String clob=fetchURL(url);
		//System.out.println("clob:");
		//System.out.println(clob);
		Matcher m = progInfoPattern.matcher(clob);
		if (m.find()) {
			String progInfo = m.group();
			//System.out.println("progInfo");
			//System.out.println(progInfo);
			Matcher m2 = infoLinePattern.matcher(progInfo);
			while (m2.find()) {
				//System.out.println("    infoLine: " + m2.group());
				//System.out.println("         key: " + m2.group(1));
				//System.out.println("       value: " + m2.group(2));
				String key = m2.group(1).toLowerCase();
				String value = m2.group(2);
				if (key.equals("bijzonderheden")) {
					String[] list = value.split(",");
					for( String item: list) {
						if (item.toLowerCase().contains("teletekst")) {
							result.addSubtitle("teletext");
						} else if (item.toLowerCase().contains("breedbeeld")) {
							//result.details.breedbeeld = true;
						} else if (value.toLowerCase().contains("zwart")) {
							//result.details.blacknwhite = true;
						} else if (value.toLowerCase().contains("stereo")) {
							//result.details.stereo = true;
						} else if (value.toLowerCase().contains("herhaling")) {
							//result.details.herhaling = true;
						} else {
							Matcher m3 = HDPattern.matcher(value);
							if (m3.find()) {
								//result.details.quality = m3.group();
							} else {
								if (!config.quiet) System.out.println("  Unknown value in 'bijzonderheden': " + item);
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
					// log.debug()
					// System.out.println("  (kijkwijzer): " + p.details.kijkwijzer);
					// System.out.println("    kijkwijzer: " + kijkwijzer);
				}
			}
			
//			result.details.fixup(result, config.quiet);
//			cache.add(result.db_id, result.details);
		} else {
			stats.cacheHits++;
		}
	}

	@Override
	public Set<TvGidsProgramme> getProgrammes(List<Channel> channels, int day,
			boolean fetchDetails) throws Exception {
		// TODO Auto-generated method stub
		// dummy, wait for superclass and interface to be generalised
		return null;
	}
}
