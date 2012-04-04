package org.vanbest.xmltv;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EPGSourceFactory {
	private static Map<String,Integer> ids = new HashMap<String,Integer>();
	private static Map<Integer,Class<EPGSource>> classes = new HashMap<Integer,Class<EPGSource>>();
	private static Map<Integer,String> names = new HashMap<Integer,String>();
	private static boolean initialised=false;

	static void init() {
		if(initialised) return;
		Properties configProp = new Properties();
        ClassLoader loader = ClassLoader.getSystemClassLoader();
        InputStream in = loader.getResourceAsStream("/org/vanbest/xmltv/tv_grab_nl_java.properties");
        try {
            configProp.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(int source=1; ; source++) {
        	String name = configProp.getProperty("org.vanbest.xmltv.epgsource.impl."+source);
        	if (name==null) break;
			try {
				Class<EPGSource> clazz = (Class<EPGSource>) loader.loadClass(name);
				classes.put(source,  clazz);
				Method getName=clazz.getMethod("getName");
				String sourceName=(String)getName.invoke(null);
				names.put(source,sourceName);
				ids.put(sourceName,source);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReflectiveOperationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
			constructor = classes.get(source).getConstructor(Config.class);
			return constructor.newInstance(source, config);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		Integer[] list = (Integer[]) classes.keySet().toArray();
		Arrays.sort(list);
		int[] result = new int[list.length];
		for(int i=0; i<result.length; i++) {
			result[i]=list[i];
		}
		return result;
	}
}
