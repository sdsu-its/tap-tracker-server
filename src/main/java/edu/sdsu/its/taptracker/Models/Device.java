package edu.sdsu.its.taptracker.Models;

import java.sql.Timestamp;

/**
 * Models a Device which logs events to the server.
 *
 * @author Tom Paulus
 *         Created on 7/18/16.
 */
public class Device {
    int id;
    String name;
    Timestamp lastEvent;

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

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Device{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Device device = (Device) o;

        return id == device.id;

    }

    @Override
    public int hashCode() {
        return id;
    }
}
