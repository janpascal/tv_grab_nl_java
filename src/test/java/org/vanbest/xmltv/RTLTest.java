package org.vanbest.xmltv;

import static org.junit.Assert.*;

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
		fail("Not yet implemented");
	}

}
