package edu.sdsu.its.taptracker_tests;

import edu.sdsu.its.taptracker.DB;
import edu.sdsu.its.taptracker.Models.User;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Test User Methods (Create, Update, Delete)
 *
 * @author Tom Paulus
 *         Created on 7/24/16.
 */
public class TestUsers {
    private static final Logger LOGGER = Logger.getLogger(TestUsers.class);

    private static final String TESTING_USERNAME = "tester";
    private static final String TESTING_PASSWORD1 = "testsOften4Goodme&sure";
    private static final String TESTING_PASSWORD2 = "this1s&otherPassword";


    @BeforeClass
    public static void setUp() throws Exception {
        if (DB.getUser(TESTING_USERNAME) == null) {
            LOGGER.warn("Testing user already exists");
            DB.deleteUser(new User(TESTING_USERNAME, null));
        }
        LOGGER.info("Creating Test User: " + TESTING_USERNAME);
        User testUser = new User(TESTING_USERNAME, TESTING_PASSWORD1);
        DB.createUser(testUser);
        LOGGER.debug("Test Username:" + TESTING_USERNAME);
        LOGGER.debug("Test Password:" + TESTING_PASSWORD1);

        TimeUnit.SECONDS.sleep(1); // Execute statements are executed asynchronously and can take a few seconds to execute

        LOGGER.info("Testing if user was created correctly.");
        assertTrue(DB.getUser(TESTING_USERNAME).equals(testUser));
        User checkPassword = DB.checkPassword(TESTING_USERNAME, TESTING_PASSWORD1);
        assertNotNull(checkPassword);
        assertEquals(checkPassword, testUser);
        assertNull(DB.checkPassword(TESTING_USERNAME, TESTING_PASSWORD2));
    }

    @AfterClass
    public static void tearDown() throws Exception {
        LOGGER.info("Deleting Testing Account");
        DB.deleteUser(DB.getUser(TESTING_USERNAME));

        TimeUnit.SECONDS.sleep(1); // Execute statements are executed asynchronously and can take a few seconds to execute

        assertNull("User was not deleted", DB.getUser(TESTING_USERNAME));
    }

    @Test
    public void getUsers() throws Exception {
        User[] users = DB.getUsers();
        LOGGER.debug(String.format("Found %d users in DB", users.length));
        assertTrue(users.length >= 1);

        boolean testUserFound = false;
        for (User u : users) {
            if (u.equals(new User(TESTING_USERNAME, null))) {
                testUserFound = true;
                break;
            }
        }
        if (!testUserFound) {
            fail("User not found in Users List");
        }
    }

    @Test
    public void update() throws Exception {
        LOGGER.info(String.format("Changing Test User's Password to: \"%s\"", TESTING_PASSWORD2));
        User user = DB.checkPassword(TESTING_USERNAME, TESTING_PASSWORD1);
        assertNotNull(user);
        user.setPassword(TESTING_PASSWORD2);
        DB.updateUser(user);

        TimeUnit.SECONDS.sleep(1); // Execute statements are executed asynchronously and can take a few seconds to execute

        User checkPassword = DB.checkPassword(TESTING_USERNAME, TESTING_PASSWORD2);
        assertNotNull(checkPassword);
        assertEquals(checkPassword, user);
        assertNull(DB.checkPassword(TESTING_USERNAME, TESTING_PASSWORD1));
    }
}
