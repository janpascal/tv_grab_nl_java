package org.vanbest.xmltv;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.StringWriter;

import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.WriterAppender;
import org.junit.Test;

public class MainTest {

	@Test
	public void testShowHeader() {
		Logger logger = Logger.getLogger(Main.class);
		StringWriter  writer = new StringWriter();
		logger.addAppender(new WriterAppender(new SimpleLayout(), writer));
		Main main = new Main();
		String[] args = {"--license"}; 
		try {
			main.processOptions(args);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		main.showHeader();
		String result = writer.toString();
		assertTrue(result.contains("ABSOLUTELY NO WARRANTY"));
		// fail("Not yet implemented");
	}

	@Test
	public void testConfigure() {
		fail("Not yet implemented");
	}

	@Test
	public void testShowLicense() {
		fail("Not yet implemented");
	}

	@Test
	public void testDefaultConfigFile() {
		fail("Not yet implemented");
	}

	@Test
	public void testMain() {
		fail("Not yet implemented");
	}

}
