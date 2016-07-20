package edu.sdsu.its.taptracker.Models;

import java.sql.Timestamp;

/**
 * Models a Device which logs events to the server.
 *
 * @author Tom Paulus
 *         Created on 7/18/16.
 */
public class Device {
    public int id;
    public String name;
    public Timestamp lastEvent;

    public Device(int id) {
        this.id = id;
    }

    public Device(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public Device(int id, String name, Timestamp lastEvent) {
        this.id = id;
        this.name = name;
        this.lastEvent = lastEvent;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
