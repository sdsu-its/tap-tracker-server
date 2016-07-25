package edu.sdsu.its.taptracker.Models;

import com.google.gson.annotations.Expose;

/**
 * Models a UI User
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    public String getUsername() {
        return username.toLowerCase();
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
