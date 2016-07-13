package edu.sdsu.its.taptracker.Models;

import com.google.gson.annotations.Expose;

/**
 * TODO JavaDoc
 *
 * @author Tom Paulus
 *         Created on 7/6/16.
 */
public class Login {
    @Expose(deserialize = false)
    public User user;

    @Expose(serialize = false)
    private String username;
    @Expose(serialize = false)
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
