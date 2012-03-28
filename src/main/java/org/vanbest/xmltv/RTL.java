package org.vanbest.xmltv;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sf.json.JSON;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.vanbest.xmltv.EPGSource.Stats;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class RTL extends AbstractEPGSource implements EPGSource  {

	private static final String programme_url="http://www.rtl.nl/active/epg_data/dag_data/";
	private static final String detail_url="http://www.rtl.nl/active/epg_data/uitzending_data/";
	private static final String icon_url="http://www.rtl.nl/service/gids/components/vaste_componenten/";
	private static final String xmltv_channel_suffix = ".rtl.nl";
	private static final int MAX_PROGRAMMES_PER_DAY = 99999;
	
	private Connection db;
	
	String[] xmlKeys = {"zendernr", "pgmsoort", "genre", "bijvnwlanden", "ondertiteling", "begintijd", "titel", 
			"site_path", "wwwadres", "presentatie", "omroep", "eindtijd", "inhoud", "tt_inhoud", "alginhoud", "afl_titel", "kijkwijzer" };
		
	Map<String,Integer> xmlKeyMap = new HashMap<String,Integer>();
	
	class RTLException extends Exception {
		public RTLException(String s) {
			super(s);
		}
	}
	
	public RTL(Config config) {
		super(config);
		try {
			db = DriverManager.getConnection("jdbc:hsqldb:file:testdb", "SA", "");
			Statement stat = db.createStatement();
			StringBuilder s = new StringBuilder();
			s.append("CREATE TABLE IF NOT EXISTS prog (id VARCHAR(32) primary key, ");
			int i=0;
			for( String key: xmlKeys) {
				if(i>0) s.append(", ");
				xmlKeyMap.put(key, i+1);
				s.append(key);
				s.append(" VARCHAR(4096)");
				i++;
			}
			s.append(");");
			System.out.println(s);
			stat.execute(s.toString());
			stat.execute("TRUNCATE TABLE prog");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(1);
			db = null;
		}
	}

	public List<Channel> getChannels() {
		List<Channel> result = new ArrayList<Channel>(10);

		URL url = null;
		try {
			url = new URL(programme_url+"1");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Document xml = null;
		try {
			xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openStream());
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Element root = xml.getDocumentElement();
		String json = root.getTextContent();
		JSONObject o = JSONObject.fromObject( json );
		for( Object k: o.keySet()) {
			JSONArray j = (JSONArray) o.get(k);
			String id = genericChannelId(k.toString());
			String name = (String) j.get(0);
			String icon = icon_url+id+".gif";
			
			Channel c = Channel.getChannel(id, name, icon);
			result.add(c);
		}

		return result;
	}
	
	private String genericChannelId(String jsonid) {
		return jsonid.replaceAll("^Z", "")+xmltv_channel_suffix; // remove initial Z
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
	private void fetchDetail(Programme prog, Date date, String id) throws Exception {
		URL url = detailUrl(id);
		Thread.sleep(config.niceMilliseconds);
		Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openStream());
		Element root = xml.getDocumentElement();
		if (root.hasAttributes()) {
			System.out.println("Unknown attributes for RTL detail root node");
		}
		StringBuilder sql = new StringBuilder("INSERT INTO prog (id");
		StringBuilder sql2= new StringBuilder(") values (?");
		for(String key:xmlKeys) {
			sql.append(",");
			sql.append(key);
			sql2.append(",");
			sql2.append("?");
		}
		sql.append(sql2);
		sql.append(");");
		// System.out.println(sql.toString());
		PreparedStatement stat = db.prepareStatement(sql.toString());
		stat.setString(1, id);
		for(String key:xmlKeys) {
			
		}
		NodeList nodes = root.getChildNodes();
		for( int i=0; i<nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if (!n.getNodeName().equals("uitzending_data_item")) {
				System.out.println("Ignoring RTL detail, tag " + n.getNodeName() +", full xml:");
				Transformer t = TransformerFactory.newInstance().newTransformer();
				t.transform(new DOMSource(xml),new StreamResult(System.out));
				System.out.println();
				continue;
			}
			// we have a uitzending_data_item node
			NodeList subnodes = n.getChildNodes();
			for( int j=0; j<subnodes.getLength(); j++) {
				try {
					handleNode(prog, date, stat, subnodes.item(j));
				} catch (RTLException e) {
					System.out.println(e.getMessage());
					Transformer t = TransformerFactory.newInstance().newTransformer();
					t.transform(new DOMSource(xml),new StreamResult(System.out));
					System.out.println();
					continue;
				}
			}
			System.out.println(stat.toString());
			stat.execute();
		}
	}

	
	private void handleNode(Programme prog, Date date, PreparedStatement stat, Node n) throws RTLException, DOMException, SQLException {
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			throw new RTLException("Ignoring non-element node " + n.getNodeName());
		}
		if (n.hasAttributes()) {
			throw new RTLException("Unknown attributes for RTL detail node " + n.getNodeName());
		}
		if (n.hasChildNodes()) {
			NodeList list = n.getChildNodes();
			for( int i=0; i<list.getLength(); i++) {
				if(list.item(i).getNodeType() == Node.ELEMENT_NODE) {
					throw new RTLException("RTL detail node " + n.getNodeName() + " has unexpected child element " + list.item(i).getNodeName());
				}
			}
		}
		Element e = (Element)n;
		stat.setString(xmlKeyMap.get(e.getTagName())+1, e.getTextContent());
		switch (e.getTagName()) {
		case "genre":
			prog.addCategory(config.translateCategory(e.getTextContent()));
			break;
		case "eindtijd":
			prog.endTime = parseTime(date, e.getTextContent());
			break;
		case "omroep":
		case "kijkwijzer":
		case "presentatie":
		case "wwwadres":
		case "alginhoud":
		case "inhoud":
		case "tt_inhoud":
		case "zendernr":
		case "titel":
		case "bijvnwlanden":
		case "afl_titel":
		case "site_path":
		case "ondertiteling":
		case "begintijd":
		case "pgmsoort":
			break;
		default:
			throw new RTLException("Ignoring unknown tag " + n.getNodeName() + ", content: \"" + e.getTextContent() + "\"");
		}
		//prog.endTime = parseTime(date, root.)
	}

	public List<Programme> getProgrammes1(List<Channel> channels, int day,
			boolean fetchDetails) throws Exception {
		List<Programme> result = new LinkedList<Programme>();
		Map<String,Channel> channelMap = new HashMap<String,Channel>();
		for(Channel c: channels) {
			if (c.enabled) channelMap.put(c.id, c);
		}
		URL url = programmeUrl(day);
		//String xmltext = fetchURL(url);
		//System.out.println(xmltext);
		Thread.sleep(config.niceMilliseconds);
		Document xml = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openStream());
		Element root = xml.getDocumentElement();
		Date date = new SimpleDateFormat("yyyy-MM-dd").parse(root.getAttribute("date"));
		System.out.println("date: " + date);
		String json = root.getTextContent();
		System.out.println("json: " + json);
		JSONObject o = JSONObject.fromObject( json );
		for( Object k: o.keySet()) {
			String id = genericChannelId(k.toString());
			if(!channelMap.containsKey(id)) {
				System.out.println("Skipping programmes for channel " + id);
				continue;
			}
			JSONArray j = (JSONArray) o.get(k);
			System.out.println(k.toString()+": "+j.toString());
			//System.out.println("Channel name:" + j.get(0));
			for (int i=1; i<j.size() && i<MAX_PROGRAMMES_PER_DAY; i++) {
				JSONArray p = (JSONArray) j.get(i);
				String starttime = p.getString(0);
				String title = p.getString(1);
				String programme_id = p.getString(2);
				String quark1 = p.getString(3);
				String quark2 = p.getString(4);
				Programme prog = new Programme();
				prog.addTitle(title);
				Date start = parseTime(date, starttime);
				prog.startTime = start;
				prog.channel = channelMap.get(id);
				fetchDetail(prog, date, programme_id);
				result.add(prog);
			}
		}
		return result;
	}

	private Date parseTime(Date date, String time) {
		Calendar result = Calendar.getInstance();
		result.setTime(date);
		String[] parts = time.split(":");
		if(parts.length != 2) {
			
		}
		int hour = Integer.parseInt(parts[0]);
		if (hour<5) {
			result.add(Calendar.DAY_OF_MONTH, 1); // early tomorrow morning
		}
		result.set(Calendar.HOUR_OF_DAY, hour);
		result.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
		return result.getTime();
	}

	private static URL programmeUrl(int day) throws MalformedURLException {
		return new URL(programme_url+day);
	}
	
	private static URL detailUrl(String id) throws Exception {
		return new URL(detail_url+id);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Config config = Config.getDefaultConfig();
		System.exit(0);
		RTL rtl = new RTL(config);
		try {
			List<Channel> channels = rtl.getChannels();
			System.out.println("Channels: " + channels);
			XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
			
			writer.writeStartDocument();
			writer.writeCharacters("\n");
			writer.writeDTD("<!DOCTYPE tv SYSTEM \"xmltv.dtd\">");
			writer.writeCharacters("\n");
			writer.writeStartElement("tv");
			for(Channel c: channels) {c.serialize(writer);}
			//List<Programme> programmes = rtl.getProgrammes1(channels.subList(0, 13), 0, true);
			List<Programme> programmes = rtl.getProgrammes1(channels, 0, true);
			for(Programme p: programmes) {p.serialize(writer);}
			writer.writeEndElement();
			writer.writeEndDocument();
			writer.flush();
			rtl.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public Set<TvGidsProgramme> getProgrammes(List<Channel> channels, int day,
			boolean fetchDetails) throws Exception {
		// TODO Refactor EPGSource to return Programme instead of TvGidsProgramme
		return null;
	}


}
