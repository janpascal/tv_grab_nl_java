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
			out.println(c.id + ": " + c.name);
		}
		out.close();
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
			String[] parts = s.split("[[:space:]]*:[[:space:]]*", 2);
			Channel c = new Channel(Integer.parseInt(parts[0]), parts[1].trim(), "");
			channels.add(c);
		}
		result.setChannels(channels);
		return result;
	}
	
	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}

}
