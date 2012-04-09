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
	
	private final static Integer SCHEMA_VERSION=1;
	private final static String SCHEMA_KEY="TV_GRAB_NL_JAVA_SCHEMA_VERSION";
	
	public ProgrammeCache(Config config) {
		this.config = config;
        try {
			db = DriverManager.getConnection(config.cacheDbHandle, config.cacheDbUser, config.cacheDbPassword);
			/* Test for upgrade path from legacy database
			Statement stat = db.createStatement();
			System.out.println("Dropping old table");
			stat.execute("DROP TABLE IF EXISTS cache");
			System.out.println("Creating new table");
			stat.execute("CREATE CACHED TABLE IF NOT EXISTS cache (id VARCHAR(64) PRIMARY KEY, date DATE, programme OTHER)");
			*/
		} catch (SQLException e) {
			db = null;
			if (!config.quiet) {
				System.out.println("Unable to open cache database, proceeding without cache");
				if (config.logLevel>=Config.LOG_DEBUG) e.printStackTrace();
			}
        }
        boolean recreateTable = false;
        if (db != null) {
	        try {
				PreparedStatement stat = db.prepareStatement("SELECT programme FROM cache WHERE source=? AND id=?");
				stat.setInt(1, 1);
				stat.setString(2, SCHEMA_KEY);
				ResultSet result = stat.executeQuery();
				if (!result.next()) {
					if (!config.quiet) System.out.println("No schema version found in database");
					recreateTable=true;
				} else {
					Integer currentSchema = (Integer) result.getObject("programme");
					if (currentSchema<SCHEMA_VERSION) {
						if (!config.quiet) System.out.println("Current cache database schema version " + currentSchema + " is lower than my version " + SCHEMA_VERSION);
						recreateTable = true;
					} else if (currentSchema>SCHEMA_VERSION) {
						if (!config.quiet) System.out.println("Got a database schema from the future, since my version is " + SCHEMA_VERSION+ " and yours is " + currentSchema);
						recreateTable = true;
					}
					
				}
				stat.close();
			} catch (SQLException e) {
				if (!config.quiet) {
					System.out.println("Got SQL exception when trying to find current database schema");
					System.out.flush();
					if (config.logLevel>=Config.LOG_DEBUG) e.printStackTrace();
					System.out.flush();
				}
				recreateTable = true;
			}
	        if (recreateTable) {
	        	if (!config.quiet) System.out.println("Unknown cache schema, removing and recreating cache");
		        try {
					Statement stat = db.createStatement();
					// System.out.println("Dropping old table");
					stat.execute("DROP TABLE IF EXISTS cache");
					// System.out.println("Creating new table");
					stat.execute("CREATE CACHED TABLE IF NOT EXISTS cache (source INTEGER, id VARCHAR(64), date DATE, programme OTHER, PRIMARY KEY (source,id))");
					stat.close();

					// System.out.println("Writing new schema version to database");
					PreparedStatement stat2 = db.prepareStatement("INSERT INTO cache VALUES (?,?,?,?)");

					stat2.setInt(1, 1);
					stat2.setString(2, SCHEMA_KEY);
					stat2.setDate(3, new java.sql.Date(new java.util.Date(2100,11,31).getTime()));
					stat2.setObject(4, SCHEMA_VERSION);
					// System.out.println(stat2.toString());
					stat2.executeUpdate();
				} catch (SQLException e) {
					if (!config.quiet) {
						System.out.println("Unable to create cache database, proceeding without cache");
						System.out.flush();
						if (config.logLevel>=Config.LOG_DEBUG) e.printStackTrace();
						System.out.flush();
					}
					db = null;
				}
			}
	        try {
				//System.out.println("Preparing statements");
				getStatement = db.prepareStatement("SELECT programme FROM cache WHERE source=? AND id=?");
				putStatement = db.prepareStatement("INSERT INTO cache VALUES (?,?,?,?)");
				removeStatement = db.prepareStatement("DELETE FROM cache WHERE source=? AND id=?");
				clearStatement = db.prepareStatement("DELETE FROM cache");
			} catch (SQLException e) {
				if (!config.quiet) {
					System.out.println("Unable to prepare statements, proceeding without cache");
					System.out.flush();
					if (config.logLevel>=Config.LOG_DEBUG) e.printStackTrace();
					System.out.flush();
				}
				db = null;
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
				System.out.println("Error fetching programme ("+source+","+id+") from cache");
				if (config.logLevel>=Config.LOG_DEBUG) e.printStackTrace();
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
			System.out.println("Error writing programme ("+source+","+id+") to cache");
			if (config.logLevel>=Config.LOG_DEBUG) e.printStackTrace();
		}
	}

	public void cleanup() {
		if (db==null) return;
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
		if (db==null) return;
		try {
			int count = clearStatement.executeUpdate();
			if (!config.quiet && count>0) {
				System.out.println("Cleared " + count + " entries from cache");
			}
		} catch (SQLException e) {
			if (config.logLevel>=Config.LOG_DEBUG) e.printStackTrace();
		}
	}

	public void close() {
		cleanup();
		if (db != null) {
			try {
				getStatement.close();
				putStatement.close();
				removeStatement.close();
				clearStatement.close();
				db.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				if (config.logLevel>=Config.LOG_DEBUG) e.printStackTrace();
			}
		}
	}
}
