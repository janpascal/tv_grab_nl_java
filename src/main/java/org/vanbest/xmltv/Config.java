package org.vanbest.xmltv;

/*
 Copyright (c) 2012,2013 Jan-Pascal van Best <janpascal@vanbest.org>

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class Config {
	// constants
	private final static int CURRENT_FILE_FORMAT = 6;

	// in config file
	public int niceMilliseconds;
	public List<Channel> channels;
	public Map<String, String> cattrans;
	public String cacheDbHandle;
	public String cacheDbUser;
	public String cacheDbPassword;
        public boolean fetchLogos;

	// not stored (yet)
	public boolean joinKijkwijzerRatings = true;
	boolean fetchDetails = true;

	// command-line options
	boolean quiet = false;
        boolean configYes = false;

	String project_version;
	String build_time;
	static Logger logger = Logger.getLogger(Config.class);

	private Config() {
		Properties configProp = new Properties();
		InputStream in = ClassLoader
				.getSystemResourceAsStream("tv_grab_nl_java.properties");
		try {
			configProp.load(in);
		} catch (IOException e) {
			logger.warn("Error reading application properties from tv_grab_nl_java.properties resource");
			logger.debug("stack trace: ", e);
		}
		project_version = configProp.getProperty("project.version");
		build_time = configProp.getProperty("build.time");
	}

	public static Config getDefaultConfig() {
		Config result = new Config();
		result.channels = new ArrayList<Channel>();
		result.cattrans = getDefaultCattrans();
		result.niceMilliseconds = 500;
		String cachefile = FileUtils.getFile(FileUtils.getUserDirectory(),
				".xmltv", "tv_grab_nl_java.cache").getPath();
		result.setCacheFile(cachefile);
		result.cacheDbUser = "SA";
		result.cacheDbPassword = "";
		result.fetchLogos= true;
		return result;
	}

	public Map<String, String> getCategories() {
		return cattrans;
	}

	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}

	public static File defaultCacheFile() {
		return FileUtils.getFile(FileUtils.getUserDirectory(), ".xmltv",
				"tv_grab_nl_java.cache");
	}

	public String translateCategory(String category) {
		if (!cattrans.containsKey(category.toLowerCase())) {
			return category;
		}
		return cattrans.get(category.toLowerCase());
	}

        /*
        MythWeb categories:
          Action         Adult          
          Animals        Art_Music      
          Business       Children       
          Comedy         Crime_Mystery  
          Documentary    Drama          
          Educational    Food           
          Game           Health_Medical 
          History        HowTo          
          Horror         Misc           
          News           Reality        
          Romance        Science_Nature 
          SciFi_Fantasy  Shopping       
          Soaps          Spiritual      
          Sports         Talk           
          Travel         War            
          Western        Movie
          Unknown        
        */

	static private Map<String, String> getDefaultCattrans() {
		Map<String, String> result = new HashMap<String, String>();
		result.put("amusement", "Unknown");
		result.put("comedy", "Comedy");
		result.put("documentaire", "Documentary");
		result.put("educatief", "Educational");
		result.put("erotiek", "Adult");
		result.put("film", "Movie");
		result.put("muziek", "Art/Music");
		result.put("informatief", "Educational");
		result.put("jeugd", "Children");
		result.put("kunst/cultuur", "Art/Music");
		result.put("misdaad", "Crime/Mystery");
		result.put("muziek", "Music");
		result.put("natuur", "Science/Nature");
		result.put("nieuws/actualiteiten", "News");
		result.put("overige", "Unknown");
		result.put("religieus", "Religion");
		result.put("serie/soap", "Drama");
		result.put("sport", "Sports");
		result.put("theater", "Art/Music");
		result.put("wetenschap", "Science/Nature");
		result.put("news", "News");
		return result;
	}

	public void writeConfig(File configFile) throws IOException {
                if (configFile.getParentFile()!=null) {
                    FileUtils.forceMkdir(configFile.getParentFile());
                }
		PrintWriter out = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(configFile)));
		out.println("config-file-format: " + CURRENT_FILE_FORMAT);
		out.println("cache-db-handle: " + escape(cacheDbHandle));
		out.println("cache-db-user: " + escape(cacheDbUser));
		out.println("cache-db-password: " + escape(cacheDbPassword));
		out.println("fetch-channel-logos: " + (fetchLogos?"yes":"no"));
		out.println("nice-time-milliseconds: " + niceMilliseconds);
		for (Channel c : channels) {
                        c.writeConfig(out);
		}
		for (Map.Entry<String, String> entry : cattrans.entrySet()) {
			out.println("category: " + escape(entry.getKey()) + ": "
					+ escape(entry.getValue()));
		}
		out.close();
	}

	public static String escape(String s) {
		return "\"" + s.replace("\\", "\\\\").replaceAll("\\\"", "\\\\\"")
				+ "\"";
	}

	public static List<String> splitLine(String s) {
		List<String> parts = new ArrayList<String>(5);
		int pos = 0;
		while (true) {
			// Find first colon outside quotes
			boolean quoted = false;
			int quoteStart = -1;
			StringBuffer buf = new StringBuffer();
			for (; pos < s.length(); pos++) {
				if (s.charAt(pos) == '"') {
					if (quoted) {
						// System.out.println(s.substring(quoteStart, pos));
						buf.append(s.substring(quoteStart, pos)
								.replaceAll("\\\\\"", "\\\"")
								.replaceAll("\\\\\\\\", "\\\\"));
					} else {
						quoteStart = pos + 1;
					}
					quoted = !quoted;
					continue;
				}
				if (s.charAt(pos) == '\\')
					pos++;
				if (quoted)
					continue;
				if (s.charAt(pos) == ':') {
					break;
				}
				buf.append(s.charAt(pos));
			}
			parts.add(buf.toString().trim());
			if (pos >= s.length()) {
				break;
			}
			pos++;
		}
		return parts;
	}

	public static Config readConfig(File file) {
		Config result = getDefaultConfig();
		result.cattrans = new HashMap<String, String>();
		int fileformat = 0; // Assume legacy config file format
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));

			while (true) {
				String s = reader.readLine();
				if (s == null)
					break;
				// System.out.println(s);
				if (!s.contains(":"))
					continue;
				if (s.startsWith("#"))
					continue;
				List<String> parts = splitLine(s);
				String key = parts.get(0).toLowerCase();
				if (key.equals("channel")) {
					// System.out.println("Adding channel " + parts +
					// " in file format " + fileformat);
					Channel c = Channel.parseConfig(fileformat, parts);
					result.channels.add(c);
				} else if (key.equals("category")) {
					result.cattrans.put(parts.get(1), parts.get(2));
				} else if (key.equals("config-file-format")) {
					try {
						fileformat = Integer.parseInt(parts.get(1));
					} catch (NumberFormatException e) {
						logger.error("Unknown config file format "
								+ parts.get(1));
						fileformat = CURRENT_FILE_FORMAT; // may crash later
					}
					if (fileformat > CURRENT_FILE_FORMAT) {
						logger.error("Unknown config file format "
								+ parts.get(1));
						fileformat = CURRENT_FILE_FORMAT;
					}
				} else if (key.equals("cache-file")) {
					if (fileformat < 4) {
						String cacheFile = parts.get(1);
						result.cacheDbHandle = "jdbc:hsqldb:file:" + cacheFile;
						result.cacheDbUser = "SA";
						result.cacheDbPassword = "";
					} else {
						logger.warn("Illegal key cache-file in config file with fileformat "
								+ fileformat + "!");
					}
				} else if (key.equals("cache-db-handle")) {
					result.cacheDbHandle = parts.get(1);
				} else if (key.equals("cache-db-user")) {
					result.cacheDbUser = parts.get(1);
				} else if (key.equals("cache-db-password")) {
					result.cacheDbPassword = parts.get(1);
				} else if (key.equals("nice-time-milliseconds")) {
					result.niceMilliseconds = Integer.parseInt(parts.get(1));
				} else if (key.equals("fetch-channel-logos")) {
                                        String value = parts.get(1).toLowerCase();
					result.fetchLogos = (value.equals("yes") ||
                                          value.equals("true") ||
                                          value.equals("1")) ;
				} else {
					logger.error("Unknown key " + key + " in config file!");
				}
			}
		} catch (IOException e) {
			logger.debug("stack trace:", e);
			logger.warn("Error reading configuration file, continuing with empty configuration");
			return getDefaultConfig();
		}
		return result;
	}

	public void setCacheFile(String cacheFile) {
		cacheDbHandle = "jdbc:hsqldb:file:" + cacheFile;
	}

}
