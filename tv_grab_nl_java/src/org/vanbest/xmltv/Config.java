package org.vanbest.xmltv;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class Config {
	public String configFile;
	public List<Channel> channels;
	
	public Config() {
	}
	
	public void writeConfig() {
		
	}
	
	public static File defaultConfigFile() {
		return FileUtils.getFile(FileUtils.getUserDirectory(), ".xmltv", "tv_grab_nl_java.conf");
	}

	public static Config readConfig() throws IOException {
		return readConfig(defaultConfigFile().getCanonicalPath());
	}

	public static Config readConfig(String filename) {
		Config result = new Config();
		return result;
	}
}
