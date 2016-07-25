package edu.sdsu.its.taptracker_tests;

import com.opencsv.CSVReader;
import edu.sdsu.its.taptracker.DB;
import edu.sdsu.its.taptracker.Models.Device;
import edu.sdsu.its.taptracker.Models.TapEvent;
import edu.sdsu.its.taptracker.Models.User;
import edu.sdsu.its.taptracker.Session;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Test Core UI Endpoints
 *
 * @author Tom Paulus
 *         Created on 7/13/16.
 */
public class TestUI {
    private static final Logger LOGGER = Logger.getLogger(TestUI.class);

    private static final String TESTING_USERNAME = "tester";
    private static final String TESTING_PASSWORD = "testsOften4Goodme&sure";

    private static final int TESTING_DEVICE_ID = 99;
    private static final int TESTING_DEVICE_EVENT_TYPE = 2;

    private static final int RECENT_EVENTS = 10;

    private static final String[] EXPORT_HEADERS = new String[]{"event_id", "device_id", "device_name", "type", "time"};

    private static boolean mTestUserCreated = false;
    private static boolean mTestDeviceCreated = false;

    @BeforeClass
    public static void setUp() throws Exception {
        if (DB.getUser(TESTING_USERNAME) == null) {
            LOGGER.info("Creating Admin User for test suite, since one does not yet exist.");
            mTestUserCreated = true;
            DB.createUser(new User(TESTING_USERNAME, TESTING_PASSWORD));
            LOGGER.debug("Test Account Username: " + TESTING_USERNAME);
            LOGGER.debug("Test Account Password: " + TESTING_PASSWORD);
        } else {
            LOGGER.warn("A test user already exists, some tests may fail if the password has been changed.");
        }

        if (DB.getDevice(TESTING_DEVICE_ID) == null) {
            LOGGER.info(String.format("Creating Test Device (%d), since it does not yet exist.", TESTING_DEVICE_ID));
            mTestDeviceCreated = true;
            DB.createDevice(new Device(TESTING_DEVICE_ID, "Test Device"));
            LOGGER.debug("Test Device ID: " + TESTING_DEVICE_ID);
        } else {
            LOGGER.warn(String.format("A test device already exists, some tests may fail and events will be created under the Testing Device (ID: %d)", TESTING_DEVICE_ID));
        }

        if (mTestDeviceCreated || mTestUserCreated) {
            TimeUnit.SECONDS.sleep(1); // Execute statements are executed asynchronously and can take a few seconds to execute
        }

        new TapEvent(TESTING_DEVICE_ID, TESTING_DEVICE_EVENT_TYPE).log();
        LOGGER.info("Logging new Test Event");
        LOGGER.debug("Test Event Device ID: " + TESTING_DEVICE_ID);
        LOGGER.debug("Test Event Type: " + TESTING_DEVICE_EVENT_TYPE);
        TimeUnit.SECONDS.sleep(1); // Execute statements are executed asynchronously and can take a few seconds to execute
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (mTestUserCreated) {
            LOGGER.info(String.format("Deleting Test User (\"%s\"), as it was created only for this test suite.", TESTING_USERNAME));
            DB.deleteUser(new User(TESTING_USERNAME, null));
        }

        if (mTestDeviceCreated) {
            LOGGER.info(String.format("Deleting Test Device (ID: %d), as it was created only for this test suite.", TESTING_DEVICE_ID));
            DB.deleteDevice(TESTING_DEVICE_ID);
        }
    }

    @Test
    public void getUser() throws Exception {
        User user = DB.getUser(TESTING_USERNAME);
        assertNotNull(user);
        assertTrue(user.getUsername() != null && user.getUsername().length() > 0);
        LOGGER.warn("User's Password was returned from DB");
        LOGGER.info(String.format("User's Password is \"%s\"", user.getPassword()));
    }

    @Test
    public void login() throws Exception {
        User user = DB.checkPassword(TESTING_USERNAME, TESTING_PASSWORD);
        assertTrue("Test User Password Mismatch", user != null);

        LOGGER.info("Creating new Session");
        Session session = new Session(user);
        LOGGER.debug("Session Token: " + session.getToken());
        LOGGER.debug("Token Expires: " + session.getExpires());

        LOGGER.info("Validating Session");
        User sessionUser = Session.validate(session.getToken());
        assertNotNull(sessionUser != null);
        assertEquals(sessionUser, user);
    }


    @Test
    public void events() throws Exception {
        // Events
        TapEvent[] events = DB.getEventsInRange(new int[]{TESTING_DEVICE_ID}, "1970-01-01", "2100-01-01");
        assertNotNull(events);
        assertTrue(events.length > 0);
        for (TapEvent e: events) {
            assertTrue("ID Restriction Failed", e.deviceID == TESTING_DEVICE_ID);
        }


        // Recent Events
        LOGGER.info(String.format("Fetching %d most recent events", RECENT_EVENTS));
        TapEvent[] recents = DB.getRecentEvents(RECENT_EVENTS);
        assertNotNull(recents);
        LOGGER.debug(String.format("Retrieved %d most recent events, MAX: %d", recents.length, RECENT_EVENTS));
        assertTrue("Count Restriction Failed", recents.length <= RECENT_EVENTS);
    }

    @Test
    public void devices() throws Exception {
        Device[] devices = DB.getDevices();
        assertNotNull(devices);
        LOGGER.info(String.format("Found %d devices in DB", devices.length));
        LOGGER.debug("Devices: " + Arrays.toString(devices));
        assertTrue(Arrays.asList(devices).contains(new Device(TESTING_DEVICE_ID)));
    }

    @Test
    public void export() throws Exception {
        File exportFile = DB.exportEvents(new int[]{TESTING_DEVICE_ID}, "1970-01-01", "2100-01-01");
        CSVReader reader = new CSVReader(new FileReader(exportFile));
        List csvEntries = reader.readAll();
        LOGGER.info("Testing for Headers");
        String[] headers = (String[]) Collections.singletonList(csvEntries.get(0)).get(0);
        LOGGER.debug("Headers:" + Arrays.toString((String[]) csvEntries.get(0)));

        for (String header: EXPORT_HEADERS) {
            LOGGER.debug("Checking Header for: " + header);
            assertTrue(Arrays.asList(headers).contains(header));
        }

        if (csvEntries.size() <= 1) {
            fail("No events returned in export file. File has too few rows");
        }

        boolean typeMatch = false;
        for (int r = 1; r < csvEntries.size(); r++) {
            String[] row = (String[]) csvEntries.get(r);
            LOGGER.debug("Row:"  + Arrays.toString(row));
            assertEquals("ID Restriction Failed", Integer.parseInt(row[1]), TESTING_DEVICE_ID);
            if (Integer.parseInt(row[3]) == TESTING_DEVICE_EVENT_TYPE) {
                typeMatch = true;
                break;
            }
        }

        if (!typeMatch){
            fail(String.format("Test Event not found, could not match Test Event Type (%d)", TESTING_DEVICE_EVENT_TYPE));
        }
    }
}
