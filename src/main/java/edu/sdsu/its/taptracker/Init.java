package edu.sdsu.its.taptracker;

import edu.sdsu.its.taptracker.Models.User;
import org.apache.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * Initialize and Teardown the WebApp and DB
 *
 * @author Tom Paulus
 *         Created on 7/23/16.
 */
@WebListener
public class Init implements ServletContextListener {
    private static final Logger LOGGER = Logger.getLogger(Init.class);
    private static final String DEFAULT_USERNAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";


    /**
     * Initialize the Webapp with the Default User if no users exist.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        User[] users = DB.getUsers();
        LOGGER.debug(String.format("Starting Webapp. Found %d users in DB", users.length));
        if (users.length == 0) {
            LOGGER.info("No users were found in the DB. Creating default User.");
            User user = new User(DEFAULT_USERNAME, DEFAULT_PASSWORD);
            DB.createUser(user);

            LOGGER.info(String.format("Initial User Created. Username: \"%s\" Password: \"%s\"", DEFAULT_USERNAME, DEFAULT_PASSWORD));
        }

        LOGGER.debug("Initializing Session Validator");
        new Session();
    }

    /**
     * Deregister DB Driver to prevent memory leaks.
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        // Loop through all drivers
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == cl) {
                // This driver was registered by the webapp's ClassLoader, so deregister it:
                try {
                    LOGGER.info(String.format("Deregistering JDBC driver: %s", driver));
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException ex) {
                    LOGGER.fatal(String.format("Error deregistering JDBC driver: %s", driver), ex);
                }
            } else {
                // driver was not registered by the webapp's ClassLoader and may be in use elsewhere
                LOGGER.info(String.format("Not deregistering JDBC driver %s as it does not belong to this webapp's ClassLoader", driver));
            }
        }
    }
}
