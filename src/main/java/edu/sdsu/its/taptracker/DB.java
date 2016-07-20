package edu.sdsu.its.taptracker;

import com.opencsv.CSVWriter;
import edu.sdsu.its.taptracker.Models.Device;
import edu.sdsu.its.taptracker.Models.TapEvent;
import edu.sdsu.its.taptracker.Models.User;
import org.apache.log4j.Logger;
import org.jasypt.util.password.StrongPasswordEncryptor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Interface with the MySQL DB
 *
 * @author Tom Paulus
 *         Created on 7/5/16.
 */
public class DB {
    private static final Logger LOGGER = Logger.getLogger(DB.class);
    private static final StrongPasswordEncryptor PASSWORD_ENCRYPTOR = new StrongPasswordEncryptor();

    private static final String db_url = Param.getParam("db-url");
    private static final String db_user = Param.getParam("db-user");
    private static final String db_password = Param.getParam("db-password");

    /**
     * Create and return a new DB Connection
     * Don't forget to close the connection!
     *
     * @return {@link Connection} DB Connection
     */
    private static Connection getConnection() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(db_url, db_user, db_password);
        } catch (Exception e) {
            LOGGER.fatal("Problem Initializing DB Connection", e);
        }

        return conn;
    }

    private static void executeStatement(final String sql) {
        new Thread() {
            @Override
            public void run() {
                Statement statement = null;
                Connection connection = getConnection();

                try {
                    statement = connection.createStatement();
                    LOGGER.info(String.format("Executing SQL Statement - \"%s\"", sql));
                    statement.execute(sql);

                } catch (SQLException e) {
                    LOGGER.error("Problem Executing Statement \"" + sql + "\"", e);
                } finally {
                    if (statement != null) {
                        try {
                            statement.close();
                            connection.close();
                        } catch (SQLException e) {
                            LOGGER.warn("Problem Closing Statement", e);
                        }
                    }
                }
            }
        }.start();
    }

    private static File queryToCSV(final String sql, final String fileName) throws IOException {
        Connection connection = getConnection();
        Statement statement = null;
        CSVWriter writer = null;
        File file = null;

        try {
            statement = connection.createStatement();
            LOGGER.info(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            file = new File(System.getProperty("java.io.tmpdir") + "/" + fileName + ".csv");

            writer = new CSVWriter(new FileWriter(file));
            writer.writeAll(resultSet, true);

            resultSet.close();

        } catch (SQLException e) {
            LOGGER.error("Problem Querying DB", e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    LOGGER.warn("Problem Closing Data Dump File");
                }
            }

            if (statement != null) {
                try {
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.warn("Problem Closing Statement", e);
                }
            }
        }

        return file;
    }

    private static String sanitize(final String input) {
        return input.replace("'", "");
    }


//    Device Calls

    /**
     * Log Event ot the Database
     *
     * @param tapEvent {@link TapEvent} Event to Log
     */
    public static void logEvent(final TapEvent tapEvent) {
        executeStatement("INSERT INTO events (device_id, type, time) VALUES (" + tapEvent.deviceID + ", " + tapEvent.type + ", NOW());");
    }

    /**
     * Retrieve a Device from the Database. Will return null if the device was not found.
     *
     * @param id {@link int} Device Unique ID
     * @return {@link Device} Fetched Device
     */
    public static Device getDevice(final int id) {
        Connection connection = getConnection();
        Statement statement = null;
        Device device = null;

        try {
            statement = connection.createStatement();
            //language=SQL
            final String sql = "SELECT * FROM devices WHERE id = " + id + ";";
            LOGGER.debug(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                device = new Device(resultSet.getInt("id"), resultSet.getString("name"));
            }
            if (device != null) LOGGER.debug("Get Device Returned: " + device.toString());
            else LOGGER.debug("No Device was found with ID: " + id);

            resultSet.close();
        } catch (SQLException e) {
            LOGGER.error("Problem querying DB for DeviceID", e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.warn("Problem Closing Statement", e);
                }
            }
        }

        return device;
    }

    /**
     * Get all devices and the time of their latest event from the DB
     *
     * @return {@link Device[]} Array of Devices
     */
    public static Device[] getDevices() {
        Connection connection = getConnection();
        Statement statement = null;
        ArrayList<Device> deviceArrayList = new ArrayList<>();

        try {
            statement = connection.createStatement();
            //language=SQL
            final String sql = "SELECT\n" +
                    "  d.id AS id,\n" +
                    "  d.name AS name,\n" +
                    "  e1.time AS last_event\n" +
                    "FROM devices d\n" +
                    "  LEFT JOIN events e1 ON (d.id = e1.device_id)\n" +
                    "  LEFT OUTER JOIN events e2 ON (d.id = e2.device_id AND\n" +
                    "                                (e1.time < e2.time OR e1.time = e2.time AND e1.id < e2.id))\n" +
                    "WHERE e2.id IS NULL;";
            LOGGER.debug(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                deviceArrayList.add(new Device(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getTimestamp("last_event")));
            }
            LOGGER.debug(String.format("%d devices were found", deviceArrayList.size()));

            resultSet.close();
        } catch (SQLException e) {
            LOGGER.error("Problem querying DB for DeviceID", e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.warn("Problem Closing Statement", e);
                }
            }
        }

        return deviceArrayList.toArray(new Device[]{});
    }

    /**
     * Create Device in the DB. Device ID will not be mutable once it has been created.
     *
     * @param device {@link Device}
     */
    public static void createDevice(final Device device) {
        //language=SQL
        executeStatement("INSERT INTO devices (id, name) VALUES (" + device.id + ", '" + sanitize(device.name != null ? device.name : "") + "');");
    }

    /**
     * Update Device Name. Device ID is not mutable.
     *
     * @param device {@link Device} Updated Device
     */
    public static void updateDevice(final Device device) {
        //language=SQL
        executeStatement("UPDATE devices SET name='" + sanitize(device.name) + "' WHERE id=" + device.id + ";");
    }

    public static TapEvent[] getDailyEventsByDevice(final int deviceID) {
        Connection connection = getConnection();
        Statement statement = null;
        ArrayList<TapEvent> tapEventArrayList = new ArrayList<>();

        try {
            statement = connection.createStatement();
            //language=SQL
            final String sql = "SELECT\n" +
                    "  `id`,\n" +
                    "  `type`\n" +
                    "FROM events\n" +
                    "WHERE `device_id` = " + deviceID + " AND\n" +
                    "      DATE(`time`) = CURDATE();";
            LOGGER.debug(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                TapEvent event = new TapEvent(
                        resultSet.getInt("id"),
                        resultSet.getInt("type"));

                tapEventArrayList.add(event);
            }
            LOGGER.debug(String.format("Retrieved %d Tap Events for Today for Device: " + deviceID, tapEventArrayList.size()));
            resultSet.close();
        } catch (SQLException e) {
            LOGGER.error("Problem querying DB for Daily events by Device ID", e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.warn("Problem Closing Statement", e);
                }
            }
        }

        return tapEventArrayList.toArray(new TapEvent[]{});
    }

//  UI Calls

    /**
     * Check if the users credentials are valid, and return the user.
     *
     * @param username {@link String} Username
     * @param password {@link String} User's Password
     * @return {@link User} User if credentials are valid, Null otherwise
     */
    static User checkPassword(final String username, final String password) {
        User user = getUser(username);

        if (user == null) {
            LOGGER.warn("User Login Failed - User does not exist");
            return null;
        }
        if (password == null || user.getPassword() == null) {
            LOGGER.warn("User Login Failed - Null Password");
            return null;
        }
        if (!PASSWORD_ENCRYPTOR.checkPassword(password, user.getPassword())) {
            LOGGER.warn("User Login Failed - Password Mismatch");
            LOGGER.info(String.format("Password does not match for username: \"%s\"", username));
            return null;
        }
        return user;
    }

    /**
     * Retrieve a User and their Password Hash from the DB via their username
     *
     * @param username {@link String} Username
     * @return {@link User} User Object (Username and Password)
     */
    static User getUser(final String username) {
        Connection connection = getConnection();
        Statement statement = null;
        User user = null;

        try {
            statement = connection.createStatement();
            //language=SQL
            final String sql = "SELECT * FROM users WHERE username='" + username + "';";
            LOGGER.debug(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            if (resultSet.next()) {
                user = new User(resultSet.getString("username"), resultSet.getString("password"));
            }
            if (user == null) LOGGER.debug("No User was found with Username: " + username);

            resultSet.close();
        } catch (SQLException e) {
            LOGGER.error("Problem querying DB for User", e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.warn("Problem Closing Statement", e);
                }
            }
        }

        return user;
    }

    static User[] getUsers() {
        Connection connection = getConnection();
        Statement statement = null;
        ArrayList<User> userArrayList = new ArrayList<>();

        try {
            statement = connection.createStatement();
            //language=SQL
            final String sql = "SELECT * FROM users ORDER BY username ASC;";
            LOGGER.debug(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                userArrayList.add(new User(resultSet.getString("username"), null));
            }
            LOGGER.debug(String.format("Found %d user account", userArrayList.size()));

            resultSet.close();
        } catch (SQLException e) {
            LOGGER.error("Problem querying DB for User", e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.warn("Problem Closing Statement", e);
                }
            }
        }

        return userArrayList.toArray(new User[]{});
    }

    /**
     * Update a UI User. Only passwords can be changed, since the username is used as the primary key.
     *
     * @param user {@link User} User to Update with Updated Fields
     * @return {@link User} Updated User
     */
    static User updateUser(final User user) {
        user.setPassword(PASSWORD_ENCRYPTOR.encryptPassword(user.getPassword()));

        //language=SQL
        executeStatement("UPDATE users SET password='" + user.getPassword() + "' WHERE username='" + user.getUsername() + "';");
        return user;
    }

    /**
     * Create a UI User. Once created, the user's username cannot be changed.
     *
     * @param user {@link User} User to Create
     * @return {@link User} Crated User
     */
    static User createUser(final User user) {
        user.setPassword(PASSWORD_ENCRYPTOR.encryptPassword(user.getPassword()));

        //language=SQL
        executeStatement("INSERT INTO users (username, password) VALUES ('" + user.getUsername() + "', '" + user.getPassword() + "');");
        return user;
    }

    /**
     * Get the n most recent events from the DB.
     *
     * @param count {@link int} Number of Events to Fetch MAX
     * @return {@link TapEvent[]} Array of Events, up to count in length
     */
    public static TapEvent[] getRecentEvents(final int count) {
        Connection connection = getConnection();
        Statement statement = null;
        ArrayList<TapEvent> tapEventArrayList = new ArrayList<>(count);

        try {
            statement = connection.createStatement();
            //language=SQL
            final String sql = "SELECT\n" +
                    "  e.id   AS `event_id`,\n" +
                    "  d.id   AS `device_id`,\n" +
                    "  d.name AS `device_name`,\n" +
                    "  type,\n" +
                    "  time\n" +
                    "FROM events e LEFT OUTER JOIN devices d ON d.id = e.device_id\n" +
                    "ORDER BY time DESC\n" +
                    "LIMIT " + count + ";";
            LOGGER.debug(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                TapEvent event = new TapEvent(
                        resultSet.getInt("event_id"),
                        resultSet.getInt("device_id"),
                        resultSet.getString("device_name"),
                        resultSet.getInt("type"),
                        resultSet.getTimestamp("time"));

                tapEventArrayList.add(event);
            }
            LOGGER.debug(String.format("Retrieved %d most recent Tap Events", tapEventArrayList.size()));
            resultSet.close();
        } catch (SQLException e) {
            LOGGER.error("Problem querying DB for Recent Events", e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.warn("Problem Closing Statement", e);
                }
            }
        }

        return tapEventArrayList.toArray(new TapEvent[]{});
    }

    /**
     * Export Events to a CSV. The File returned is a temporary file and the information should be moved to a permanent
     * location if being stored for any amount of time that extends beyond the scope of the session.
     * <p>
     * Date Format for Start/End Date: YYYY-MM-DD
     *
     * @param devices {@link int[]} List of Device IDs to filter. If blank, all IDs will be selected.
     * @param start   {@link String} Start Date (YYYY-MM-DD)
     * @param end     {@link String} End Date, inclusive (YYYY-MM-DD)
     * @return {@link File} CSV with events meeting the specified parameters
     */
    public static File exportEvents(final int[] devices, final String start, final String end) throws IOException {
        String idRestriction = "";
        if (devices != null && devices.length != 0) {
            idRestriction = "AND ";
            for (int i : devices) {
                idRestriction += "device_id=" + i + " OR ";
            }
            idRestriction = idRestriction.substring(0, idRestriction.length() - 4); // Remove the last or and extra spaces.
        }

        //language=SQL
        String query = "SELECT\n" +
                "  e.id   AS `event_id`,\n" +
                "  d.id   AS `device_id`,\n" +
                "  d.name AS `device_name`,\n" +
                "  type,\n" +
                "  time\n" +
                "FROM events e LEFT OUTER JOIN devices d ON d.id = e.device_id\n" +
                "WHERE e.time BETWEEN '" + start + "' AND\n" +
                "  DATE_ADD('" + end + "', INTERVAL 1 DAY)\n" +
                idRestriction + "\n" +
                "ORDER BY time DESC;";

        return queryToCSV(query, String.format("csv-export-%s-%s", start, end));
    }

    /**
     * Export events to Array for Serialization.
     * <p>
     * Date Format for Start/End Date: YYYY-MM-DD
     *
     * @param devices {@link int[]} List of Device IDs to filter. If blank, all IDs will be selected.
     * @param start   {@link String} Start Date (YYYY-MM-DD)
     * @param end     {@link String} End Date, inclusive (YYYY-MM-DD)
     * @return {@link TapEvent[]} Matching Events
     */
    public static TapEvent[] getEventsInRange(final int[] devices, final String start, final String end) {
        Connection connection = getConnection();
        Statement statement = null;
        ArrayList<TapEvent> tapEventArrayList = new ArrayList<>();

        String idRestriction = "";
        if (devices != null && devices.length != 0) {
            idRestriction = "AND ";
            for (int i : devices) {
                idRestriction += "device_id=" + i + " OR ";
            }
            idRestriction = idRestriction.substring(0, idRestriction.length() - 4); // Remove the last or and extra spaces.
        }

        try {
            statement = connection.createStatement();
            //language=SQL
            String sql = "SELECT\n" +
                    "  e.id   AS `event_id`,\n" +
                    "  d.id   AS `device_id`,\n" +
                    "  d.name AS `device_name`,\n" +
                    "  type,\n" +
                    "  time\n" +
                    "FROM events e LEFT OUTER JOIN devices d ON d.id = e.device_id\n" +
                    "WHERE e.time BETWEEN '" + start + "' AND\n" +
                    "  DATE_ADD('" + end + "', INTERVAL 1 DAY)\n" +
                    idRestriction + "\n" +
                    "ORDER BY time DESC;";
            LOGGER.debug(String.format("Executing SQL Query - \"%s\"", sql));
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                TapEvent event = new TapEvent(
                        resultSet.getInt("event_id"),
                        resultSet.getInt("device_id"),
                        resultSet.getString("device_name"),
                        resultSet.getInt("type"),
                        resultSet.getTimestamp("time"));

                tapEventArrayList.add(event);
            }
            LOGGER.debug(String.format("Retrieved %d Tap Events in range between %s and %s", tapEventArrayList.size(), start, end));
            resultSet.close();
        } catch (SQLException e) {
            LOGGER.error("Problem querying DB for Recent Events", e);
        } finally {
            if (statement != null) {
                try {
                    statement.close();
                    connection.close();
                } catch (SQLException e) {
                    LOGGER.warn("Problem Closing Statement", e);
                }
            }
        }

        return tapEventArrayList.toArray(new TapEvent[]{});
    }

    // Unit Tests

    /**
     * Delete all events associated with a device and remove the device form the DB.
     * Use with extreme caution since this cannot be undone.
     *
     * @param deviceID {@link int} Device ID
     * @throws InterruptedException A delay is used to ensure that the sequence of the commands is correct, this should
     *                              not be interrupted if possible.
     */
    public static void deleteDevice(final int deviceID) throws InterruptedException {
        LOGGER.warn("Deleting ALL events and device information for DeviceID: " + deviceID);

        //language=SQL
        executeStatement("DELETE FROM events WHERE `device_id` = " + deviceID + ";");

        TimeUnit.SECONDS.sleep(1); // Execute statements are executed asynchronously and can take a few seconds to execute

        //language=SQL
        executeStatement("DELETE FROM devices WHERE `id` = " + deviceID + ";");

        TimeUnit.SECONDS.sleep(1); // Execute statements are executed asynchronously and can take a few seconds to execute
    }

    public static void main(String[] args) {
        System.out.println("Create First User for Checkout");

        final Scanner scanner = new Scanner(System.in);
        System.out.println("Username:");
        final String username = scanner.next();

        System.out.println("Password:");
        final String password = scanner.next();

        User user = new User(username, password);
        createUser(user);
    }
}

