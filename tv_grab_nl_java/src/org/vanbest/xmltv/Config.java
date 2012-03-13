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
import java.util.List;

import org.apache.commons.io.FileUtils;

public class Config {
	public List<Channel> channels;
	
	public Config() {
	}
	
	public void writeConfig(File configFile) throws IOException {
		FileUtils.forceMkdir(configFile.getParentFile());
		PrintWriter out = new PrintWriter(new OutputStreamWriter( new FileOutputStream( configFile )));
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
		out.close();
	}
	
	public static String unescape(String s) {
		String result = s.trim();
		if (result.charAt(0)=='"') {
			result = result.substring(1, result.length()-1);
			result = result.replaceAll("\\:",":").replaceAll("\\\"", "\"");
		}
		return result;
	}
	
	public static String escape(String s) {
		return "\"" + s.replaceAll("\"", "\\\"").replaceAll(":", "\\:") + "\"";
	}


	public static String[] old_splitLine(String s) {
		int colon = 0;
		while (true) {
			colon  = s.indexOf(':', colon+1);
			if (colon<0 || s.charAt(colon-1) != '\\') {
				break;
			}
		}
		if (colon<0) {
			String[] parts = {s};
			return parts;
		} 
		String id = s.substring(0,  colon).trim();
		int firstColon = colon;
		while (true) {
			colon  = s.indexOf(':', colon+1);
			if (colon<0 || s.charAt(colon-1) != '\\') {
				break;
			}
		}
		if (colon<0) {
			String name = unescape(s.substring(firstColon+1));
			
			String[] parts = {id, name};
			return parts;
		} 
		String name = unescape(s.substring(firstColon+1, colon));
		String icon = unescape(s.substring(colon+1));
		String[] parts = {id,name,icon};
		return parts;
	}
	public static List<String> splitLine(String s) {
		int colon = 0;
		int prev = 0;
		List<String> parts = new ArrayList<String>(5);
		while (true) {
			// Find first unescaped colon	
			while (true) {
				colon  = s.indexOf(':', prev);
				if (colon<0 || s.charAt(colon-1) != '\\') {
					break;
				}
			}
			if (colon<0) {
				// Last part
				parts.add(unescape(s.substring(prev)));
				break;
			} else {
				parts.add(unescape(s.substring(prev,colon)));
				prev = colon+1;
			}
		}
		return parts;
	}
	
	public static Config readConfig(File file) {
		Config result = new Config();
		try {
			BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( file)));
			List<Channel> channels = new ArrayList<Channel>();
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
			}
			result.setChannels(channels);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Cannot read configuration file, continuing with empty configuration");
		}
		return result;
	}
	
	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}

}

