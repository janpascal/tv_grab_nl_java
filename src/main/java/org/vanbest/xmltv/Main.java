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

			System.out.println("Fetching programme data for days " + this.offset + "-" + (this.offset+this.days-1));
			System.out.println("... from " + config.channels.size() + " channels");
			System.out.println("... using cache file " + config.cacheFile.getCanonicalPath());
		}
		
		XmlTvWriter writer = new XmlTvWriter(outputWriter, config);
		writer.writeChannels(config.channels);

		TvGids gids = new TvGids(config);

		for (int day=offset; day<offset+days; day++) {
			if (!config.quiet) System.out.print("Fetching information for day " + day);
			Set<Programme> programmes = new HashSet<Programme>();
			for( Channel c: config.channels ) {
				if (!config.quiet) System.out.print(".");
				ArrayList<Channel> cs = new ArrayList<Channel>(2);
				cs.add(c);
				Set<Programme> p = gids.getProgrammes(cs, day, true);
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
			System.out.println("Number of programmes from cache: " + gids.cacheHits);
			System.out.println("Number of programmes fetched: " + gids.cacheMisses);
			System.out.println("Number of fetch errors: " + gids.fetchErrors);
		}
	}
	
	public void configure() throws IOException {
		TvGids gids = new TvGids(config);
		
		Set<Integer> oldChannels = new HashSet<Integer>();
		for (Channel c: config.channels) {
			oldChannels.add(c.id);
		}
		List<Channel> channels = gids.getChannels();
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		boolean all = false;
		boolean none = false;
		boolean keep = false;
		for (Channel c: channels) {
			boolean selected = oldChannels.contains(c.id);
			System.out.print("add channel " + c.id + " (" + c.name + ") [[y]es,[n]o,[a]ll,[none],[k]eep selection (default=" + (selected?"yes":"no") + ")] ");
			if (keep) {
				c.selected = selected;
				System.out.println(selected?"Y":"N");
				continue;
			} 
			if (all) {
				c.selected = true;
				System.out.println("Y");
				continue;
			} 
			if (none) {
				c.selected = false;
				System.out.println("N");
				continue;
			} 
			while(true) {
				String s = reader.readLine().toLowerCase();
				if (s.isEmpty()) {
					c.selected = selected;
					break;
				} else if ( s.startsWith("k")) {
					c.selected = selected;
					keep = true;
					break;
				} else if ( s.startsWith("y")) {
					c.selected = true;
					break;
				} else if ( s.startsWith("a")) {
					c.selected = true;
					all = true;
					break;
				} else if ( s.startsWith("none")) {
					c.selected = false;
					none = true;
					break;
				} else if ( s.startsWith("n")) {
					c.selected = false;
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
		options.addOption("d", "description", false, "Display a description to identify this grabber");
		options.addOption("c", "capabilities", false, "Show grabber capabilities");
		options.addOption("q", "quiet", false, "Be quiet");
		options.addOption("o", "output", true, "Set xlmtv output filename");
		options.addOption("y", "days", true, "Number of days to grab");
		options.addOption("s", "offset", true, "Start day for grabbing (0=today)");
		options.addOption("n", "configure", false, "Interactive configuration");
		options.addOption("f", "config-file", true, "Configuration file location");
		options.addOption("h", "cache", true, "Cache file location");
		options.addOption("e", "help", false, "Show this help");
		options.addOption("l", "log-level", true, "Set log level (0x0100=JSON)");
		options.addOption("i", "license", false, "Show license information");
		//options.addOption("p", "preferredmethod", false, "Show preferred method");

		CommandLine line = null;
		try {
			line = new GnuParser().parse(options, args);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(line.hasOption("i")) { 
                        showLicense();
                        System.exit(0);
		}
		if(line.hasOption("f")) { 
			configFile = new File(line.getOptionValue("f"));	
		}
		config = Config.readConfig(configFile);
		if (line.hasOption("q")) {
			config.quiet = true;
		}
		if (line.hasOption("d")) {
			System.out.println("tv_grab_nl_java version " + config.project_version);
			System.out.println("tv_grab_nl_java is a parser for Dutch TV listings using the tvgids.nl JSON interface");
			System.exit(0);
		}
		if (line.hasOption("e")) {
			// automatically generate the help statement
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "tv_grab_nl_java", options );
			System.exit(0);
		}
		if (line.hasOption("o")) {
			this.outputWriter = new PrintStream( new FileOutputStream(line.getOptionValue("o")));
		}
		if (line.hasOption("l")) {
			config.logLevel = Integer.parseInt(line.getOptionValue("l"));
		}
		if (line.hasOption("h")) {
			config.cacheFile = new File(line.getOptionValue("h"));
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
