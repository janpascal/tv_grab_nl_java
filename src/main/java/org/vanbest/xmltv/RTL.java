package org.vanbest.xmltv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

public class RTL {

	static final String programme_url="http://www.rtl.nl/active/epg_data/dag_data/";
	static final String detail_url="http://www.rtl.nl/active/epg_data/uitzending_data/";
	
	int fetchErrors = 0;

	public List<Channel> getChannels() throws Exception {
		List<Channel> result = new ArrayList<Channel>(10);

		URL url = new URL(programme_url+"1");
		Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openStream());
		Element root = xml.getDocumentElement();
		String json = root.getTextContent();
		JSONObject o = JSONObject.fromObject( json );
		for( Object k: o.keySet()) {
			JSONArray j = (JSONArray) o.get(k);
			String id = k.toString().replaceAll("^Z", ""); // remove initial Z
			String name = (String) j.get(0);
			
			Channel c = Channel.getChannel(id, name);
			result.add(c);
		}

		return result;
	}
	
	protected String fetchURL(URL url) throws Exception {
		StringBuffer buf = new StringBuffer();
		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader( url.openStream()));
			String s;
			while ((s = reader.readLine()) != null) buf.append(s);
		} catch (IOException e) {
			fetchErrors++;
			throw new Exception("Error getting program data from url " + url, e);
		}
		return buf.toString();  
	}


	protected void fetchDay(int day) throws Exception {
		URL url = new URL(programme_url+day);
		String xmltext = fetchURL(url);
		System.out.println(xmltext);
		Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openStream());
		Element root = xml.getDocumentElement();
		Date date = new SimpleDateFormat("yyyy-MM-dd").parse(root.getAttribute("date"));
		System.out.println("date: " + date);
		String json = root.getTextContent();
		System.out.println("json: " + json);
		JSONObject o = JSONObject.fromObject( json );
		for( Object k: o.keySet()) {
			JSONArray j = (JSONArray) o.get(k);
			System.out.println(k.toString()+": "+j.toString());
			System.out.println("Channel name:" + j.get(0));
			for (int i=1; i<j.size() && i<3; i++) {
				JSONArray p = (JSONArray) j.get(i);
				String starttime = p.getString(0);
				String title = p.getString(1);
				String id = p.getString(2);
				String quark1 = p.getString(3);
				String quark2 = p.getString(4);
				System.out.println("    starttime: " + starttime);
				System.out.println("        title: " + title);
				System.out.println("           id: " + id);
				fetchDetail(id);
			}
		}
	}
	
	/*
	 * <?xml version="1.0" encoding="iso-8859-1" ?>
	 * <uitzending_data>
	 *   <uitzending_data_item>
	 *     <zendernr>5</zendernr>
	 *     <pgmsoort>Realityserie</pgmsoort>
	 *     <genre>Amusement</genre>
	 *     <bijvnwlanden></bijvnwlanden>
	 *     <ondertiteling></ondertiteling>
	 *     <begintijd>05:00</begintijd>
	 *     <titel>Marriage Under Construction</titel>
	 *     <site_path>0</site_path>
	 *     <wwwadres></wwwadres>
	 *     <presentatie></presentatie>
	 *     <omroep></omroep>
	 *     <eindtijd>06:00</eindtijd>
	 *     <inhoud></inhoud>
	 *     <tt_inhoud>Een jong stel wordt gevolgd bij het zoeken naar, en vervolgens verbouwen en inrichten van, hun eerste huis. Dit verloopt uiteraard niet zonder slag of stoot.</tt_inhoud>
	 *     <alginhoud>Een jong stel wordt gevolgd bij het zoeken naar, en vervolgens verbouwen en inrichten van, hun eerste huis. Dit verloopt uiteraard niet zonder slag of stoot.</alginhoud>
	 *     <afl_titel></afl_titel>
	 *     <kijkwijzer></kijkwijzer>
	 *   </uitzending_data_item>
	 * </uitzending_data>

	 */
	private void fetchDetail(String id) throws Exception {
		// TODO Auto-generated method stub
		URL url = new URL(detail_url+id);
		String xmltext = fetchURL(url);
		System.out.println(xmltext);
		Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openStream());
		Element root = xml.getDocumentElement();
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RTL rtl = new RTL();
		try {
			// rtl.fetchDay(1);
			List<Channel> channels = rtl.getChannels();
			System.out.println("Channels: " + channels);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
