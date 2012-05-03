package org.vanbest.xmltv;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class RTLTest {

	private RTL rtl;
	private final static int RTL_SOURCE_ID=2;
	@Before
	public void setUp() throws Exception {
		Config config = Config.getDefaultConfig();
		rtl = new RTL(RTL_SOURCE_ID, config);
	}

	@After
	public void tearDown() throws Exception {
		rtl.close();
	}

	@Test
	public void testGetName() {
		assertEquals("RTL name should be known", "rtl.nl", rtl.getName());
	}

	@Test
	public void testgetChannels() {
		List<Channel> channels = rtl.getChannels();
		
		// there should be a least 20 channels
		assertTrue("There should be at least 20 channels from rtl.nl", channels.size()>=20);
		// there should be an "RTL 4" channel
		boolean foundRTL4 = false;
		for(Channel c: channels) {
			if(c.defaultName().equals("RTL 4")) {
				foundRTL4 = true;
				assertFalse("RTL 4 channel should have at least one icon", c.icons.isEmpty());
			}
			assertEquals("All channels should have RTL.nl source id", RTL_SOURCE_ID, c.source);
		}
		if(!foundRTL4) {
			fail("Channel RTL4 not found, should be there");
		}
	}

	@Test
	public void testFindGTSTRerun() throws Exception {
		List<Channel> channels = rtl.getChannels();

		Channel rtl4 = null;
		for(Channel c: channels) {
			if(c.defaultName().equals("RTL 4")) {
				rtl4 = c;
			}
		}
		assertNotNull("Should be able to find RTL 4 channel", rtl4);
		
		List<Programme> today = rtl.getProgrammes(rtl4, 0);
		assertTrue("Expect at leat 10 programmes for a day", today.size()>=10);
		
		Calendar now = Calendar.getInstance();
		if(now.get(Calendar.MONTH)<=Calendar.MAY || now.get(Calendar.MONTH)>=Calendar.SEPTEMBER) {
			int offset;
			switch(now.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SATURDAY: offset = 2; break;
			case Calendar.SUNDAY: offset = 1; break;
			default: offset = 0;
			}
			int rerun;
			switch(now.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.FRIDAY: rerun = 3; break;
			case Calendar.SATURDAY: rerun = 2; break;
			default: rerun = 1;
			}
			List<Programme> first = rtl.getProgrammes(rtl4,  offset);
			Programme gtstOriginal = null;
			for(Programme p: first) {
				if(p.getFirstTitle().matches("Goede Tijden.*")) {
					if (p.startTime.getHours()>=19) {
						gtstOriginal = p;
						break;
					}
				}
			}
			assertNotNull("Should have a programme called Goede Tijden, Slechte Tijden after 19:00 on date with offset "+offset+ " for today", gtstOriginal);
			System.out.println(gtstOriginal.toString());

			List<Programme> reruns = rtl.getProgrammes(rtl4,  rerun);
			Programme gtstRerun = null;
			for(Programme p: reruns) {
				if(p.getFirstTitle().matches("Goede Tijden.*")) {
					if (p.startTime.getHours()<=15) {
						gtstRerun = p;
						break;
					}
				}
			}
			assertNotNull("Should have a programme called Goede Tijden, Slechte Tijden before 15:00 on date with offset "+rerun, gtstRerun);

			assertEquals("GTST rerun should have the same description as the original", gtstRerun.descriptions.get(0).title, gtstOriginal.descriptions.get(0).title);
		}
		
		
		
		
	}
}
