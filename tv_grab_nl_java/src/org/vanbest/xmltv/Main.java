package org.vanbest.xmltv;

import java.util.List;
import java.util.Set;

public class Main {
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<Channel> channels = TvGids.getChannels();
		System.out.println(channels);
		
		try {
			System.out.println(TvGids.programmeUrl(channels, 0));
			
			List<Channel> myChannels = channels.subList(0,  2);
			Set<Programme> programmes = TvGids.getProgrammes(myChannels, 0, true);
			
			System.out.println( programmes );
			
			XmlTvWriter writer = new XmlTvWriter(System.out);
			writer.writeChannels(myChannels);
			System.out.flush();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

}
