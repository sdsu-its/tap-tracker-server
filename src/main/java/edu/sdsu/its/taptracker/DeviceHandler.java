package edu.sdsu.its.taptracker;

import com.google.gson.Gson;
import edu.sdsu.its.taptracker.Models.TapEvent;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.TimeZone;

/**
 * TODO JavaDoc
 *
 * @author Tom Paulus
 *         Created on 7/5/16.
 */
@Path("device")
public class DeviceHandler {
    private static final Logger LOGGER = Logger.getLogger(DeviceHandler.class);

    /**
     * TODO JDOC
     *
     * @param deviceID
     * @return
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
            counts[e.type - 1] ++;
        }

        String return_value = "";
        for (int count : counts) {
            return_value += count + " ";
        }
        return_value = return_value.trim();

        return Response.status(Response.Status.OK).entity(return_value).build();
    }

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
     * TODO JDOC
     *
     * @param payload {@link String} JSON Formatted TapEvent {@see TapEvent}
     * @return
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

    static class Device {
        int id;
        String name;

        Device(int id) {
            this.id = id;
        }

        Device(int id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return "Device{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
