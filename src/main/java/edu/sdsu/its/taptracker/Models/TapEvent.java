package edu.sdsu.its.taptracker.Models;

import edu.sdsu.its.taptracker.DB;

import java.sql.Timestamp;

/**
 * TODO JavaDoc
 *
 * @author Tom Paulus
 *         Created on 7/6/16.
 */
public class TapEvent {
    public int id;
    public int deviceID;
    public String deviceName;
    public int type;
    public Timestamp time;

    public TapEvent(int id, int deviceID, String deviceName, int type, Timestamp time) {
        this.id = id;
        this.deviceID = deviceID;
        this.deviceName = deviceName;
        this.type = type;
        this.time = time;
    }

    public TapEvent(int id, int type) {
        this.id = id;
        this.type = type;
    }

    public void log() {
        DB.logEvent(this);
    }

}
