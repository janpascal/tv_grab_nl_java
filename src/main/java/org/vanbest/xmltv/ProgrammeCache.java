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
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

public class ProgrammeCache {
	private Connection db;
	private Config config;
	private PreparedStatement getStatement;
	private PreparedStatement putStatement;
	private PreparedStatement removeStatement;
	private PreparedStatement clearStatement;
	
	public ProgrammeCache(Config config) {
		this.config = config;
        try {
			db = DriverManager.getConnection(config.cacheDbHandle, config.cacheDbUser, config.cacheDbPassword);
			Statement stat = db.createStatement();
			stat.execute("CREATE TABLE IF NOT EXISTS cache (source INTEGER, id VARCHAR(64), date DATE, programme OTHER, PRIMARY KEY (source,id))");
			stat.close();
			
			getStatement = db.prepareStatement("SELECT programme FROM cache WHERE source=? AND id=?");
			putStatement = db.prepareStatement("INSERT INTO cache VALUES (?,?,?,?)");
			removeStatement = db.prepareStatement("DELETE FROM cache WHERE source=? AND id=?");
			clearStatement = db.prepareStatement("DELETE FROM cache");
		} catch (SQLException e) {
			db = null;
			if (!config.quiet) {
				System.out.println("Unable to open cache database, proceeding without cache");
				e.printStackTrace();
			}
		}
	}
	
	public Programme get(int source, String id) {
		if (db==null) return null;
		try {
			getStatement.setInt(1, source);
			getStatement.setString(2, id);
			ResultSet r = getStatement.executeQuery();
			if (!r.next()) return null; // not found
			try {
				Programme result = (Programme) r.getObject("programme");
				return result;
			} catch (java.sql.SQLDataException e) {
				removeCacheEntry(source, id);
				return null;
			}
		} catch (SQLException e) {
			if (!config.quiet) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	private void removeCacheEntry(int source, String id) {
		try {
			removeStatement.setInt(1, source);
			removeStatement.setString(2, id);
			removeStatement.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void put(int source, String id, Programme prog) {
		if (db == null) return;
		try {
			putStatement.setInt(1, source);
			putStatement.setString(2, id);
			putStatement.setDate(3, new java.sql.Date(prog.startTime.getTime()));
			putStatement.setObject(4, prog);
			//System.out.println(putStatement.toString());
			int count = putStatement.executeUpdate();
			if (count!=1 && !config.quiet) {
				System.out.println("Weird, cache database update statement affected " + count + " rows");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void cleanup() {
		Statement stat;
		try {
			stat = db.createStatement();
			int count = stat.executeUpdate("DELETE FROM cache WHERE date<CURRENT_DATE - 3 DAY");
			if (!config.quiet && count>0) {
				System.out.println("Purged " + count + " old entries from cache");
			}
			stat.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void clear() {
		try {
			int count = clearStatement.executeUpdate();
			if (!config.quiet && count>0) {
				System.out.println("Cleared " + count + " entries from cache");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void close() {
		cleanup();
		if (db != null) {
			try {
				getStatement.close();
				putStatement.close();
				db.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
