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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    private static final String programme_base_url = "http://www.rtl.nl/system/s4m/tvguide/guide_for_one_day.xml";

    private static final String icon_url = "http://www.rtl.nl/service/gids/components/vaste_componenten/";
    private static final int MAX_PROGRAMMES_PER_DAY = 9999;
    public static final String NAME = "rtl.nl";
    static Logger logger = Logger.getLogger(RTL.class);

    static boolean debug = false;
    PrintWriter debugWriter;

    private Map<String,JSONObject> abstracts = new HashMap<String,JSONObject>();
    private Map<String,JSONObject> episodes = new HashMap<String,JSONObject>();
    private Map<String,JSONObject> seasons = new HashMap<String,JSONObject>();

    class RTLException extends Exception {
        public RTLException(String s) {
            super(s);
        }
    }

    public RTL(int sourceId, Config config) {
        super(sourceId, config);
    }

    public String getName() {
        return NAME;
    }

    // http://www.rtl.nl/system/s4m/tvguide/guide_for_one_day.xml?output=json&days_ahead=7&days_back=1&station=ALL
    // Note: station parameter is either ALL or one of RTL4,RTL5,RTL7,RTL8
    private static URL programmeUrl(int days_back, int days_ahead) throws MalformedURLException {
        return new URL(programme_base_url + "?output=json&days_ahead="+days_ahead+"&days_back="+days_back+"&station=ALL");
    }

    public List<Channel> getChannels() {
        Map<String,Channel> channels = new HashMap<String,Channel>(5);
        JSONObject o;
        try {
            URL url = programmeUrl(0, 0);
            o = fetchJSON(url);
        } catch (Exception e) {
            logger.error("Error fetching channels for source "+getName(), e);
            return new ArrayList<Channel>();
        }
        JSONArray schedules = o.getJSONArray("schedule");
        for (int i=0; i<schedules.size(); i++) {
            JSONObject schedule = schedules.getJSONObject(i);
            String channel = schedule.getString("station");
            if(!channels.containsKey(channel)) {
                Channel c = Channel.getChannel(getId(), channel, channel);
                // TODO: channel icon
                channels.put(channel, c);
            }
        }
        List<Channel> result = new ArrayList<Channel>(10);
        return new ArrayList<Channel>(channels.values());
    }
    /* 
    * (AL|6|9|12|16){0,1}[GASTHD]+
    * AL = alle leeftijden
    * A Angst
    * G Geweld
    * S Sex
    * T Grof Taalgebruik
    * H drugs/alcoHol
    * D?  Discriminatie
    *
    * Voorkomende combinaties:
    *   G
    *   GA
    *   GAT
    *   GST
    *   GT
    *   A
    *   AH
    *   AL
    *   ALT
    *   AT
    *   HT
    *   DT
    *   ST
    */

    private Pattern kijkwijzerPattern = Pattern.compile("^(AL|\\d+)(.*)");
    @Override
    List<String> parseKijkwijzer(String s) {
        logger.trace("Kijkwijzer: " +s);
	List<String> result = new ArrayList<String>();
        Matcher m = kijkwijzerPattern.matcher(s);
        if (m.matches()) {
            String l = m.group(1);
            if(l.equals("AL")) {
                result.add("Voor alle leeftijden");
            } else {
                int leeftijd = Integer.parseInt(l);
                result.add("Afgeraden voor kinderen jonger dan "+leeftijd+" jaar");
            }
            String cat = m.group(2).toLowerCase();
            for (int i = 0; i < cat.length(); i++) {
	        char c = cat.charAt(i);
                String tekst = kijkwijzerCategorie(c);
                if(tekst!=null) {
                    result.add(tekst);
                } else {
                    logger.warn("Unknown RTL Kijkwijzer combination \""+s+"\"");
                }
            }
        } else {
            logger.warn("Unknown RTL Kijkwijzer combination \""+s+"\"");
        }
        return result;
    }

    Programme createProgramme(JSONObject schedule, Map<String,Channel> channelMap) {
        Programme prog = new Programme();
        prog.startTime = new Date(1000L*schedule.getLong("unixtime"));
        prog.channel = channelMap.get(schedule.getString("station")).getXmltvChannelId();
        if(schedule.getBoolean("rerun")) prog.setPreviouslyShown();

        String abstractKey = schedule.optString("abstract_key");
        if(abstractKey!=null) {
            JSONObject abstrac = abstracts.get(abstractKey);
            prog.addTitle(abstrac.getString("name"));
            logger.debug("\""+prog.getFirstTitle()+"\"");
        }
        String episodeKey = schedule.optString("episode_key");
        if(episodeKey!=null) {
            JSONObject episode = episodes.get(episodeKey);
            String s = episode.optString("name");
            if(s!=null && !s.isEmpty()) prog.addSecondaryTitle(s);
            s = episode.optString("episode_number");
            if(s!=null && !s.isEmpty()) prog.addEpisode(s, "onscreen");
            s = episode.optString("synopsis");
            if(s!=null && !s.isEmpty()) prog.addDescription(s);
            String kijkwijzer = episode.optString("nicam");
            if (kijkwijzer != null && !kijkwijzer.isEmpty()) {
                List<String> list = parseKijkwijzer(kijkwijzer);
                if (config.joinKijkwijzerRatings) {
                    // mythtv doesn't understand multiple <rating> tags
                    prog.addRating("kijkwijzer", StringUtils.join(list, ","));
                } else {
                    for (String rating : list) {
                        prog.addRating("kijkwijzer", rating);
                    }
                }
            }
        }
        String seasonKey = schedule.optString("season_key");
        if(seasonKey!=null) {
            JSONObject season = seasons.get(seasonKey);
            // ignored
            // season_number
            // name
        }
        return prog;
    }

    void parseLibrary(JSONArray library) {
        if(library.size()!=1) {
            logger.warn("RTL library array size is not equals to one!");
        }
        JSONObject lib = library.getJSONObject(0);
        // library: abstracts
        JSONArray abstractArray = lib.getJSONArray("abstracts");
        for(int i=0; i<abstractArray.size(); i++) {
            JSONObject abstrac = abstractArray.getJSONObject(i);
            abstracts.put(abstrac.getString("abstract_key"), abstrac);
        }
        // library: seasons
        JSONArray seasonArray = lib.getJSONArray("seasons");
        for(int i=0; i<seasonArray.size(); i++) {
            JSONObject season = seasonArray.getJSONObject(i);
            seasons.put(season.getString("season_key"), season);
        }
        // library: episodes
        JSONArray episodeArray = lib.getJSONArray("episodes");
        for(int i=0; i<episodeArray.size(); i++) {
            JSONObject episode = episodeArray.getJSONObject(i);
            episodes.put(episode.getString("episode_key"), episode);
        }
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
        URL url = programmeUrl(0, day);
        JSONObject json = fetchJSON(url);

        // First parse the library
        JSONArray library = json.getJSONArray("library");
        parseLibrary(library);

        // Then the schedules
        JSONArray schedules = json.getJSONArray("schedule");
        for (int i=0; i<schedules.size(); i++) {
            JSONObject schedule = schedules.getJSONObject(i);

            // Skip programme if station not in channel list
            String station=schedule.getString("station");
            if (!channelMap.containsKey(station)) {
                if (!config.quiet)
                   logger.info("Skipping programmes for channel " + station);
                continue;
            }
            result.add(createProgramme(schedule, channelMap));
        }

        return result;
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
                    /*
                    for (int k = 0; k < rtl.xmlKeys.length; k++) {
                            rtl.debugWriter.print(rtl.xmlKeys[k]);
                            rtl.debugWriter.print(",");
                    }
                    */
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
