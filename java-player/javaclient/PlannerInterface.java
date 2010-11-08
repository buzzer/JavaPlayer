/*
 *  Player Java Client - PlannerInterface.java
 *  Copyright (C) 2005 Radu Bogdan Rusu
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * $Id: PlannerInterface.java 29 2005-11-24 12:39:51Z veedee $
 *
 */
package javaclient;

import javaclient.structures.PlayerPlannerWaypointT;

/**
 * The planner interface provides control of a 2-D motion planner.
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 * </ul>
 */
public class PlannerInterface extends PlayerDevice {

    private static final boolean isDebugging = PlayerClient.isDebugging;

    /* 2D motion planner */
    private final short PLAYER_PLANNER_CODE    = PlayerClient.PLAYER_PLANNER_CODE;
    
    /* the player message types (see player.h) */
    private static final short PLAYER_MSGTYPE_CMD = PlayerClient.PLAYER_MSGTYPE_CMD;
    private static final short PLAYER_MSGTYPE_REQ = PlayerClient.PLAYER_MSGTYPE_REQ;

    /* configuration subtypes */
    protected static final short PLAYER_PLANNER_GET_WAYPOINTS_REQ = 10;
    protected static final short PLAYER_PLANNER_ENABLE_REQ        = 11;
    /** the maximum number of waypoints in a single plan */
    public static final short PLAYER_PLANNER_MAX_WAYPOINTS     = 128;

    /* object containing the player_planner_waypoints in case of a threaded call */
    private PlayerPlannerWaypointT ppwt[] = new PlayerPlannerWaypointT[PLAYER_PLANNER_MAX_WAYPOINTS];
    private int waypoints_count           = PLAYER_PLANNER_MAX_WAYPOINTS;
    private boolean    readyPPWT          = false;
    
    private short valid;            /* Did the planner find a valid path? */
    private short done;             /* Have we arrived at the goal? */
    private int px, py, pa;         /* Current location (mm,mm,deg) */
    private int gx, gy, ga;         /* Goal location (mm,mm,deg) */
    private int wx, wy, wa;         /* Current waypoint location (mm,mm,deg) */
    private short curr_waypoint;    /* Current waypoint index (handy if you already have the list
                                        of waypoints). May be negative if there's no plan, or if 
                                        the plan is done */
    private int waypointPlan_count; /* Number of waypoints in the plan */
    
    /**
     * Constructor for PlannerInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public PlannerInterface (PlayerClient pc, short indexOfDevice) {
        super(pc);
        device    = PLAYER_PLANNER_CODE;
        index     = indexOfDevice;
    }

    /**
     * Read the planner data.
     */
    public synchronized void readData () {
        readHeader ();
        try {
            valid          = (short)is.readUnsignedByte (); /* Did the planner find a valid path? */  
            done           = (short)is.readUnsignedByte (); /* Have we arrived at the goal? */ 
            px             = is.readInt ();                 /* Current location (mm,mm,deg) */ 
            py             = is.readInt ();                 
            pa             = is.readInt ();                 
            gx             = is.readInt ();              /* Goal location (mm,mm,deg) */
            gy             = is.readInt ();
            ga             = is.readInt ();
            wx             = is.readInt ();              /* Current waypoint location (mm,mm,deg) */
            wy             = is.readInt ();
            wa             = is.readInt ();
            curr_waypoint  = is.readShort ();            /* Current waypoint index */ 
            waypointPlan_count = (int)is.readUnsignedShort ();  /* Number of waypoints in the plan */
        } catch (Exception e) {
            System.err.println ("[Planner] : Error when reading payload: " + e.toString ());
        }
    }

    /**
     * Did the planner find a valid path?
     * @return 1 if the planner found a valid path, 0 otherwise
     */
    public synchronized short getValid () { return this.valid; }

    /**
     * Have we arrived at the goal?
     * @return 1 if the goal was reached, 0 otherwise
     */
    public synchronized short getDone () { return this.done; }
    
    /**
     * Get the current location (X - mm)
     * @return the current X location in mm
     */
    public synchronized int getPx () { return this.px; }
    
    /**
     * Get the current location (Y - mm)
     * @return the current Y location in mm
     */
    public synchronized int getPy () { return this.py; }
    
    /**
     * Get the current location (A - deg)
     * @return the current A location in deg
     */
    public synchronized int getPa () { return this.pa; }
    
    /**
     * Get the goal location (X - mm)
     * @return the goal X location in mm
     */
    public synchronized int getGx () { return this.gx; }
    
    /**
     * Get the goal location (Y - mm)
     * @return the goal Y location in mm
     */
    public synchronized int getGy () { return this.gy; }
    
    /**
     * Get the goal location (A - deg)
     * @return the goal A location in deg
     */
    public synchronized int getGa () { return this.ga; }
    
    /**
     * Get the current waypoint location (X - mm)
     * @return the current waypoint X location in mm
     */
    public synchronized int getWx () { return this.wx; }
    
    /**
     * Get the waypoint current location (Y - mm)
     * @return the current waypoint Y location in mm
     */
    public synchronized int getWy () { return this.wy; }
    
    /**
     * Get the current waypoint location (A - deg)
     * @return the current waypoint A location in deg
     */
    public synchronized int getWa () { return this.wa; }
    
    /**
     * Get the current waypoint index
     * @return the current waypoint index as a short
     */
    public synchronized short getCurWaypoint () { return this.curr_waypoint; }
    
    /**
     * Get the number of waypoints in the plan
     * @return the number of waypoints in the plan as an int
     */
    public synchronized int getWaypointPlanCount () { return this.waypointPlan_count; }
    
    /**
     * Sends a new goal to the planner interface.
     * @param newgx goal location (X - mm)
     * @param newgy goal location (Y - mm)
     * @param newga goal location (A - deg)
     */
    public void setGoal (int newgx, int newgy, int newga) {
        try {
            sendHeader (PLAYER_MSGTYPE_CMD, 12);   /* 12 byte payload */
            os.writeInt (newgx);                   /* Goal location (mm,mm,deg) */
            os.writeInt (newgy);
            os.writeInt (newga);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Planner] : Couldn't send set new goals command: " +
                    e.toString ());
        }
    }

    /**
     * Configuration request: Get waypoints.
     * <br><br>
     * See the player_planner_waypoints_req structure from player.h
     */
    public void getWaypoints () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 1);     /* 1 byte payload */
            os.writeByte (PLAYER_PLANNER_GET_WAYPOINTS_REQ);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Planner] : Couldn't send PLAYER_PLANNER_GET_WAYPOINTS_REQ " +
                    "command: " + e.toString ());
        }
    }

    /**
     * Configuration request: Enable/disable robot motion.
     * <br><br>
     * See the player_planner_enable_req structure from player.h
     * @param state 1 to enable, 0 to disable
     */
    public void setRobotMotion (byte state) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 2);     /* 2 bytes payload */
            os.writeByte (PLAYER_PLANNER_ENABLE_REQ);
            os.writeByte (state);                   /* 1 to enable, 0 to disable */
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Planner] : Couldn't send PLAYER_PLANNER_ENABLE_REQ command: " + 
                    e.toString ());
        }
    }

    /**
     * Handle acknowledgement response messages (threaded mode).
     * @param size size of the payload
     */
    public void handleResponse (int size) {
        if (size == 0) {
            if (isDebugging)
            	System.err.println ("[Planner][Debug] : Unexpected response of size 0!");
            return;
        }
        try {
            /* each reply begins with a uint8_t subtype field */
            byte subtype = is.readByte ();
            switch (subtype) {
                case PLAYER_PLANNER_GET_WAYPOINTS_REQ: {
                    waypoints_count = is.readUnsignedShort();   /* number of waypoints to follow */
                    
                    for (int i = 0; i < waypoints_count; i++) {
                    	ppwt[i] = new PlayerPlannerWaypointT ();
                    	ppwt[i].setX (is.readInt ());     /* waypoint location (mm,mm,deg) */
                    	ppwt[i].setY (is.readInt ());
                    	ppwt[i].setA (is.readInt ());
                    }
                    
                    readyPPWT = true;
                    break;
                }
                case PLAYER_PLANNER_ENABLE_REQ: {
                    break;
                }
                default:{
                    System.err.println ("[Planner] : Unexpected response " + subtype + 
                            " of size = " + size);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println ("[Planner] : Error when reading payload " + e.toString ());
        }
    }

    /**
     * Get the number of waypoints to follow
     * @return number of waypoints to follow as an int
     */
    public synchronized int getWaypointCount () { return this.waypoints_count; }
    
    /**
     * Get a specified waypoint from an array of waypoints.
     * @return an object of type PlayerPlannerWaypointT containing the requested data
     */
    public synchronized PlayerPlannerWaypointT getPPWT (int i) { return ppwt[i]; }
    
    /**
     * Get the waypoints.
     * @return an array of objects of type PlayerPlannerWaypointT containing the requested data
     */
    public synchronized PlayerPlannerWaypointT[] getAllPPWT () { return ppwt; }
    
    /**
     * Check if waypoint data is available.
     * @return true if ready, false if not ready 
     */
    public synchronized boolean isReadyPPWT () {
        if (readyPPWT) {
            readyPPWT = false;
            return true;
        }
        return false;
    }

}
