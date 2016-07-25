package edu.sdsu.its.taptracker_tests;

import edu.sdsu.its.taptracker.DB;
import edu.sdsu.its.taptracker.Models.Device;
import edu.sdsu.its.taptracker.Models.TapEvent;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test Device Handler Methods and Related DB Calls.
 *
 * @author Tom Paulus
 *         Created on 7/12/16.
 */
public class TestDevices {
    private static final Logger LOGGER = Logger.getLogger(TestDevices.class);
    private static final int DEVICE_ID = 99;
    private static final String DEVICE_NAME = "Test Device";


    @BeforeClass
    public static void setUp() throws Exception {
        LOGGER.info(String.format("Deleting all history of test device (%d)", DEVICE_ID));
        DB.deleteDevice(DEVICE_ID);

        TimeUnit.SECONDS.sleep(2); // Execute statements are executed asynchronously and can take a few seconds to execute

        LOGGER.info("Creating new Device with ID: " + DEVICE_ID);
        DB.createDevice(new Device(DEVICE_ID));

        TimeUnit.SECONDS.sleep(1); // Execute statements are executed asynchronously and can take a few seconds to execute

        logEvent();
    }


    private static void logEvent() throws Exception {
        LOGGER.info("Creating 2 test events");
        LOGGER.debug("Creating new Type 1 event for DeviceID: " + DEVICE_ID);
        DB.logEvent(new TapEvent(DEVICE_ID, 1));

        LOGGER.debug("Creating new Type 3 event for DeviceID: " + DEVICE_ID);
        DB.logEvent(new TapEvent(DEVICE_ID, 3));

        TimeUnit.SECONDS.sleep(1); // Execute statements are executed asynchronously and can take a few seconds to execute


        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");

        LOGGER.debug("Current Date: " + ft.format(dNow));

        TapEvent[] events = DB.getEventsInRange(new int[]{DEVICE_ID}, ft.format(dNow), ft.format(dNow));
        LOGGER.info(String.format("%d events found that match the deviceID from today", events.length));

        assertTrue("Results Array shorter than expected", events.length >= 2);
        assertTrue("Result Array longer than expected", events.length <= 2);

        boolean type1_found = false;
        boolean type3_found = false;
        for (TapEvent event : events) {
            if (event.type == 1) type1_found = true;
            if (event.type == 3) type3_found = true;
        }

        assertTrue("Logged Type1 Event not found", type1_found);
        assertTrue("Logged Type3 Event not found", type3_found);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        LOGGER.info(String.format("Deleting all history of test device (%d)", DEVICE_ID));
        DB.deleteDevice(DEVICE_ID);
    }

    @Test
    public void getDevice() throws Exception {
        Device device = DB.getDevice(DEVICE_ID);
        assertNotNull(String.format("Created Device (%d) could not be found", DEVICE_ID), device);
        assertTrue(String.format("Created Device (%d) could not be found", DEVICE_ID), device.getId() != 0);
    }

    @Test
    public void updateDevice() throws Exception {
        LOGGER.debug("Updating Test Device Name to \"Test Device\"");
        Device device = new Device(DEVICE_ID, DEVICE_NAME);
        DB.updateDevice(device);

        TimeUnit.SECONDS.sleep(1); // Execute statements are executed asynchronously and can take a few seconds to execute

        assertEquals("Device Name was NOT Updated", DEVICE_NAME, DB.getDevice(DEVICE_ID).getName());
    }

    @Test
    public void deviceCounts() throws Exception {
        TapEvent[] events = DB.getDailyEventsByDevice(DEVICE_ID);
        int[] counts = new int[3];
        for (TapEvent e : events) {
            counts[e.type - 1]++;
        }
        assertTrue(String.format("Count Mismatch - Expected 1 Type 1 event, found %d", counts[0]), counts[0] == 1);
        assertTrue(String.format("Count Mismatch - Expected 0 Type 2 event, found %d", counts[1]), counts[1] == 0);
        assertTrue(String.format("Count Mismatch - Expected 1 Type 3 event, found %d", counts[2]), counts[2] == 1);
    }
}
