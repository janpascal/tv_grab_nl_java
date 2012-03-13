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
	
	public static String escape(String s) {
		return "\"" + s.replaceAll("\\\"", "\\\\\"") + "\"";
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
						buf.append(s.substring(quoteStart, pos).replaceAll("\\\\\"", "\\\""));
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

