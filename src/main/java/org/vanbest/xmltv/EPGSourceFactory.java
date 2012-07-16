package org.vanbest.xmltv;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

public class EPGSourceFactory {
	private static Map<String,Integer> ids = new HashMap<String,Integer>();
	private static Map<Integer,Class<EPGSource>> classes = new HashMap<Integer,Class<EPGSource>>();
	private static Map<Integer,String> names = new HashMap<Integer,String>();
	private static boolean initialised=false;
	private static List<Integer> sources=new ArrayList<Integer>();
	static Logger logger = Logger.getLogger(EPGSourceFactory.class);


	static void init() {
		if(initialised) return;
		Properties configProp = new Properties();
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        InputStream in = loader.getResourceAsStream("tv_grab_nl_java.properties");
        try {
            configProp.load(in);
        } catch (IOException e) {
            logger.warn("Error reading application properties resource", e);
        }
        for(int source=1; ; source++) {
        	String name = configProp.getProperty("org.vanbest.xmltv.epgsource.impl."+source);
        	if (name==null) break;
			try {
				Class<EPGSource> clazz = (Class<EPGSource>) loader.loadClass(name);
				classes.put(source,  clazz);
				// System.out.println("clazz: " + clazz.toString());
				Field NAME=clazz.getField("NAME");
				// System.out.println("NAME: " + NAME.toString());
				String sourceName=(String)NAME.get(null);
				names.put(source,sourceName);
				ids.put(sourceName,source);
				sources.add(source);
			} catch (Exception e) {
				logger.error("Error reading EPG Source class "+name, e);
			}
        }
        initialised=true;
	}

	private EPGSourceFactory() {
		init();
	}

	public static EPGSourceFactory newInstance() {
		return new EPGSourceFactory();
	}
	
	public EPGSource createEPGSource(int source, Config config) {
		Constructor<EPGSource> constructor;
		try {
			constructor = classes.get(source).getConstructor(Integer.TYPE,Config.class);
			return constructor.newInstance(source, config);
		} catch (Exception e) {
			logger.error("Error instantiating EPG source "+classes.get(source), e);
		}
		return null;
	}
	
	public EPGSource createEPGSource(String source, Config config) {
		int sourceId = getChannelSourceId(source);
		return createEPGSource(sourceId, config);
	}
	
	public String getChannelSourceName(int id) {
		return names.get(id);
	}
	
	public int getChannelSourceId(String name) {
		return ids.get(name);
	}

	public int[] getAll() {
		int[] result = new int[sources.size()];
		for(int i=0; i<result.length; i++) {
			result[i]=sources.get(i);
		}
		return result;
	}
}
