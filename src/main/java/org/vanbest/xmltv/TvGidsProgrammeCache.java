package org.vanbest.xmltv;

/*
  Copyright (c) 2012 Jan-Pascal van Best <janpascal@vanbest.org>

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  The full license text can be found in the LICENSE file.
*/

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

public class TvGidsProgrammeCache {
	private File cacheFile;
	private Map<String,TvGidsProgrammeDetails> cache;
	
	public TvGidsProgrammeCache(File cacheFile) {
		this.cacheFile = cacheFile;
		if (cacheFile.canRead()) {
			try {
				cache = (Map<String,TvGidsProgrammeDetails>) new ObjectInputStream( new FileInputStream( cacheFile ) ).readObject();
			} catch (InvalidClassException e) {
				// TODO Auto-generated catch block
				cache = new HashMap<String,TvGidsProgrammeDetails>();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				cache = new HashMap<String,TvGidsProgrammeDetails>();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				cache = new HashMap<String,TvGidsProgrammeDetails>();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				cache = new HashMap<String,TvGidsProgrammeDetails>();
			}
		} else {
			cache = new HashMap<String,TvGidsProgrammeDetails>();
		}
		// FileUtils.forceMkdir(root);
	}
	
	public TvGidsProgrammeDetails getDetails(String id) {
		return cache.get(id);
	}
	
	public void add(String id, TvGidsProgrammeDetails d) {
		cache.put(id, d);
	}
	
	public void close() throws FileNotFoundException, IOException {
		new ObjectOutputStream( new FileOutputStream(cacheFile)).writeObject(cache);
	}
}
