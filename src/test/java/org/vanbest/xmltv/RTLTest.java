package org.vanbest.xmltv;

import static org.junit.Assert.*;

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
	public void testGetProgrammesListOfChannelInt() {
		List<Channel> channels = rtl.getChannels();
		
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

}
