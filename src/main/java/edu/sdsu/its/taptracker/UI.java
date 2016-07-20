package edu.sdsu.its.taptracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import edu.sdsu.its.taptracker.Models.Login;
import edu.sdsu.its.taptracker.Models.User;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Front End/User Interface Endpoints.
 * All requests, except login, require a "session" header to be transmitted with the request for authentication
 *
 * @author Tom Paulus
 *         Created on 7/6/16.
 */
@Path("ui")
public class UI {
    private static final Logger LOGGER = Logger.getLogger(UI.class);
    private static final int DEFAULT_COUNT = 25;


    /**
     * Login a User. Uses their username and password to verify their identity and retrieve their user information.
     * Returns a Login Object with their User Information and Session Token.
     *
     * @param payload {@link String} JSON Formatted Login Object {@see Models.Login}
     * @return {@link Response} Login JSON (With User and Session Information)
     */
    @Path("login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(final String payload) {
        final GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        final Gson gson = builder.create();

        LOGGER.debug("Recieved Login Request. Payload - \n" + payload);
        Login login = gson.fromJson(payload, Login.class);
        LOGGER.debug("Login Requested for User: " + login.getUsername());
        Response.ResponseBuilder response;

        User user = DB.checkPassword(login.getUsername(), login.getPassword());
        if (user != null) {
            LOGGER.debug("Valid User - " + user.getUsername());
            login.user = user;
            Session session = new Session(user);
            LOGGER.debug(String.format("Session token for \"%s\" = \"%s\"", login.getUsername(), session.getToken()));

            response = Response.status(Response.Status.OK).entity(gson.toJson(login)).header("session", session.getToken());
        } else {
            response = Response.status(Response.Status.FORBIDDEN).entity(gson.toJson(new SimpleMessage("Error", "Username or Password are incorrect")));
        }

        return response.build();
    }

    /**
     * Fetch recent events from the DB.
     *
     * @param userToken {@link String} Authentication Token
     * @param count     {@link int} Maximum number of events to fetch. 25 if left blank or 0.
     * @return {@link Response} Array of TapEvents {@see edu.sdsu.its.taptracker.Models.TapEvent}
     */
    @Path("recent_events")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRecentEvents(@HeaderParam("session") final String userToken,
                                    @QueryParam("count") int count) {
        User user = Session.validate(userToken);
        Gson gson = new Gson();
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        LOGGER.info("Recieved request for recent events from " + user.getUsername());
        if (count == 0) {
            LOGGER.warn("No count was specified for recent events, using default");
            count = DEFAULT_COUNT;
        }

        return Response.status(Response.Status.OK).entity(gson.toJson(DB.getRecentEvents(count))).build();

    }

    /**
     * Get events in a given time range.
     * Useful for Analytics.
     *
     * @param userToken {@link String} Authentication Token
     * @param startDate {@link String} Start Date (YYYY-MM-DD)
     * @param endDate   {@link String} End Date (YYYY-MM-DD)
     * @return {@link Response} Array of TapEvents {@see edu.sdsu.its.taptracker.Models.TapEvent}
     */
    @Path("events")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEventsForRange(@HeaderParam("session") final String userToken,
                                      @QueryParam("start") final String startDate,
                                      @QueryParam("end") final String endDate) {
        User user = Session.validate(userToken);
        Gson gson = new Gson();
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        LOGGER.info(String.format("Recieved request for events in range (%s - %s) from %s", startDate, endDate, user.getUsername()));

        return Response.status(Response.Status.OK).entity(gson.toJson(DB.getEventsInRange(new int[]{}, startDate, endDate))).build();
    }

    /**
     * Get all registered devices
     *
     * @param userToken {@link String} Authentication Token
     * @return {@link Response} Array of all Registered Devices and their Name and last event
     */
    @Path("devices")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevices(@HeaderParam("session") final String userToken) {
        User user = Session.validate(userToken);
        Gson gson = new Gson();
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        LOGGER.info(String.format("Recieved request for all devices from %s", user.getUsername()));

        return Response.status(Response.Status.OK).entity(gson.toJson(DB.getDevices())).build();
    }


    /**
     * Export a CSV of events that meet specified device and date parameters.
     *
     * @param userToken {@link String} Authentication Token
     * @param deviceIDs {@link String} List of Device IDs to filter. If blank, all IDs will be selected.
     * @param startDate {@link String} Start Date (YYYY-MM-DD)
     * @param endDate {@link String} End Date (YYYY-MM-DD)
     * @return {@link Response} CSV File Download
     */
    @Path("csv_export")
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces("text/csv")
    public Response exportCSV(@HeaderParam("session") final String userToken,
                              @QueryParam("ids") final String deviceIDs,
                              @QueryParam("start") final String startDate,
                              @QueryParam("end") final String endDate) {

        User user = Session.validate(userToken);
        Gson gson = new Gson();
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        LOGGER.info("Recieved request for CSV Export from " + user.getUsername());
        LOGGER.debug(String.format("Generating CSV Export report for:\n" +
                "IDs: %s\n" +
                "Start: %s\n" +
                "End: %s", deviceIDs, startDate, endDate));

        try {
            File exportFile = DB.exportEvents(gson.fromJson(deviceIDs, int[].class), startDate, endDate);
            return Response.status(Response.Status.OK).entity(new FileInputStream(exportFile)).build();
        } catch (IOException e) {
            LOGGER.error("Problem Exporting CSV File", e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(new SimpleMessage("Error", "Problem Generating Export CSV"))).build();
        }
    }


    static class SimpleMessage {
        private String status = null;
        private String message;

        public SimpleMessage(String message) {
            this.message = message;
        }

        public SimpleMessage(String status, String message) {
            this.status = status;
            this.message = message;
        }
    }
}
