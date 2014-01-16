package org.vanbest.xmltv;

/*
 Copyright (c) 2012-2013 Jan-Pascal van Best <janpascal@vanbest.org>

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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class ProgrammeCache.
 */
public class ProgrammeCache {

    /** The db. */
    private Connection db;

    /** The config. */
    private Config config;

    /** The get statement. */
    private PreparedStatement getStatement;

    /** The put statement. */
    private PreparedStatement putStatement;

    /** The remove statement. */
    private PreparedStatement removeStatement;

    /** The clear statement. */
    private PreparedStatement clearStatement;

    /** The clear source statement. */
    private PreparedStatement clearSourceStatement;

    /** The Constant SCHEMA_VERSION. */
    private final static Integer SCHEMA_VERSION = 3;

    /** The Constant SCHEMA_KEY_ID. */
    private final static String SCHEMA_KEY_SOURCE = "TV_GRAB_NL_JAVA";
    private final static String SCHEMA_KEY_ID = "SCHEMA_VERSION";

    /** The logger. */
    static Logger logger = Logger.getLogger(ProgrammeCache.class);

    /**
     * Instantiates a new programme cache.
     *
     * @param config the config
     */
    public ProgrammeCache(Config config) {
        this.config = config;
        try {
            db = DriverManager.getConnection(config.cacheDbHandle,
                    config.cacheDbUser, config.cacheDbPassword);
            /*
             * Test for upgrade path from legacy database Statement stat =
             * db.createStatement(); .println("Dropping old table");
             * stat.execute("DROP TABLE IF EXISTS cache");
             * System.out.println("Creating new table"); stat.execute(
             * "CREATE CACHED TABLE IF NOT EXISTS cache (id VARCHAR(64) PRIMARY KEY, date DATE, programme OTHER)"
             * );
             */
        } catch (SQLException e) {
            db = null;
            if (!config.quiet) {
                logger.warn("Unable to open cache database, proceeding without cache");
                logger.debug("Stack trace: ", e);
            }
        }
        boolean recreateTable = false;
        if (db != null) {
            try {
                PreparedStatement stat = db
                        .prepareStatement("SELECT programme FROM cache WHERE source=? AND id=?");
                stat.setString(1, SCHEMA_KEY_SOURCE);
                stat.setString(2, SCHEMA_KEY_ID);
                ResultSet result = stat.executeQuery();
                if (!result.next()) {
                    logger.debug("No schema version found in database");
                    recreateTable = true;
                } else {
                    Integer currentSchema = (Integer) result
                            .getObject("programme");
                    if (currentSchema < SCHEMA_VERSION) {
                        logger.debug("Current cache database schema version "
                                + currentSchema + " is lower than my version "
                                + SCHEMA_VERSION);
                        recreateTable = true;
                    } else if (currentSchema > SCHEMA_VERSION) {
                        logger.warn("Got a database schema from the future, since my version is "
                                + SCHEMA_VERSION
                                + " and yours is "
                                + currentSchema);
                        recreateTable = true;
                    }

                }
                stat.close();
            } catch (SQLException e) {
                if (!config.quiet) {
                    logger.warn("Got SQL exception when trying to find current database schema");
                    logger.debug("Stack trace", e);
                }
                recreateTable = true;
            }
            if (recreateTable) {
                logger.info("Unknown cache schema, removing and recreating cache");
                try {
                    Statement stat = db.createStatement();
                    logger.trace("Dropping old table");
                    stat.execute("DROP TABLE IF EXISTS cache");
                    logger.trace("Creating new table");
                    stat.execute("CREATE CACHED TABLE IF NOT EXISTS cache (source CHAR(20), id VARCHAR(128), date DATE, programme OTHER, PRIMARY KEY (source,id))");
                    stat.close();

                    logger.trace("Writing new schema version to database");
                    PreparedStatement stat2 = db
                            .prepareStatement("INSERT INTO cache VALUES (?,?,?,?)");

                    stat2.setString(1, SCHEMA_KEY_SOURCE);
                    stat2.setString(2, SCHEMA_KEY_ID);
                    stat2.setDate(3, new java.sql.Date(new java.util.Date(2100,
                            11, 31).getTime()));
                    stat2.setObject(4, SCHEMA_VERSION);
                    // System.out.println(stat2.toString());
                    stat2.executeUpdate();
                } catch (SQLException e) {
                    if (!config.quiet) {
                        logger.warn("Unable to create cache database, proceeding without cache");
                        logger.debug("stack trace: ", e);
                    }
                    db = null;
                }
            }
            try {
                // System.out.println("Preparing statements");
                getStatement = db
                        .prepareStatement("SELECT programme FROM cache WHERE source=? AND id=?");
                putStatement = db
                        .prepareStatement("INSERT INTO cache VALUES (?,?,?,?)");
                removeStatement = db
                        .prepareStatement("DELETE FROM cache WHERE source=? AND id=?");
                clearStatement = db.prepareStatement("DELETE FROM cache");
                clearSourceStatement = db
                        .prepareStatement("DELETE FROM cache WHERE source=?");
            } catch (SQLException e) {
                if (!config.quiet) {
                    logger.warn("Unable to prepare statements, proceeding without cache");
                    logger.debug("stack trace: ", e);
                }
                db = null;
            }

        }
    }

    /**
     * Gets the.
     *
     * @param source the source
     * @param id the id
     * @return the programme
     */
    public Programme get(String source, String id) {
        if (db == null)
            return null;
        try {
            getStatement.setString(1, source);
            getStatement.setString(2, id);
            ResultSet r = getStatement.executeQuery();
            if (!r.next())
                return null; // not found
            try {
                Programme result = (Programme) r.getObject("programme");
                return result;
            } catch (java.sql.SQLDataException e) {
                removeCacheEntry(source, id);
                return null;
            }
        } catch (SQLException e) {
            if (!config.quiet) {
                logger.warn("Error fetching programme (" + source + "," + id
                        + ") from cache");
                logger.debug("stack trace: ", e);
            }
            return null;
        }
    }

    /**
     * Removes the cache entry.
     *
     * @param source the source
     * @param id the id
     */
    private void removeCacheEntry(String source, String id) {
        try {
            removeStatement.setString(1, source);
            removeStatement.setString(2, id);
            removeStatement.execute();
        } catch (SQLException e) {
            logger.warn("Exception trying to remove item " + id
                    + " from source " + source + " from cache");
            logger.debug("Stack trace: ", e);
        }
    }

    /**
     * Put.
     *
     * @param source the source
     * @param id the id
     * @param prog the prog
     */
    public void put(String source, String id, Programme prog) {
        if (db == null)
            return;
        try {
            putStatement.setString(1, source);
            putStatement.setString(2, id);
            putStatement.setDate(3, new java.sql.Date(prog.startTime.getTime()));
            putStatement.setObject(4, prog);
            // System.out.println(putStatement.toString());
            int count = putStatement.executeUpdate();
            if (count != 1 && !config.quiet) {
                logger.warn("Weird, cache database update statement affected "
                        + count + " rows");
            }
        } catch (SQLException e) {
            logger.warn("Error writing programme (" + source + "," + id
                    + ") to cache");
            logger.debug("stack trace:", e);
        }
    }

    /**
     * Cleanup.
     */
    public void cleanup() {
        if (db == null)
            return;
        Statement stat;
        try {
            stat = db.createStatement();
            int count = stat
                    .executeUpdate("DELETE FROM cache WHERE date<CURRENT_DATE - 3 DAY");
            if (!config.quiet && count > 0) {
                logger.info("Purged " + count + " old entries from cache");
            }
            stat.close();
        } catch (SQLException e) {
            logger.debug("stack trace:", e);
        }
    }

    /**
     * Clear.
     */
    public void clear() {
        if (db == null)
            return;
        try {
            int count = clearStatement.executeUpdate();
            if (!config.quiet && count > 0) {
                logger.info("Cleared " + count + " entries from cache");
            }
        } catch (SQLException e) {
            logger.warn("Failed to clear cache");
            logger.debug("Stack trace: ", e);
        }
    }

    /**
     * Clear.
     *
     * @param source the source
     */
    public void clear(String source) {
        if (db == null)
            return;
        try {
            clearSourceStatement.setString(1, source);
            int count = clearSourceStatement.executeUpdate();
            if (!config.quiet && count > 0) {
                logger.info("Cleared " + count + " entries from cache");
            }
        } catch (SQLException e) {
            logger.warn("Failed to clear cache");
            logger.debug("Stack trace: ", e);
        }
    }

    /**
     * Close.
     */
    public void close() {
        cleanup();
        if (db != null) {
            try {
                getStatement.close();
                putStatement.close();
                removeStatement.close();
                clearStatement.close();
                clearSourceStatement.close();
                db.close();
            } catch (SQLException e) {
                logger.warn("Error closing cache database connection");
                logger.debug("Stack trace: ", e);
            }
        }
    }

}
