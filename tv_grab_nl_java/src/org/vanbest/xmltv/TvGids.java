package org.vanbest.xmltv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.sf.ezmorph.object.DateMorpher;
import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

public class TvGids {

	static String channels_url="http://www.tvgids.nl/json/lists/channels.php";
	static String programme_base_url="http://www.tvgids.nl/json/lists/programs.php";
	static String detail_base_url = "http://www.tvgids.nl/json/lists/program.php";

	ProgrammeCache cache;
	boolean initialised = false;
	
	public TvGids(File cacheFile) {
		cache = new ProgrammeCache(cacheFile);
		if ( ! initialised ) {
			String[] formats = {"yyyy-MM-dd HH:mm:ss"};
			JSONUtils.getMorpherRegistry().registerMorpher( new DateMorpher(formats, new Locale("nl")));
			initialised = true;
		}
	}
	
	public void close() throws FileNotFoundException, IOException {
		cache.close();
	}

	static public List<Channel> getChannels() {
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

		JSONArray jsonArray = JSONArray.fromObject( json.toString() );  
		// System.out.println( jsonArray );  
		
		for( int i=0; i<jsonArray.size(); i++ ) {
			JSONObject zender = jsonArray.getJSONObject(i);
			//System.out.println( "id: " + zender.getString("id"));
			//System.out.println( "name: " + zender.getString("name"));
			result.add( new Channel(zender.getInt("id"), zender.getString("name"), zender.getString("name_short")));
		}

		return result;
		
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
	
	public static URL detailUrl(String id) throws Exception {
		StringBuilder s = new StringBuilder(detail_base_url);
		s.append("?id=");
		s.append(id);
		return new URL(s.toString());
	}
		
	public Set<Programme> getProgrammes(List<Channel> channels, int day, boolean fetchDetails) throws Exception {
		Set<Programme> result = new HashSet<Programme>();
		URL url = programmeUrl(channels, day);

		JSONObject jsonObject = fetchJSON(url);  
		//System.out.println( jsonObject );  
		
		for( Channel c: channels) {
			JSON ps = (JSON) jsonObject.get(""+c.id);
			//System.out.println( ps );
			if ( ps.isArray() ) {
				JSONArray programs = (JSONArray) ps;
				for( int i=0; i<programs.size(); i++ ) {
					JSONObject programme = programs.getJSONObject(i);
					Programme p = (Programme) JSONObject.toBean(programme, Programme.class);
					if (fetchDetails) {
						p.details = getDetails(p.db_id);
					}
					p.channel = c;
					result.add( p );
				}
			} else { 
				JSONObject programs = (JSONObject) ps;
				for( Object o: programs.keySet() ) {
					JSONObject programme = programs.getJSONObject(o.toString());
					Programme p = (Programme) JSONObject.toBean(programme, Programme.class);
					if (fetchDetails) {
						p.details = getDetails(p.db_id);
					}
					p.channel = c;
					result.add( p );
				}
			}
		}

		return result;
	}
	
	private JSONObject fetchJSON(URL url) throws Exception {
		Thread.sleep(100);
		StringBuffer json = new StringBuffer();
		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader( url.openStream()));
			String s;
			while ((s = reader.readLine()) != null) json.append(s);
		} catch (IOException e) {
			throw new Exception("Error getting program data", e);
		}
		return JSONObject.fromObject( json.toString() );  
	}

	private ProgrammeDetails getDetails(String db_id) throws Exception {
		ProgrammeDetails d = cache.getDetails(db_id);
		if ( d != null ) {
			return d;
		}
		URL url = detailUrl(db_id);
		JSONObject json = fetchJSON(url);
		//System.out.println( json );  
		d = (ProgrammeDetails) JSONObject.toBean(json, ProgrammeDetails.class);
		cache.add(db_id, d);
		return d;
	}
}
