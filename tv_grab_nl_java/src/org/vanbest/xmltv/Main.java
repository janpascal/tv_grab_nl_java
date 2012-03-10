package org.vanbest.xmltv;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.Parser;

public class Main {
	
	/**
	 * @param args
	 */
	
	public static void test() {
		TvGids gids = new TvGids();
		
		List<Channel> channels = gids.getChannels();
		
		try {
			List<Channel> myChannels = channels; // .subList(0,  2);
			Set<Programme> programmes = new HashSet<Programme>();
			for( Channel c: myChannels ) {
				ArrayList<Channel> cs = new ArrayList<Channel>(2);
				cs.add(c);
				Set<Programme> p = gids.getProgrammes(cs, 0, true);
				programmes.addAll( p );
			}
			
			XmlTvWriter writer = new XmlTvWriter(new FileOutputStream("/tmp/tv_grab_nl_java.xml"));
			writer.writeChannels(myChannels);
			writer.writePrograms(programmes);
			writer.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				gids.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void configure() {
		TvGids gids = new TvGids();
		
		List<Channel> channels = gids.getChannels();
		
		
	}
	
	public void processOptions(String[] args) {
		Options options = new Options();
		options.addOption("d", "description", false, "Display a description to identify this grabber");
		options.addOption("c", "capablities", false, "Show grabber capabilities");
		options.addOption("q", "quiet", false, "Be quiet");
		options.addOption("o", "output", true, "Set xlmtv output filename");
		options.addOption("d", "days", true, "Number of days to grab");
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
	
	public static void main(String[] args) {
		Main main = new Main();
		main.processOptions(args);
	}

}
