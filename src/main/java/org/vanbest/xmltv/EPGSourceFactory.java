package org.vanbest.xmltv;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.NoSuchFieldException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Properties;

import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.scanners.SubTypesScanner;

import org.apache.log4j.Logger;

public class EPGSourceFactory {
    private static Map<String, Class<? extends EPGSource>> sources = new HashMap<String, Class<? extends EPGSource>>();

    private static boolean initialised = false;
    static Logger logger = Logger.getLogger(EPGSourceFactory.class);

    static void init() {
        if (initialised) return;

        Reflections reflections = new Reflections(
            ClasspathHelper.forPackage("org.vanbest.xmltv"), new SubTypesScanner());
        Set<Class<? extends EPGSource>> implementingTypes =
            reflections.getSubTypesOf(EPGSource.class);

        for(Class<? extends EPGSource> clazz: implementingTypes) {
            try {
                Field NAME;
                try {
                    NAME = clazz.getField("NAME");
                } catch (NoSuchFieldException e2) {
                    continue;
                }
                logger.debug("EPG source classname: " + NAME.toString());
                String sourceName = (String) NAME.get(null);
                sources.put(sourceName, clazz);
            } catch (Exception e) {
                logger.error("Error reading EPG Source class " + clazz, e);
            }
        }
        initialised = true;
    }

    private EPGSourceFactory() {
        init();
    }

    public static EPGSourceFactory newInstance() {
        return new EPGSourceFactory();
    }

    public EPGSource createEPGSource(Class<? extends EPGSource> source, Config config) {
        Constructor<? extends EPGSource> constructor;
        try {
            constructor = source.getConstructor(Config.class);
            return constructor.newInstance(config);
        } catch (Exception e) {
            logger.error("Error instantiating EPG source " + source, e);
        }
        return null;
    }

    public EPGSource createEPGSource(String source, Config config) {
        return createEPGSource(sources.get(source), config);
    }

    public List<EPGSource> getAll(Config config) {
        ArrayList<EPGSource> result = new ArrayList<EPGSource>();
        for(Class<? extends EPGSource> clazz: sources.values()) {
            result.add(createEPGSource(clazz, config));
        }
        return result;
    }
}
