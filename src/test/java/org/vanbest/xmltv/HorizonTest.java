package org.vanbest.xmltv;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class HorizonTest {

	private static Horizon horizon;
	private static List<Channel> channels = null;

	@BeforeClass
	public static void setUp() throws Exception {
		Config config = Config.getDefaultConfig();
		horizon = new Horizon(config);
		horizon.clearCache();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		horizon.close();
	}

	@Test
	public void testGetName() {
		assertEquals("Horizon name should be known", "horizon.tv", horizon.getName());
	}

	@Test
	public void testgetChannels() {
		fetchChannels();

		// there should be a least 20 channels
		assertTrue("There should be at least 20 channels from horizon.nl",
				channels.size() >= 20);
		// there should be an "RTL4" channel
		boolean foundRTL4 = false;
		for (Channel c : channels) {
			if (c.defaultName().equals("RTL 4") ||
                            c.defaultName().equals("RTL 4 HD")) {
				foundRTL4 = true;
				assertFalse("RTL 4 channel should have at least one icon",
						c.icons.isEmpty());
			}
			assertEquals("All channels should have RTL.nl source id",
					horizon.getName(), c.source);
		}
		if (!foundRTL4) {
			fail("Channel RTL4 not found, should be there");
		}
	}

	private void fetchChannels() {
		if (channels == null)
			channels = horizon.getChannels();
	}

	@Test
	public void testFindGTSTRerun() throws Exception {
		fetchChannels();
		Channel rtl4 = null;
		for (Channel c : channels) {
			if (c.defaultName().equals("RTL 4") ||
                            c.defaultName().equals("RTL 4 HD")) {
				rtl4 = c;
                                break;
			}
		}
		assertNotNull("Should be able to find RTL 4 channel", rtl4);

		List<Programme> today = horizon.getProgrammes(rtl4, 0);
		assertTrue("Expect at leat 10 programmes for a day", today.size() >= 10);

		Calendar now = Calendar.getInstance();
		if (now.get(Calendar.MONTH) <= Calendar.MAY
				|| now.get(Calendar.MONTH) >= Calendar.SEPTEMBER) {
			int offset;
			switch (now.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.SATURDAY:
				offset = 2;
				break;
			case Calendar.SUNDAY:
				offset = 1;
				break;
			default:
				offset = 0;
			}
			int rerun;
			switch (now.get(Calendar.DAY_OF_WEEK)) {
			case Calendar.FRIDAY:
				rerun = 3;
				break;
			case Calendar.SATURDAY:
				rerun = 3;
				break;
			case Calendar.SUNDAY:
				rerun = 2;
				break;
			default:
				rerun = 1;
			}
			List<Programme> first = horizon.getProgrammes(rtl4, offset);
			Programme gtstOriginal = null;
			for (Programme p : first) {
				if (p.getFirstTitle().matches("Goede Tijden.*") ||
				    p.getFirstTitle().matches("Goede tijden.*")) {
					if (p.startTime.getHours() >= 19) {
						gtstOriginal = p;
						break;
					}
				}
			}
			assertNotNull("Should have a programme called Goede Tijden, Slechte Tijden after 19:00 on date with offset "
							+ offset + " for today", gtstOriginal);
			assertNotNull("GTST should have a description",
					gtstOriginal.descriptions);
			assertTrue("GTST should have at least one description",
					gtstOriginal.descriptions.size() > 0);
			assertNotNull("GTST should have at least one non-empty description",
					gtstOriginal.descriptions.get(0).title);
			assertFalse("GTST should have at least one non-empty description",
					gtstOriginal.descriptions.get(0).title.isEmpty());
                        /* Not provided any more
			assertNotNull("GTST should have kijkwijzer information",
					gtstOriginal.ratings);
			assertTrue("GTST should have at least one kijkwijzer ratings",
					gtstOriginal.ratings.size() >= 1);
			assertNotNull("GTST rating should have kijkwijzer system",
					gtstOriginal.ratings.get(0).system);
			assertTrue("GTST rating should have kijkwijzer system filled in",
					gtstOriginal.ratings.get(0).system.matches(".*ijkwijz.*"));
			assertNotNull("GTST rating should have value",
					gtstOriginal.ratings.get(0).value);
			assertFalse("GTST rating should have value",
					gtstOriginal.ratings.get(0).value.isEmpty());
                        */

			List<Programme> reruns = horizon.getProgrammes(rtl4, rerun);
			Programme gtstRerun = null;
			for (Programme p : reruns) {
				if (p.getFirstTitle().matches("Goede Tijden.*") ||
				    p.getFirstTitle().matches("Goede tijden.*")) {
					if (p.startTime.getHours() <= 15) {
						gtstRerun = p;
						break;
					}
				}
			}
			assertNotNull(
					"Should have a programme called Goede Tijden, Slechte Tijden before 15:00 on date with offset "
							+ rerun, gtstRerun);

			assertEquals(
					"GTST rerun should have the same description as the original",
					gtstRerun.descriptions.get(0).title,
					gtstOriginal.descriptions.get(0).title);
		}
	}
}
