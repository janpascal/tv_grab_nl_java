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
	
	public ProgrammeCache(Config config) {
		this.config = config;
        try {
			db = DriverManager.getConnection(config.cacheDbHandle, config.cacheDbUser, config.cacheDbPassword);
			Statement stat = db.createStatement();
			stat.execute("CREATE TABLE IF NOT EXISTS cache (id VARCHAR(64) PRIMARY KEY, date DATE, programme OTHER)");
			stat.close();
			
			getStatement = db.prepareStatement("SELECT programme FROM cache WHERE id=?");
			putStatement = db.prepareStatement("INSERT INTO cache VALUES (?,?,?)");
		} catch (SQLException e) {
			db = null;
			if (!config.quiet) {
				System.out.println("Unable to open cache database, proceeding without cache");
				e.printStackTrace();
			}
		}
	}
	
	public Programme get(String id) {
		if (db==null) return null;
		try {
			getStatement.setString(1, id);
			ResultSet r = getStatement.executeQuery();
			if (!r.next()) return null; // not found
			return (Programme) r.getObject("programme");
		} catch (SQLException e) {
			if (!config.quiet) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public void put(String id, Programme prog) {
		if (db == null) return;
		try {
			putStatement.setString(1, id);
			putStatement.setDate(2, new java.sql.Date(prog.startTime.getTime()));
			putStatement.setObject(3, prog);
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
			if (!config.quiet) {
				System.out.println("Purged " + count + " old entries from cache");
			}
			stat.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void close() throws FileNotFoundException, IOException {
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
