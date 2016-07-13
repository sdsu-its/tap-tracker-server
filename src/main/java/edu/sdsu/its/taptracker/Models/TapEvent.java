package edu.sdsu.its.taptracker.Models;

import edu.sdsu.its.taptracker.DB;

import java.sql.Timestamp;

/**
 * Models a Tap Event as Recieved by the Server and what is sent to the UI.
 * Recieved events only have the deviceID and the type defined. Outgoing events have all fields defined.
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

    public TapEvent(int deviceID, int type) {
        this.deviceID = deviceID;
        this.type = type;
    }

    public void log() {
        DB.logEvent(this);
    }

}
