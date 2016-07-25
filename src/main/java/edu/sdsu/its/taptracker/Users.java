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
     * List all Users
     *
     * @param userToken {@link String} Authentication Token
     * @return {@link Response} List of Users as JSON Array
     */
    @GET
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUsers(@HeaderParam("session") final String userToken) {
        User user = Session.validate(userToken);
        Gson gson = new Gson();
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new UI.SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        LOGGER.info("Recieved to list all users from " + user.getUsername());

        return Response.status(Response.Status.OK).entity(gson.toJson(DB.getUsers())).build();
    }

    /**
     * Update a user's Password.
     * Usernames cannot be changed since they are used as the primary key in the Database.
     *
     * @param userToken {@link String} Authentication Token
     * @param payload   {@link String} UpdateUser JSON {@see User}
     * @return {@link Response} Message if operation was successful
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@HeaderParam("session") final String userToken,
                               final String payload) {

        User user = Session.validate(userToken);
        Gson gson = new Gson();
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new UI.SimpleMessage("Error", "Invalid Session Token"))).build();
        }
        LOGGER.info("Recieved to update password from " + user.getUsername());

        User updateUser = gson.fromJson(payload, User.class);
        if (updateUser != null && updateUser.getUsername() != null && updateUser.getUsername().length() > 0 &&
                updateUser.getPassword() != null && updateUser.getPassword().length() > 0) {
            DB.updateUser(updateUser);
            LOGGER.debug(String.format("\"%s\"'s password was updated successfully", updateUser.getUsername()));
            return Response.status(Response.Status.OK).entity(gson.toJson(new UI.SimpleMessage("User Updated Successfully"))).build();
        } else {
            LOGGER.warn("Incomplete User Object, Cannot Update.\n" + payload);
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson(new UI.SimpleMessage("Error", "Incomplete User Object"))).build();
        }
    }

    /**
     * Create a new user.
     * Once a user has been created, their username cannot be changed, since it is used as the primary key in the Database.
     *
     * @param userToken {@link String} Authentication Token
     * @param payload   {@link String} User JSON {@see User}
     * @return {@link Response} Message if operation was successful
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(@HeaderParam("session") final String userToken,
                               final String payload) {

        User user = Session.validate(userToken);
        Gson gson = new Gson();
        if (user == null || user.getUsername() == null || user.getPassword() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(gson.toJson(new UI.SimpleMessage("Error", "Invalid Session Token"))).build();
        }

        User createUser = gson.fromJson(payload, User.class);
        if (createUser != null && createUser.getUsername() != null && createUser.getUsername().length() > 0 &&
                createUser.getPassword() != null && createUser.getPassword().length() > 0) {
            LOGGER.info(String.format("Recieved to create user(%s) from %s", createUser.getUsername(), user.getUsername()));

            if (DB.getUser(createUser.getUsername()) != null) {
                LOGGER.warn(String.format("Username overlap, cannot create \"%s\" - Already exists in DB", createUser.getUsername()));
                return Response.status(Response.Status.BAD_REQUEST).entity(gson.toJson(new UI.SimpleMessage("Error", "Username Overlap"))).build();

            } else {
                DB.createUser(createUser);
                return Response.status(Response.Status.CREATED).build();
            }
        } else {
            LOGGER.warn("Incomplete User Object, Cannot Create.\n" + payload);
            return Response.status(Response.Status.NOT_ACCEPTABLE).entity(gson.toJson(new UI.SimpleMessage("Error", "Incomplete User Object"))).build();
        }
    }
}
