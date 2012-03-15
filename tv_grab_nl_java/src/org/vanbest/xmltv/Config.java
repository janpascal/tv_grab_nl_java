package org.vanbest.xmltv;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class Config {
	public List<Channel> channels;
	public Map<String, String> cattrans;
	protected File cacheFile;
	boolean quiet = false;
	
	private Config() {
	}
	
	public Map<String,String> getCategories() {
		return cattrans;
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
		for(Channel c: channels) {
			if (!c.selected) {
				out.print("#");
			}
			out.print("channel: " + c.id + ": " + escape(c.name));
			if (c.iconUrl != null) {
				out.print(" : " + escape(c.iconUrl));
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
		Config result = new Config();
		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( file)));
			List<Channel> channels = new ArrayList<Channel>();
			Map<String,String> cattrans = new HashMap<String,String>();
			File cacheFile = defaultCacheFile();
			while(true) {
				String s = reader.readLine();
				if(s==null) break;
				if (!s.contains(":")) continue;
				if (s.startsWith("#")) continue;
				List<String> parts = splitLine(s);
				if (parts.get(0).toLowerCase().equals("channel")) {
					Channel c = new Channel(Integer.parseInt(parts.get(1)), parts.get(2), "");
					if (parts.size()>3) {
						c.setIconUrl(parts.get(3));
					}
			 		channels.add(c);
				}
				if (parts.get(0).toLowerCase().equals("category")) {
					cattrans.put(parts.get(1), parts.get(2));
				}
				if (parts.get(0).toLowerCase().equals("cache-file")) {
					cacheFile = new File(parts.get(1));
				}
			}
			result.setChannels(channels);
			result.cattrans = cattrans;
			result.cacheFile = cacheFile;
		} catch (IOException e) {
			System.out.println("Cannot read configuration file, continuing with empty configuration");
			return getDefaultConfig();
		}
		return result;
	}
	
	public static Config getDefaultConfig() {
		Config result = new Config();
		result.channels = new ArrayList<Channel>();
		result.cattrans = getDefaultCattrans();
		result.cacheFile = defaultCacheFile();
		return result;
	}
	
	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}

}

