package edu.sdsu.its.taptracker.Models;

import com.google.gson.annotations.Expose;

/**
 * TODO JavaDoc
 *
 * @author Tom Paulus
 *         Created on 7/6/16.
 */
public class User {
    @Expose
    private String username;

    @Expose(serialize = false)
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
