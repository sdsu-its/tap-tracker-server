package edu.sdsu.its.taptracker;

import com.google.gson.Gson;
import edu.sdsu.its.taptracker.Models.Device;
import edu.sdsu.its.taptracker.Models.User;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Device Management Endpoints.
 *
 * @author Tom Paulus
 *         Created on 7/20/16.
 */
@Path("device")
public class Devices {
    private static final Logger LOGGER = Logger.getLogger(Devices.class);

    /**
     * Update a Device. The DeviceID cannot be updated, because it is used as the Primary Key in the DB
     *
     * @param userToken {@link String} Authentication Token
     * @param payload   {@link String} Device JSON {@see Device}
     * @return {@link Response} Message on status of operation as JSON
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateDevice(@HeaderParam("session") final String userToken,
                                 final String payload) {
        User user = Session.validate(userToken);
        Gson gson = new Gson();
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new UI.SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        LOGGER.info("Recieved to update device from " + user.getUsername());

        Device device = gson.fromJson(payload, Device.class);
        if (device != null && device.getId() != 0) {
            if (DB.getDevice(device.getId()) == null) {
                LOGGER.warn(String.format("Unknown Device ID (%d), Cannot Update", device.getId()));
                return Response.status(Response.Status.BAD_GATEWAY).entity(gson.toJson(new UI.SimpleMessage("Error", "Unknown Device ID. You may need to create the device first."))).build();
            } else {
                DB.updateDevice(device);
                return Response.status(Response.Status.ACCEPTED).entity(gson.toJson(new UI.SimpleMessage("Updated Device Successfully"))).build();
            }
        } else {
            LOGGER.warn("Incomplete Device Object, Cannot Update\n" + payload);
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson(new UI.SimpleMessage("Error", "Incomplete Device Object"))).build();
        }
    }

    /**
     * Create a device. Once created, the deviceID cannot be changed.
     *
     * @param userToken {@link String} Authentication Token
     * @param payload   {@link String} Device JSON {@see Device}
     * @return {@link Response} Message on status of operation as JSON
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createDevice(@HeaderParam("session") final String userToken,
                                 final String payload) {
        User user = Session.validate(userToken);
        Gson gson = new Gson();
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new UI.SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        LOGGER.info("Recieved to create device from " + user.getUsername());

        Device device = gson.fromJson(payload, Device.class);

        if (device != null && device.getId() != 0) {
            if (DB.getDevice(device.getId()) != null) {
                LOGGER.warn(String.format("Device ID Overlap, Cannot create device with ID: %d, already exists", device.getId()));
                return Response.status(Response.Status.BAD_GATEWAY).entity(gson.toJson(new UI.SimpleMessage("Error", "Overlapping Device ID, that ID already exists."))).build();
            } else {
                DB.createDevice(device);
                return Response.status(Response.Status.CREATED).entity(gson.toJson(new UI.SimpleMessage("Device Created Successfully."))).build();
            }
        } else {
            LOGGER.warn("Incomplete Device Object, Cannot Create\n" + payload);
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson(new UI.SimpleMessage("Error", "Incomplete Device Object"))).build();
        }
    }

}
