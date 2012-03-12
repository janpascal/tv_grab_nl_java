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
			out.print(c.id + ": " + escape(c.name));
			if (c.iconUrl != null) {
				out.print(" : " + escape(c.iconUrl));
			}
			out.println();
		}
		out.close();
	}
	
	public static String unescape(String s) {
		String result = s.trim();
		result = result.substring(1, result.length()-1);
		result = result.replaceAll("\\\"", "\"");
		return result;
	}
	
	public static String escape(String s) {
		return "\"" + s.replaceAll("\"", "\\\"") + "\"";
	}
	
	public static String[] splitLine(String s) {
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
	
	public static Config readConfig(File file) throws IOException {
		Config result = new Config();
		BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( file)));
		List<Channel> channels = new ArrayList<Channel>();
		while(true) {
			String s = reader.readLine();
			if(s==null) break;
			if (!s.contains(":")) continue;
			if (s.startsWith("#")) continue;
			String[] parts = splitLine(s);
			Channel c = new Channel(Integer.parseInt(parts[0]), parts[1], "");
			if (parts.length>2) {
				c.setIconUrl(parts[2]);
			}
			channels.add(c);
		}
		result.setChannels(channels);
		return result;
	}
	
	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}

}
