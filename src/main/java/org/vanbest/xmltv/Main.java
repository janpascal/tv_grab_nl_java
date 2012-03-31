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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;
import org.apache.commons.io.FileUtils;

public class Main {
	private File configFile;
	private Config config;
	private PrintStream outputWriter;
	private int days = 5;
	private int offset = 0;
	/**
	 * @param args
	 */
	
	public Main() {
		this.configFile = defaultConfigFile();
		this.outputWriter = System.out;
	}
	
	public void run() throws FactoryConfigurationError, Exception {
		if (!config.quiet) {
			System.out.println("tv_grab_nl_java version "+config.project_version + ", Copyright (C) 2012 Jan-Pascal van Best <janpascal@vanbest.org>");
 			System.out.println("tv_grab_nl_java comes with ABSOLUTELY NO WARRANTY. It is free software, and you are welcome to redistribute it");
 			System.out.println("under certain conditions; `tv_grab_nl_java --license' for details.");

			System.out.println("Fetching programme data for " + this.days + " starting from day " + this.offset);
			int enabledCount = 0;
			for(Channel c: config.channels) { if (c.enabled) enabledCount++; } 
			System.out.println("... from " + enabledCount + " channels");
			System.out.println("... using cache file " + config.cacheFile.getCanonicalPath());
		}
		
		XmlTvWriter writer = new XmlTvWriter(outputWriter, config);
		writer.writeChannels(config.channels);

		EPGSource gids = new TvGids(config);

		for (int day=offset; day<offset+days; day++) {
			if (!config.quiet) System.out.print("Fetching information for day " + day);
			Set<TvGidsProgramme> programmes = new HashSet<TvGidsProgramme>();
			for(Channel c: config.channels) {
				if (!c.enabled) continue;
				if (!config.quiet) System.out.print(".");
				Set<TvGidsProgramme> p = gids.getProgrammes(c, day, true);
				writer.writePrograms(p);
				writer.flush();
			}
			if (!config.quiet) System.out.println();
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
		if (!config.quiet) {
			EPGSource.Stats stats = gids.getStats();
			System.out.println("Number of programmes from cache: " + stats.cacheHits);
			System.out.println("Number of programmes fetched: " + stats.cacheMisses);
			System.out.println("Number of fetch errors: " + stats.fetchErrors);
		}
	}
	
	public void configure() throws IOException {
		EPGSource gids = new TvGids(config);
		
		Set<String> oldChannels = new HashSet<String>();
		for (Channel c: config.channels) {
			if (c.enabled) { 
				oldChannels.add(c.source+"::"+c.id); 
			}
		}
		List<Channel> channels = gids.getChannels();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		boolean all = false;
		boolean none = false;
		boolean keep = false;
		for (Channel c: channels) {
			boolean selected = oldChannels.contains(c.source+"::"+c.id);
			System.out.print("add channel " + c.id + " (" + c.defaultName() + ") [[y]es,[n]o,[a]ll,[none],[k]eep selection (default=" + (selected?"yes":"no") + ")] ");
			if (keep) {
				c.enabled = selected;
				System.out.println(selected?"Y":"N");
				continue;
			} 
			if (all) {
				c.enabled = true;
				System.out.println("Y");
				continue;
			} 
			if (none) {
				c.enabled = false;
				System.out.println("N");
				continue;
			} 
			while(true) {
				String s = reader.readLine().toLowerCase();
				if (s.isEmpty()) {
					c.enabled = selected;
					break;
				} else if ( s.startsWith("k")) {
					c.enabled = selected;
					keep = true;
					break;
				} else if ( s.startsWith("y")) {
					c.enabled = true;
					break;
				} else if ( s.startsWith("a")) {
					c.enabled = true;
					all = true;
					break;
				} else if ( s.startsWith("none")) {
					c.enabled = false;
					none = true;
					break;
				} else if ( s.startsWith("n")) {
					c.enabled = false;
					break;
				}
			}
		}
		
		config.setChannels(channels);
		try {
			config.writeConfig(configFile);
			System.out.println("Configuration file written to " + configFile.getPath());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static String copyright = "Copyright (c) 2012 Jan-Pascal van Best <janpascal@vanbest.org>" + System.getProperty("line.separator") +
	  "" + System.getProperty("line.separator") +
	  "This program is free software; you can redistribute it and/or modify" + System.getProperty("line.separator") +
	  "it under the terms of the GNU General Public License as published by" + System.getProperty("line.separator") +
	  "the Free Software Foundation; either version 2 of the License, or" + System.getProperty("line.separator") +
	  "(at your option) any later version." + System.getProperty("line.separator") +
      "" + System.getProperty("line.separator") +
	  "This program is distributed in the hope that it will be useful," + System.getProperty("line.separator") +
	  "but WITHOUT ANY WARRANTY; without even the implied warranty of" + System.getProperty("line.separator") +
	  "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the" + System.getProperty("line.separator") +
	  "GNU General Public License for more details." + System.getProperty("line.separator") +
	  "" + System.getProperty("line.separator") +
      "You should have received a copy of the GNU General Public License along" + System.getProperty("line.separator") +
	  "with this program; if not, write to the Free Software Foundation, Inc.," + System.getProperty("line.separator") +
	  "51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.";
	
    public void showLicense() {
      	System.out.println(copyright);
    }
	
    public void processOptions(String[] args) throws FileNotFoundException {
    	Options options = new Options();
    	options.addOption(OptionBuilder
    					.withLongOpt("description")
    					.withDescription("Display a description to identify this grabber")
    					.create())
    			.addOption(OptionBuilder
    					.withLongOpt("capabilities")
    					.withDescription("Show grabber capabilities")
    					.create())
				.addOption(OptionBuilder
						.withLongOpt("quiet")
						.withDescription("Be quiet")
						.create())
				.addOption(OptionBuilder
						.withLongOpt("output")
						.hasArg()
						.withDescription("Set xlmtv output filename")
						.create())
				.addOption(OptionBuilder
						.withLongOpt("days")
						.hasArg()
						.withDescription("Number of days to grab")
						.create())
				.addOption(OptionBuilder
						.withLongOpt("offset")
						.hasArg()
						.withDescription("Start day for grabbing (0=today)")
						.create())
				.addOption(OptionBuilder
						.withLongOpt("configure")
						.withDescription("Interactive configuration")
						.create())
				.addOption(OptionBuilder
						.withLongOpt("config-file")
						.hasArg()
						.withDescription("Configuration file location")
						.create())
				.addOption(OptionBuilder
						.withLongOpt("cache")
						.hasArg()
						.withDescription("Cache file location")
						.create())
				.addOption(OptionBuilder
						.withLongOpt("help")
						.withDescription("Show this help")
						.create())
				.addOption(OptionBuilder
						.withLongOpt("log-level")
						.hasArg()
						.withDescription("Set log level (0x0100=JSON)")
						.create())
				.addOption(OptionBuilder
						.withLongOpt("license")
						.withDescription("Show license information")
						.create());
		//.addOption(OptionBuilder.withLongOpt("preferredmethod").withDescription("Show preferred method").create();

		CommandLine line = null;
		try {
			line = new GnuParser().parse(options, args);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(line.hasOption("license")) { 
                        showLicense();
                        System.exit(0);
		}
		if(line.hasOption("config-file")) { 
			configFile = new File(line.getOptionValue("config-file"));	
		}
		config = Config.readConfig(configFile);
		if (line.hasOption("quiet")) {
			config.quiet = true;
		}
		if (line.hasOption("description")) {
			System.out.println("tv_grab_nl_java version " + config.project_version);
			System.out.println("tv_grab_nl_java is a parser for Dutch TV listings using the tvgids.nl JSON interface");
			System.exit(0);
		}
		if (line.hasOption("help")) {
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "tv_grab_nl_java", options );
			System.exit(0);
		}
		if (line.hasOption("output")) {
			this.outputWriter = new PrintStream( new FileOutputStream(line.getOptionValue("output")));
		}
		if (line.hasOption("log-level")) {
			config.logLevel = Integer.parseInt(line.getOptionValue("log-level"));
		}
		if (line.hasOption("cache")) {
			config.cacheFile = new File(line.getOptionValue("cache"));
		}
		if (line.hasOption("days")) {
			this.days = Integer.parseInt(line.getOptionValue("days"));
		}
		if (line.hasOption("offset")) {
			this.offset = Integer.parseInt(line.getOptionValue("offset"));
		}
		if (line.hasOption("capabilities")) {
			System.out.println("baseline");
			System.out.println("manualconfig");
			System.out.println("cache");
			// System.out.println("preferredmethod");
			System.exit(0);
		}
		if (line.hasOption("configure")) {
			try {
				configure();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.exit(0);
		}
	}
	
	public static File defaultConfigFile() {
		return FileUtils.getFile(FileUtils.getUserDirectory(), ".xmltv", "tv_grab_nl_java.conf");
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
