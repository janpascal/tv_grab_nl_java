package org.vanbest.xmltv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class RTL {

	static final String programme_url="http://www.rtl.nl/active/epg_data/dag_data/";
	static final String detail_url="http://www.rtl.nl/active/epg_data/uitzending_data/";
	
	int fetchErrors = 0;


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


	protected void run() throws Exception {
		URL url = new URL(programme_url+"1");
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
			for (int i=1; i<j.size(); i++) {
				JSONArray p = (JSONArray) j.get(i);
				String starttime = p.getString(0);
				String title = p.getString(1);
				String id = p.getString(2);
				String quark1 = p.getString(3);
				String quark2 = p.getString(4);
				System.out.println("    starttime: " + starttime);
				
			}
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RTL rtl = new RTL();
		try {
			rtl.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
