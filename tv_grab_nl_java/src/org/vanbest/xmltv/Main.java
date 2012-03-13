package org.vanbest.xmltv;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.io.FileUtils;

public class Main {
	
	private File configFile;
	private PrintStream outputWriter;
	private File cacheFile;
	private int days = 5;
	private int offset = 0;
	private boolean quiet = false;
	/**
	 * @param args
	 */
	
	public Main() {
		this.configFile = defaultConfigFile();
		this.outputWriter = System.out;
		this.cacheFile = defaultCacheFile();
	}
	
	public void run() throws FactoryConfigurationError, Exception {
		Config config = Config.readConfig(configFile);
		
		XmlTvWriter writer = new XmlTvWriter(outputWriter);
		writer.writeChannels(config.channels);

		TvGids gids = new TvGids(cacheFile);

		for (int day=offset; day<offset+days; day++) {
			if (!quiet) System.out.println("Fetching information for day " + day);
			Set<Programme> programmes = new HashSet<Programme>();
			for( Channel c: config.channels ) {
				ArrayList<Channel> cs = new ArrayList<Channel>(2);
				cs.add(c);
				Set<Programme> p = gids.getProgrammes(cs, day, true);
				writer.writePrograms(p);
				writer.flush();
			}
		}
		
		try {
			gids.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		writer.close();
	}
	
	public void configure() {
		TvGids gids = new TvGids(cacheFile);
		
		List<Channel> channels = gids.getChannels();
		//System.out.println(channels);
		
		Config config = new Config();
		config.setChannels(channels);
		try {
			config.writeConfig(configFile);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
	public void processOptions(String[] args) throws FileNotFoundException {
		Options options = new Options();
		options.addOption("d", "description", false, "Display a description to identify this grabber");
		options.addOption("c", "capabilities", false, "Show grabber capabilities");
		options.addOption("q", "quiet", false, "Be quiet");
		options.addOption("o", "output", true, "Set xlmtv output filename");
		options.addOption("y", "days", true, "Number of days to grab");
		options.addOption("s", "offset", true, "Start day for grabbing (0=today)");
		options.addOption("n", "configure", false, "Interactive configuration");
		options.addOption("f", "config-file", true, "Configuration file location");
		options.addOption("h", "cache", true, "Cache file location");
		//options.addOption("p", "preferredmethod", false, "Show preferred method");

		CommandLine line = null;
		try {
			line = new GnuParser().parse(options, args);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (line.hasOption("d")) {
			System.out.println("tv_grab_nl_java is a parser for Dutch TV listings using the tvgids.nl JSON interface");
			System.exit(0);
		}
		if (line.hasOption("q")) {
			this.quiet = true;
		}
		if(line.hasOption("f")) { 
			configFile = new File(line.getOptionValue("f"));	
		}
		if (line.hasOption("o")) {
			this.outputWriter = new PrintStream( new FileOutputStream(line.getOptionValue("o")));
		}
		if (line.hasOption("h")) {
			this.cacheFile = new File(line.getOptionValue("h"));
		}
		if (line.hasOption("y")) {
			this.days = Integer.parseInt(line.getOptionValue("y"));
		}
		if (line.hasOption("s")) {
			this.offset = Integer.parseInt(line.getOptionValue("s"));
		}
		if (line.hasOption("c")) {
			System.out.println("baseline");
			System.out.println("manualconfig");
			System.out.println("cache");
			// System.out.println("preferredmethod");
			System.exit(0);
		}
		if (line.hasOption("n")) {
			configure();
			System.exit(0);
		}

	}
	
	public static File defaultConfigFile() {
		return FileUtils.getFile(FileUtils.getUserDirectory(), ".xmltv", "tv_grab_nl_java.conf");
	}
	
	public static File defaultCacheFile() {
		return FileUtils.getFile(FileUtils.getUserDirectory(), ".xmltv", "tv_grab_nl_java.cache");
	}

	public static void main(String[] args)  {
		Main main = new Main();
		try {
			main.processOptions(args);
			main.run();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
