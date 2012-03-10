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
	
	public void writeConfig(String filename) throws IOException {
		FileUtils.forceMkdir(new File(filename).getParentFile());
		PrintWriter out = new PrintWriter(new OutputStreamWriter( new FileOutputStream( filename )));
		for(Channel c: channels) {
			out.println(c.id + ": " + c.name);
		}
		out.close();
	}
	
	public static File defaultConfigFile() {
		return FileUtils.getFile(FileUtils.getUserDirectory(), ".xmltv", "tv_grab_nl_java.conf");
	}

	public static Config readConfig() throws IOException {
		return readConfig(defaultConfigFile().getCanonicalPath());
	}

	public static Config readConfig(String filename) throws IOException {
		Config result = new Config();
		BufferedReader reader = new BufferedReader( new InputStreamReader( new FileInputStream( filename)));
		List<Channel> channels = new ArrayList<Channel>();
		while(true) {
			String s = reader.readLine();
			if(s==null) break;
			if (!s.contains(":")) continue;
			if (s.startsWith("#")) continue;
			String[] parts = s.split("[[:space:]]*:[[:space:]]*", 2);
			Channel c = new Channel(Integer.parseInt(parts[0]), parts[1], "");
			channels.add(c);
		}
		result.setChannels(channels);
		return result;
	}
	
	public void setChannels(List<Channel> channels) {
		this.channels = channels;
	}

}
