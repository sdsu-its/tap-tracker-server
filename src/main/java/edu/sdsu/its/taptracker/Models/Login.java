package edu.sdsu.its.taptracker.Models;

import com.google.gson.annotations.Expose;

/**
 * Models a User Logging in via the UI to obtain a session token.
 *
 * @author Tom Paulus
 *         Created on 7/6/16.
 */
public class Login {
    @Expose(deserialize = false)
    public User user;

    @SuppressWarnings("unused")
    @Expose(serialize = false)
    private String username;
    @SuppressWarnings("unused")
    @Expose(serialize = false)
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
