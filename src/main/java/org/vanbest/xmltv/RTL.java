package org.vanbest.xmltv;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class RTL extends AbstractEPGSource implements EPGSource {

	private static final String programme_url = "http://www.rtl.nl/active/epg_data/dag_data/";
	private static final String detail_url = "http://www.rtl.nl/active/epg_data/uitzending_data/";
	private static final String icon_url = "http://www.rtl.nl/service/gids/components/vaste_componenten/";
	private static final int MAX_PROGRAMMES_PER_DAY = 9999;
	public static final String NAME = "rtl.nl";
	static Logger logger = Logger.getLogger(RTL.class);

	String[] xmlKeys = { "zendernr", "pgmsoort", "genre", "bijvnwlanden",
			"ondertiteling", "begintijd", "titel", "site_path", "wwwadres",
			"presentatie", "omroep", "eindtijd", "inhoud", "tt_inhoud",
			"alginhoud", "afl_titel", "kijkwijzer" };
	Map<String, Integer> xmlKeyMap = new HashMap<String, Integer>();

	static boolean debug = false;
	PrintWriter debugWriter;

	class RTLException extends Exception {
		public RTLException(String s) {
			super(s);
		}
	}

	class DateStatus {
		Date programDate;
		Calendar prevStartTime = null;
		final static int START_TIME = 1;
		final static int END_TIME = 2;

		public DateStatus(Date date) {
			reset(date);
		}

		public void reset(Date date) {
			this.programDate = date;
			this.prevStartTime = null;
		}
	}

	class DescStatus {
		String inhoud;
		String alginhoud;
		String tt_inhoud;
	}

	public RTL(int sourceId, Config config) {
		super(sourceId, config);
		if (debug) {
			for (int i = 0; i < xmlKeys.length; i++) {
				xmlKeyMap.put(xmlKeys[i], i);
			}
		}
	}

	public String getName() {
		return NAME;
	}

        private String xmlToString(Document doc) {
                try {
                  Transformer transformer = TransformerFactory.newInstance().newTransformer();
                  StringWriter stw = new StringWriter();  
                  transformer.transform(new DOMSource(doc), new StreamResult(stw));  
                  return stw.toString();
                } catch (TransformerConfigurationException e) {
                  logger.debug("Cannot convert XML Document to String, e");
                  return "";
                } catch (TransformerException e) {
                  logger.debug("Cannot convert XML Document to String, e");
                  return "";
                }
        }

	private Document fetchXML(URL url) throws Exception {
		Document xml = null;
		boolean done = false;
		logger.debug("Fetching XML from URL "+url);
		for (int count = 0; !done; count++) {
			Thread.sleep(config.niceMilliseconds*(1<<count));
           		try {
                          	xml = DocumentBuilderFactory.newInstance()
                                  	.newDocumentBuilder().parse(url.openStream());
                          	done = true;
                  	} catch (Exception e) {
        				if (!config.quiet) {
        					logger.warn("Error fetching from url " + url + ", count="
        							+ count);
        				}
        				if (count >= MAX_FETCH_TRIES) {
        					stats.fetchErrors++;
        					logger.debug("Error getting data from url", e);
        					throw new Exception("Error getting data from url "
        							+ url, e);
        				}
         	       }
                }
		logger.debug(xmlToString(xml));
                return xml;
        }

	public List<Channel> getChannels() {
		List<Channel> result = new ArrayList<Channel>(10);

		URL url = null;
		try {
			url = new URL(programme_url + "1");
		} catch (MalformedURLException e) {
			logger.error("Exception creating RTL channel list url", e);
		}
		Document xml = null;
		try {
			xml = fetchXML(url);
		} catch (Exception e) {
			logger.error("Exception reading RTL channel listing from " + url
					+ " and transforming to XML", e);
                        return result;
		}
		Element root = xml.getDocumentElement();
		String json = root.getTextContent();
		JSONObject o = JSONObject.fromObject(json);
		for (Object k : o.keySet()) {
			JSONArray j = (JSONArray) o.get(k);
			String id = genericChannelId(k.toString());
			String name = (String) j.get(0);
                        String icon = icon_url + id + ".gif";
			Channel c = Channel.getChannel(getId(), id, name);
                        c.addIcon(icon);
			result.add(c);
		}

		Collections.sort(result, new Comparator<Channel>() {
			public int compare(Channel o1, Channel o2) {
				if (o1.source == o2.source) {
					int c1 = Integer.parseInt(o1.id);
					int c2 = Integer.parseInt(o2.id);
					return (c1 == c2 ? 0 : ((c1 < c2) ? -1 : 1));
				} else {
					return o1.source < o2.source ? -1 : 1;
				}
			}
		});
		return result;
	}

	private String genericChannelId(String jsonid) {
		return jsonid.replaceAll("^Z", ""); // remove initial Z
	}

	/*
	 * <?xml version="1.0" encoding="iso-8859-1" ?> <uitzending_data>
	 * <uitzending_data_item> <zendernr>5</zendernr>
	 * <pgmsoort>Realityserie</pgmsoort> <genre>Amusement</genre>
	 * <bijvnwlanden></bijvnwlanden> <ondertiteling></ondertiteling>
	 * <begintijd>05:00</begintijd> <titel>Marriage Under Construction</titel>
	 * <site_path>0</site_path> <wwwadres></wwwadres>
	 * <presentatie></presentatie> <omroep></omroep> <eindtijd>06:00</eindtijd>
	 * <inhoud></inhoud> <tt_inhoud>Een jong stel wordt gevolgd bij het zoeken
	 * naar, en vervolgens verbouwen en inrichten van, hun eerste huis. Dit
	 * verloopt uiteraard niet zonder slag of stoot.</tt_inhoud> <alginhoud>Een
	 * jong stel wordt gevolgd bij het zoeken naar, en vervolgens verbouwen en
	 * inrichten van, hun eerste huis. Dit verloopt uiteraard niet zonder slag
	 * of stoot.</alginhoud> <afl_titel></afl_titel> <kijkwijzer></kijkwijzer>
	 * </uitzending_data_item> </uitzending_data>
	 */
	private void fetchDetail(Programme prog, DateStatus dateStatus, String id)
			throws Exception {
		URL url = detailUrl(id);
		Document xml = fetchXML(url);
		Element root = xml.getDocumentElement();
		if (root.hasAttributes()) {
			logger.warn("Unknown attributes for RTL detail root node");
		}
		NodeList nodes = root.getChildNodes();
		DescStatus descStatus = new DescStatus();
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			if (!n.getNodeName().equals("uitzending_data_item")) {
				logger.warn("Ignoring RTL detail, tag " + n.getNodeName()
						+ ", full xml:");
				logger.debug(xmlToString(xml));
				continue;
			}
			// we have a uitzending_data_item node
			NodeList subnodes = n.getChildNodes();
			String[] result = new String[xmlKeys.length];
			for (int j = 0; j < subnodes.getLength(); j++) {
				try {
					if (debug) {
						Node sub = subnodes.item(j);
						String key = ((Element) sub).getTagName();
						int index = xmlKeyMap.get(key);
						String value = "\""
								+ sub.getTextContent().replaceAll("\\s", " ")
								+ "\"";
						result[index] = value;
					}
					handleNode(prog, dateStatus, descStatus, subnodes.item(j));
				} catch (RTLException e) {
					logger.debug(xmlToString(xml), e);
					continue;
				}
			}
			if (debug) {
				for (int k = 0; k < result.length; k++) {
					debugWriter.print(result[k]);
					debugWriter.print(",");
				}
				debugWriter.println();
			}
		}
		StringBuilder description = new StringBuilder();
		if (descStatus.alginhoud != null)
			description.append(descStatus.alginhoud);
		if (descStatus.inhoud != null) {
			if (description.length() != 0) {
				description.append("<p>");
			}
			description.append(descStatus.inhoud);
		}
		if (description.length() == 0 && descStatus.tt_inhoud != null) {
			// only use tt_inhoud if the other two are both empty, since it is
			// almost
			// always a summary of those fields and others such as <presenter>
			description.append(descStatus.tt_inhoud);
		}
		prog.addDescription(description.toString());
	}

	private void handleNode(Programme prog, DateStatus dateStatus,
			DescStatus descStatus, Node n) throws RTLException, DOMException,
			SQLException {
		if (n.getNodeType() != Node.ELEMENT_NODE) {
			throw new RTLException("Ignoring non-element node "
					+ n.getNodeName());
		}
		if (n.hasAttributes()) {
			throw new RTLException("Unknown attributes for RTL detail node "
					+ n.getNodeName());
		}
		if (n.hasChildNodes()) {
			NodeList list = n.getChildNodes();
			for (int i = 0; i < list.getLength(); i++) {
				if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
					throw new RTLException("RTL detail node " + n.getNodeName()
							+ " has unexpected child element "
							+ list.item(i).getNodeName());
				}
			}
		}
		Element e = (Element) n;
		String tag = e.getTagName();

		if (e.getTextContent().isEmpty()) {
			return;
		}
		if (tag.equals("genre")) {
			prog.addCategory(config.translateCategory(e.getTextContent()));
		} else if (tag.equals("eindtijd")) {
			prog.endTime = parseTime(e.getTextContent(), dateStatus,
					DateStatus.END_TIME);
		} else if (tag.equals("omroep")) {
		} else if (tag.equals("kijkwijzer")) {
			logger.trace(prog.toString());
			logger.trace("Kijkwijzer: \"" + e.getTextContent() + "\"");
			String kijkwijzer = e.getTextContent();
			List<String> list = parseKijkwijzer(kijkwijzer);
			if (config.joinKijkwijzerRatings) {
				// mythtv doesn't understand multiple <rating> tags
				prog.addRating("kijkwijzer", StringUtils.join(list, ","));
			} else {
				for (String rating : list) {
					prog.addRating("kijkwijzer", rating);
				}
			}
			logger.trace("Kijkwijzer: \"" + StringUtils.join(list, ",") + "\"");
			// TODO add kijkwijzer icons?
		} else if (tag.equals("presentatie")) {
			// A; A en B; A, B, C en D
			String[] presentatoren = e.getTextContent().split(", | en ");
			for (String pres : presentatoren) {
				prog.addPresenter(pres);
			}
		} else if (tag.equals("wwwadres")) {
			prog.addUrl(e.getTextContent());
		} else if (tag.equals("alginhoud")) {
			descStatus.alginhoud = e.getTextContent();
		} else if (tag.equals("inhoud")) {
			descStatus.inhoud = e.getTextContent();
		} else if (tag.equals("tt_inhoud")) {
			descStatus.tt_inhoud = e.getTextContent();
			// ignore, is summary of other fields
		} else if (tag.equals("zendernr")) {
		} else if (tag.equals("titel")) {
		} else if (tag.equals("bijvnwlanden")) {
		} else if (tag.equals("afl_titel")) {
			prog.addSecondaryTitle(e.getTextContent());
		} else if (tag.equals("site_path")) {
		} else if (tag.equals("ondertiteling")) {
			if (e.getTextContent().equals("J")) {
				prog.addSubtitle("teletext");
			} else {
				throw new RTLException("Ignoring unknown value \""
						+ n.getTextContent() + "\" for tag ondertiteling");
			}
		} else if (tag.equals("begintijd")) {
		} else if (tag.equals("pgmsoort")) {
		} else {
			throw new RTLException("Ignoring unknown tag " + n.getNodeName()
					+ ", content: \"" + e.getTextContent() + "\"");
		}
		// prog.endTime = parseTime(date, root.)
	}

	@Override
	public List<Programme> getProgrammes(List<Channel> channels, int day)
			throws Exception {
		List<Programme> result = new LinkedList<Programme>();
		Map<String, Channel> channelMap = new HashMap<String, Channel>();
		for (Channel c : channels) {
			if (c.enabled && c.source == getId())
				channelMap.put(c.id, c);
		}
		URL url = programmeUrl(day);
		Document xml = fetchXML(url);
		Element root = xml.getDocumentElement();
		Date date = new SimpleDateFormat("yyyy-MM-dd").parse(root
				.getAttribute("date"));
		DateStatus dateStatus = new DateStatus(date);
		// System.out.println("date: " + date);
		String json = root.getTextContent();
		// System.out.println("json: " + json);
		JSONObject o = JSONObject.fromObject(json);
		String prevChannel = null;
		for (Object k : o.keySet()) {
			String id = genericChannelId(k.toString());
			if (!channelMap.containsKey(id)) {
				// if (!config.quiet)
				// System.out.println("Skipping programmes for channel " + id);
				continue;
			}
			if (!id.equals(prevChannel)) {
				dateStatus.reset(date);
				prevChannel = id;
			}
			JSONArray j = (JSONArray) o.get(k);
			// System.out.println(k.toString()+": "+j.toString());
			// System.out.println("Channel name:" + j.get(0));
			for (int i = 1; i < j.size() && i < MAX_PROGRAMMES_PER_DAY; i++) {
				JSONArray p = (JSONArray) j.get(i);
				String starttime = p.getString(0);
				String title = p.getString(1);
				String programme_id = p.getString(2);
				String genre_id = p.getString(3); // 1 = amusement, etc
				String quark2 = p.getString(4); // 0 of 1, movie flag?
				if (debug)
					debugWriter.print("\"" + id + "\",\"" + starttime + "\",\""
							+ title + "\",\"" + genre_id + "\",\"" + quark2
							+ "\",");
				Programme prog = cache.get(getId(), programme_id);
				if (prog == null) {
					stats.cacheMisses++;
					prog = new Programme();
					prog.addTitle(title);
					prog.startTime = parseTime(starttime, dateStatus,
							DateStatus.START_TIME);
					prog.channel = channelMap.get(id).getXmltvChannelId();
					if (config.fetchDetails) {
						fetchDetail(prog, dateStatus, programme_id);
					}
					cache.put(getId(), programme_id, prog);
				} else {
					stats.cacheHits++;
				}
				result.add(prog);
			}
		}
		return result;
	}

	// Assumption: programmes are more-or-less in ascending order by start time
	private Date parseTime(String time, DateStatus status, int mode) {
		Calendar result = Calendar.getInstance();
		result.setTime(status.programDate);
		String[] parts = time.split(":");
		if (parts.length != 2) {
			if (!config.quiet)
				logger.debug("Wrong time format " + time);
			// ignore
		}
		result.set(Calendar.HOUR_OF_DAY, Integer.parseInt(parts[0]));
		result.set(Calendar.MINUTE, Integer.parseInt(parts[1]));
		Calendar prev = status.prevStartTime;
		// Check if the start time of a new program is at most one hour before
		// the start time of
		// the previous one. End time of a program should be at or after the
		// start time of the
		// program. Else, assume it's on the next day.
		if (prev != null) {
			if (mode == DateStatus.START_TIME) {
				prev.add(Calendar.HOUR_OF_DAY, -1);
			}
			if (result.before(prev)) {
				result.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
		if (mode == DateStatus.START_TIME) {
			status.prevStartTime = result;
		}
		return result.getTime();
	}

	private static URL programmeUrl(int day) throws MalformedURLException {
		return new URL(programme_url + day);
	}

	private static URL detailUrl(String id) throws Exception {
		return new URL(detail_url + id);
	}

	/**
	 * @param args
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		debug = true;
		Logger.getRootLogger().setLevel(Level.TRACE);

		Config config = Config.getDefaultConfig();
		config.niceMilliseconds = 50;
		RTL rtl = new RTL(2, config);
		if (debug) {
			rtl.cache.clear();
			logger.info("Writing CSV to rtl.csv");
			rtl.debugWriter = new PrintWriter(new BufferedOutputStream(
					new FileOutputStream("rtl.csv")));
			rtl.debugWriter
					.print("\"zender\",\"starttime\",\"title\",\"quark1\",\"quark2\",");
			for (int k = 0; k < rtl.xmlKeys.length; k++) {
				rtl.debugWriter.print(rtl.xmlKeys[k]);
				rtl.debugWriter.print(",");
			}
			rtl.debugWriter.println();
		}

		try {
			List<Channel> channels = rtl.getChannels();
			logger.info("Channels: " + channels);
			XMLStreamWriter writer = XMLOutputFactory.newInstance()
					.createXMLStreamWriter(new FileWriter("rtl.xml"));
			writer.writeStartDocument();
			writer.writeCharacters("\n");
			writer.writeDTD("<!DOCTYPE tv SYSTEM \"xmltv.dtd\">");
			writer.writeCharacters("\n");
			writer.writeStartElement("tv");
			for (Channel c : channels) {
				c.serialize(writer, true);
			}
			writer.flush();
			// List<Programme> programmes =
			// rtl.getProgrammes(channels.subList(6, 9), 0);
			for (int day = 0; day < 10; day++) {
				List<Programme> programmes = rtl.getProgrammes(channels, day);
				for (Programme p : programmes) {
					p.serialize(writer);
				}
			}
			writer.writeEndElement();
			writer.writeEndDocument();
			writer.flush();
			if (!config.quiet) {
				EPGSource.Stats stats = rtl.getStats();
				logger.info("Number of programmes from cache: "
						+ stats.cacheHits);
				logger.info("Number of programmes fetched: "
						+ stats.cacheMisses);
				logger.info("Number of fetch errors: " + stats.fetchErrors);
			}
			if (debug) {
				rtl.debugWriter.flush();
				rtl.debugWriter.close();
			}
			rtl.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.debug("Exception in RTL.main()", e);
		}
	}

}
