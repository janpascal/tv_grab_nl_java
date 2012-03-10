package org.vanbest.xmltv;

import java.io.BufferedReader;
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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

public class TvGids {

	static String channels_url="http://www.tvgids.nl/json/lists/channels.php";
	static String programme_base_url="http://www.tvgids.nl/json/lists/programs.php";
	static String detail_base_url = "http://www.tvgids.nl/json/lists/program.php";

	static public List<Channel> getChannels() {
		List<Channel> result = new ArrayList<Channel>(10);
		URL url = null;
		try {
			url = new URL(channels_url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		StringBuffer json = new StringBuffer();
		try {

			BufferedReader reader = new BufferedReader( new InputStreamReader( url.openStream()));

			String s;
			while ((s = reader.readLine()) != null) {
			        json.append(s);
			     }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		String json = "[{'id':'1','name':'Nederland 1','name_short':'Ned 1'},{'id':'2','name':'Nederland 2','name_short':'Ned 2'},{'id':'3','name':'Nederland 3','name_short':'Ned 3'},{'id':'4','name':'RTL 4','name_short':'RTL 4'},{'id':'31','name':'RTL 5','name_short':'RTL 5'},{'id':'46','name':'RTL 7','name_short':'RTL 7'},{'id':'92','name':'RTL 8','name_short':'RTL 8'},{'id':'36','name':'SBS 6','name_short':'SBS 6'},{'id':'37','name':'NET 5','name_short':'NET 5'},{'id':'34','name':'Veronica','name_short':'Veronica'},{'id':'29','name':'Discovery Channel','name_short':'Discovery'},{'id':'18','name':'National Geographic','name_short':'NGC'},{'id':'84','name':'Het Gesprek','name_short':'HetGesprek'},{'id':'406','name':'NostalgieNet','name_short':'Nost Net'},{'id':'5','name':'E&eacute;n','name_short':'E&eacute;n'},{'id':'6','name':'KETNET/Canvas','name_short':'KET/Can'},{'id':'25','name':'MTV','name_short':'MTV'},{'id':'405','name':'TLC','name_short':'tlc'},{'id':'91','name':'Comedy Central','name_short':'Com. Centr.'},{'id':'49','name':'VTM','name_short':'VTM'},{'id':'59','name':'2BE','name_short':'2BE'},{'id':'89','name':'Nickelodeon','name_short':'Nick'},{'id':'60','name':'VT4','name_short':'VT4'},{'id':'90','name':'BVN','name_short':'BVN'},{'id':'404','name':'FOXlife','name_short':'FOXlife'},{'id':'7','name':'BBC 1','name_short':'BBC 1'},{'id':'8','name':'BBC 2','name_short':'BBC 2'},{'id':'86','name':'BBC World','name_short':'BBC W'},{'id':'9','name':'ARD','name_short':'ARD'},{'id':'10','name':'ZDF','name_short':'ZDF'},{'id':'13','name':'NDR Fernsehen','name_short':'NDR'},{'id':'14','name':'S&uuml;dwest Fernsehen','name_short':'SWF'},{'id':'12','name':'WDR Fernsehen','name_short':'WDR'},{'id':'50','name':'3Sat','name_short':'3Sat'},{'id':'28','name':'Sat 1','name_short':'Sat 1'},{'id':'11','name':'RTL','name_short':'RTL'},{'id':'58','name':'PRO 7','name_short':'PRO 7'},{'id':'15','name':'RTBF La 1','name_short':'RTBF La1'},{'id':'16','name':'RTBF La 2','name_short':'RTBF La2'},{'id':'17','name':'TV 5','name_short':'TV 5'},{'id':'27','name':'Rai Uno','name_short':'Rai Uno'},{'id':'32','name':'TRT int.','name_short':'TRT'},{'id':'40','name':'AT 5','name_short':'AT 5'},{'id':'24','name':'Film 1 Premiere','name_short':'Film1 Prem.'},{'id':'39','name':'Film 1 Family','name_short':'Film1 Fam.'},{'id':'107','name':'Film 1 Festival','name_short':'Film1 Fest.'},{'id':'35','name':'TMF','name_short':'TMF'},{'id':'73','name':'Mezzo','name_short':'Mezzo'},{'id':'21','name':'Cartoon Network','name_short':'Cart. Net.'},{'id':'26','name':'CNN','name_short':'CNN'},{'id':'19','name':'Eurosport','name_short':'Eurosport'},{'id':'99','name':'Sport1','name_short':'Sport1'},{'id':'20','name':'TCM','name_short':'TCM'},{'id':'65','name':'Animal Planet','name_short':'Animal Pl.'},{'id':'87','name':'TV E','name_short':'TVE'},{'id':'38','name':'ARTE','name_short':'ARTE'},{'id':'103','name':'RTV Noord-Holland','name_short':'RTV N-H'},{'id':'100','name':'RTV Utrecht','name_short':'Utrecht'},{'id':'101','name':'RTV West','name_short':'RTV West'},{'id':'102','name':'RTV Rijnmond','name_short':'Rijnmond'},{'id':'104','name':'BBC Entertainment','name_short':'BBC E'},{'id':'105','name':'Private Spice','name_short':'Private Sp.'},{'id':'93','name':'13TH STREET','name_short':'13TH ST'},{'id':'94','name':'Syfy','name_short':'Syfy'},{'id':'109','name':'Omrop Frysl&acirc;n','name_short':'Frysl&acirc;n'},{'id':'112','name':'Omroep Gelderland','name_short':'Gelderland'},{'id':'115','name':'L1 TV','name_short':'L1 TV'},{'id':'110','name':'RTV Drenthe','name_short':'Drenthe'},{'id':'113','name':'Omroep Flevoland','name_short':'Flevoland'},{'id':'116','name':'Omroep Zeeland','name_short':'Zeeland'},{'id':'108','name':'RTV Noord','name_short':'RTV Noord'},{'id':'111','name':'RTV Oost','name_short':'RTV Oost'},{'id':'114','name':'Omroep Brabant','name_short':'Brabant'},{'id':'67','name':'Consumenten 24','name_short':'Cons24'},{'id':'81','name':'HollandDoc 24','name_short':'HolDoc24'},{'id':'314','name':'Journaal 24','name_short':'Journaal24'},{'id':'316','name':'Best 24','name_short':'Best24'},{'id':'401','name':'Playboy TV','name_short':'Playboy'},{'id':'306','name':'Discovery Science','name_short':'Disc. Sc.'},{'id':'403','name':'Goed TV','name_short':'Goed TV'},{'id':'303','name':'Hallmark','name_short':'Hallmark'},{'id':'300','name':'BBC 3','name_short':'BBC 3'},{'id':'310','name':'3voor12 Portal','name_short':''},{'id':'64','name':'Familie 24','name_short':'Fam24'},{'id':'69','name':'Sterren 24','name_short':'Sterren24'},{'id':'82','name':'Geschiedenis 24','name_short':'Gesch24'},{'id':'312','name':'Nick Jr.','name_short':'Nick Jr.'},{'id':'311','name':'Disney XD','name_short':'Disney XD'},{'id':'317','name':'Comedy Family','name_short':'Com. Fam.'},{'id':'402','name':'Adult Channel','name_short':'Adult'},{'id':'305','name':'Discovery World','name_short':'Disc. World'},{'id':'308','name':'3voor12 Central','name_short':''},{'id':'148','name':'Eredivisie Live','name_short':'Eredivisie'},{'id':'66','name':'HumorTV 24','name_short':'Humor24'},{'id':'70','name':'Cultura 24','name_short':'Cult24'},{'id':'83','name':'3voor12','name_short':''},{'id':'313','name':'Boomerang','name_short':'Boomerang'},{'id':'315','name':'Zone Reality','name_short':'ZoneReality'},{'id':'400','name':'Hustler TV','name_short':'Hustler'},{'id':'307','name':'Discovery Travel &amp; Living','name_short':'Disc T&amp;L'},{'id':'304','name':'MGM','name_short':'MGM'},{'id':'301','name':'BBC 4','name_short':'BBC 4'},{'id':'309','name':'3voor12 On Stage','name_short':''},{'id':'33','name':'Spirit 24','name_short':'Spirit24'}]";

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
	
		
	static public Set<Programme> getProgrammes(List<Channel> channels, int day, boolean fetchDetails) throws Exception {
		Set<Programme> result = new HashSet<Programme>();
		URL url = programmeUrl(channels, day);

		JSONObject jsonObject = fetchJSON(url);  
		System.out.println( jsonObject );  
		
		String[] formats = {"yyyy-MM-dd HH:mm:ss"};
		JSONUtils.getMorpherRegistry().registerMorpher( new DateMorpher(formats, new Locale("nl")));

		for( Channel i: channels) {
			JSONObject programs = jsonObject.getJSONObject(""+i.id);
			System.out.println( programs );

			for( Object o: programs.keySet() ) {
				JSONObject programme = programs.getJSONObject(o.toString());
				Programme p = (Programme) JSONObject.toBean(programme, Programme.class);
				if (fetchDetails) {
					p.details = getDetails(p.db_id);
				}
				p.channel = i;
				result.add( p );
			}
		}

		return result;
	}
	
	private static JSONObject fetchJSON(URL url) throws Exception {
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

	private static ProgrammeDetails getDetails(String db_id) throws Exception {
		Set<Programme> result = new HashSet<Programme>();
		URL url = detailUrl(db_id);
		JSONObject json = fetchJSON(url);
		//System.out.println( json );  
		ProgrammeDetails d = (ProgrammeDetails) JSONObject.toBean(json, ProgrammeDetails.class);
		return d;
	}
}
