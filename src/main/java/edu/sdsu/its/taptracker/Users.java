package edu.sdsu.its.taptracker;

import com.google.gson.Gson;
import edu.sdsu.its.taptracker.Models.User;
import org.apache.log4j.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * User Management Endpoints
 *
 * @author Tom Paulus
 *         Created on 7/19/16.
 */
@Path("user")
public class Users {
    private static final Logger LOGGER = Logger.getLogger(Users.class);

    /**
     * Update a user's Password.
     * Usernames cannot be changed since they are used as the primary key in the Database.
     *
     * @param userToken {@link String} Authentication Token
     * @param payload {@link String} UpdateUser JSON {@see Users.updatePayload}
     * @return {@link Response} Message if operation was successful
     */
    @Path("update")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@HeaderParam("session") final String userToken,
                               final String payload){

        User user = Session.validate(userToken);
        Gson gson = new Gson();
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new UI.SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        LOGGER.info("Recieved to update password from " + user.getUsername());

        updatePayload updatePayload = gson.fromJson(payload, Users.updatePayload.class);

        User updateUser = DB.checkPassword(updatePayload.username, updatePayload.currentPassword);
        if (updateUser == null) {
            LOGGER.info("Incorrect Current password for user: " + updatePayload.username);
            return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new UI.SimpleMessage("Current Password not correct."))).build();
        }

        updateUser.setPassword(updatePayload.newPassword);
        DB.updateUser(updateUser);
        LOGGER.debug(String.format("\"%s\"'s password was updated successfully", updatePayload.username));

        return Response.status(Response.Status.OK).entity(gson.toJson(new UI.SimpleMessage("User Updated Successfully"))).build();
    }

    /**
     * Create a new user.
     * Once a user has been created, their username cannot be changed, since it is used as the primary key in the Database.
     *
     * @param userToken {@link String} Authentication Token
     * @param payload {@link String} User JSON {@see User}
     * @return {@link Response} Message if operation was successful
     */
    @Path("create")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(@HeaderParam("session") final String userToken,
                               final String payload) {
        // TODO
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    private static class updatePayload{
        public String username;
        String currentPassword;
        String newPassword;
    }

}
