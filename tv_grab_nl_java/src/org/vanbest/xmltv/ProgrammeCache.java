package org.vanbest.xmltv;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class ProgrammeCache {
	private File cacheFile;
	private Map<String,ProgrammeDetails> cache;
	
	public ProgrammeCache(File cacheFile) {
		this.cacheFile = cacheFile;
		if (cacheFile.canRead()) {
			try {
				cache = (Map<String,ProgrammeDetails>) new ObjectInputStream( new FileInputStream( cacheFile ) ).readObject();
			} catch (InvalidClassException e) {
				// TODO Auto-generated catch block
				
				cache = new HashMap<String,ProgrammeDetails>();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				cache = new HashMap<String,ProgrammeDetails>();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				cache = new HashMap<String,ProgrammeDetails>();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				cache = new HashMap<String,ProgrammeDetails>();
			}
		} else {
			cache = new HashMap<String,ProgrammeDetails>();
		}
		// FileUtils.forceMkdir(root);
	}
	
	public ProgrammeDetails getDetails(String id) {
		return cache.get(id);
	}
	
	public void add(String id, ProgrammeDetails d) {
		cache.put(id, d);
	}
	
	public void close() throws FileNotFoundException, IOException {
		new ObjectOutputStream( new FileOutputStream(cacheFile)).writeObject(cache);
	}
}
