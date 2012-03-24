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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

public class Config {
	public int niceMilliseconds;
	public List<Channel> channels;
	public Map<String, String> cattrans;
	protected File cacheFile;
	boolean quiet = false;
	public int logLevel = LOG_DEFAULT;
	
	public static final int LOG_INFO = 0x0001;
	public static final int LOG_JSON = 0x0100;
	private static final int LOG_PROGRAMME_INFO = 0x0200;
	
	public static int LOG_DEFAULT = LOG_INFO;

	String project_version;
	
	private Config() {
		Properties configProp = new Properties();
        InputStream in = this.getClass().getResourceAsStream("/tv_grab_nl_java.properties");
        try {
            configProp.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        project_version=configProp.getProperty("project.version");
	}
	
	public static Config getDefaultConfig() {
		Config result = new Config();
		result.channels = new ArrayList<Channel>();
		result.cattrans = getDefaultCattrans();
		result.cacheFile = defaultCacheFile();
		result.niceMilliseconds = 500;
		return result;
	}
		
	public Map<String,String> getCategories() {
		return cattrans;
	}

	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}

	public static File defaultCacheFile() {
		return FileUtils.getFile(FileUtils.getUserDirectory(), ".xmltv", "tv_grab_nl_java.cache");
	}

	static private Map<String,String> getDefaultCattrans() {
		Map<String,String> result = new HashMap<String,String>();
		result.put("amusement", "Animated");
		result.put("comedy", "Comedy");
		result.put("documentaire", "Documentary");
		result.put("educatief", "Educational");
		result.put("erotiek", "Adult");
		result.put("film", "Film");
		result.put("muziek", "Art/Music");
		result.put("informatief", "Educational");
		result.put("jeugd", "Children");
		result.put("kunst/cultuur", "Arts/Culture");
		result.put("misdaad", "Crime/Mystery");	
		result.put("muziek", "Music");
		result.put("natuur", "Science/Nature");
		result.put("nieuws/actualiteiten", "News");
		result.put("overige", "Unknown");
		result.put("religieus", "Religion");
		result.put("serie/soap", "Drama");
		result.put("sport", "Sports");
		result.put("theater", "Arts/Culture");
		result.put("wetenschap", "Science/Nature");
		return result;
	}

	public void writeConfig(File configFile) throws IOException {
		FileUtils.forceMkdir(configFile.getParentFile());
		PrintWriter out = new PrintWriter(new OutputStreamWriter( new FileOutputStream( configFile )));
		out.println("cache-file: " + escape(cacheFile.getPath()));
		out.println("nice-time-milliseconds: " + niceMilliseconds);
		for(Channel c: channels) {
			if (!c.selected) {
				out.print("#");
			}
			// FIXME: handle multiple channels names, icons and urls
			out.print("channel: " + c.id + ": " + escape(c.defaultName()));
			if (!c.icons.isEmpty()) {
				out.print(" : " + escape(c.icons.get(0).url));
			}
			out.println();
		}
		for(Map.Entry<String,String> entry: cattrans.entrySet()) {
			out.println("category: " + escape(entry.getKey()) + ": " + escape(entry.getValue()));
		}
		out.close();
	}
	
	public static String escape(String s) {
		return "\"" + s.replace("\\", "\\\\").replaceAll("\\\"", "\\\\\"") + "\"";
	}

	public static List<String> splitLine(String s) {
		List<String> parts = new ArrayList<String>(5);
		int pos=0;
		while (true) {
			// Find first colon outside quotes
			boolean quoted=false;
			int quoteStart=-1;
			StringBuffer buf = new StringBuffer();
			for (; pos<s.length(); pos++) {
				if (s.charAt(pos)=='"') {
					if (quoted) {
						//System.out.println(s.substring(quoteStart, pos));
						buf.append(s.substring(quoteStart, pos).replaceAll("\\\\\"", "\\\"").replaceAll("\\\\\\\\","\\\\"));
					} else {
						quoteStart = pos+1;
					}
					quoted=!quoted;
					continue;
				}
				if(s.charAt(pos)=='\\') pos++;
				if(quoted) continue;
				if(s.charAt(pos)==':') {
					break;
				}
				buf.append(s.charAt(pos));
			}
			parts.add(buf.toString().trim());
			if (pos>=s.length()) {
				break;
			}
			pos++;
		}
		return parts;
	}

	public static Config readConfig(File file) {
		Config result = getDefaultConfig();
		result.cattrans = new HashMap<String,String>();
		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( file)));
			
			while(true) {
				String s = reader.readLine();
				if(s==null) break;
				if (!s.contains(":")) continue;
				if (s.startsWith("#")) continue;
				List<String> parts = splitLine(s);
				if (parts.get(0).toLowerCase().equals("channel")) {
					Channel c = Channel.getChannel(parts.get(1), parts.get(2));
					if (parts.size()>3) {
						c.addIcon(parts.get(3));
					}
			 		result.channels.add(c);
				}
				switch (parts.get(0).toLowerCase()) {
				case "category" :
					result.cattrans.put(parts.get(1), parts.get(2));
					break;
				case "cache-file":
					result.cacheFile = new File(parts.get(1));
					break;
				case "nice-time-milliseconds":
					result.niceMilliseconds = Integer.parseInt(parts.get(1));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Error reading configuration file, continuing with empty configuration");
			return getDefaultConfig();
		}
		return result;
	}
	
	public boolean logJSON() {
		return (logLevel & LOG_JSON) != 0;
	}

	public boolean logProgrammes() {
		return (logLevel & LOG_PROGRAMME_INFO) != 0;
	}

}

