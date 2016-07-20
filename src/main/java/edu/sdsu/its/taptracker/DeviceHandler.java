package edu.sdsu.its.taptracker;

import com.google.gson.Gson;
import edu.sdsu.its.taptracker.Models.Device;
import edu.sdsu.its.taptracker.Models.TapEvent;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.TimeZone;

/**
 * Interface with the Device. POST request payloads should be JSON formatted, but the encoding header is not checked for
 * device simplicity.
 *
 * @author Tom Paulus
 *         Created on 7/5/16.
 */
@Path("device")
public class DeviceHandler {
    private static final Logger LOGGER = Logger.getLogger(DeviceHandler.class);

    /**
     * Initialize the Device. The device register's its deviceID and a device name is returned if available.
     *
     * @param deviceID {@link int} Device's Unique ID
     * @return {@link Response} Device Name if stored, else an empty string
     */
    @Path("start")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.TEXT_PLAIN)
    public Response startDevice(@QueryParam("id") final int deviceID) {
        LOGGER.info("Device Start Up: " + deviceID);
        Device device = DB.getDevice(deviceID);
        if (device == null) {
            LOGGER.info("Creating New Device - ID: " + deviceID);
            device = new Device(deviceID);
            DB.createDevice(device);

            if (device.name == null) device.name = "";
            return Response.status(Response.Status.CREATED).build();
        }
        if (device.name == null) device.name = "";
        return Response.status(Response.Status.OK).entity(device.name).build();
    }

    /**
     * Get Daily Tap Counts for the day for the specific device.
     *
     * @param deviceID {@link int} Device's Unique ID
     * @return {@link Response} Counts for the day for the device. Format: "1 2 3"
     */
    @Path("counts")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getCounts(@QueryParam("id") final int deviceID) {
        if (DB.getDevice(deviceID) == null) {
            LOGGER.warn("Unknown Device ID - ID: " + deviceID);
            return Response.status(Response.Status.UNAUTHORIZED).entity("Unknown Device. Restart Device to register with API.").build();
        }

        TapEvent[] events = DB.getDailyEventsByDevice(deviceID);
        int[] counts = new int[3];
        for (TapEvent e : events) {
            counts[e.type - 1]++;
        }

        String return_value = "";
        for (int count : counts) {
            return_value += count + " ";
        }
        return_value = return_value.trim();

        return Response.status(Response.Status.OK).entity(return_value).build();
    }

    /**
     * Get the Current Timezone Offset for the provided zone.
     * List of TimeZone IDs: https://garygregory.wordpress.com/2013/06/18/what-are-the-java-timezone-ids/
     *
     * @param zoneName {@link String} Java TimeZone ID
     * @return {@link Response} Time Zone offset as an Hour Integer
     */
    @Path("tz_offset")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.TEXT_PLAIN)
    public Response getTimeZoneOffset(@QueryParam("zone") final String zoneName) {
        TimeZone tz = TimeZone.getTimeZone(zoneName);
        int offset = tz.getOffset(new Date().getTime()) / 1000 / 60 / 60;

        return Response.status(Response.Status.OK).entity(offset).build();
    }

    /**
     * Create and Log an Event (Button Push).
     * Three different types can be sent and stored (1-3).
     *
     * @param payload {@link String} JSON Formatted TapEvent {@see TapEvent}
     * @return {@link Response} If the event was logged sucesfully
     */
    @Path("event")
    @POST
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.TEXT_PLAIN)
    public Response logEvent(final String payload) {
        Response.ResponseBuilder response;

        LOGGER.debug("Recieved Event Payload - " + payload);
        Gson gson = new Gson();
        TapEvent tapEvent = gson.fromJson(payload, TapEvent.class);
        if (tapEvent != null) {
            LOGGER.info(String.format("Recieved Tap (Type: %d) from %d", tapEvent.type, tapEvent.deviceID));
            if (DB.getDevice(tapEvent.deviceID) == null) {
                LOGGER.warn("Unknown Device ID - ID: " + tapEvent.deviceID);
                response = Response.status(Response.Status.UNAUTHORIZED).entity("Unknown Device. Restart Device to register with API.");
            } else {
                tapEvent.log();
                response = Response.status(Response.Status.OK).entity("Event Created and Logged");
            }
        } else {
            LOGGER.warn("Incomplete Event Payload - " + payload);
            response = Response.status(Response.Status.NOT_ACCEPTABLE).entity("Incomplete Event Payload");
        }
        return response.build();
    }
}
