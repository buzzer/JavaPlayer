/*
 *  Player Java Client - BumperInterface.java
 *  Copyright (C) 2003-2005 Maxim A. Batalin & Radu Bogdan Rusu
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
 * $Id: BumperInterface.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient;

import javaclient.structures.PlayerBumperGeomT;
import javaclient.structures.PlayerBumperDefineT;
/**
 * The bumper interface returns data from a bumper array. 
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class BumperInterface extends PlayerDevice {

    private static final boolean isDebugging = PlayerClient.isDebugging;
    
    private final short PLAYER_BUMPER_CODE = PlayerClient.PLAYER_BUMPER_CODE; /* bumper array */

    /* the player message types (see player.h) */
    private final short PLAYER_MSGTYPE_REQ = PlayerClient.PLAYER_MSGTYPE_REQ;

    /** maximum number of bumper samples */
    public static final int PLAYER_BUMPER_MAX_SAMPLES = 32;

    /* request types */
    protected final short PLAYER_BUMPER_GET_GEOM_REQ = 1;
    
    /* the number of valid bumper readings */
    private byte   bumperCount = PLAYER_BUMPER_MAX_SAMPLES;
    /* array of bumper values */
    private byte[] bumpers     = new byte[PLAYER_BUMPER_MAX_SAMPLES];
    
    /* object containing player_bumper_geom in case of a threaded call */
    private PlayerBumperGeomT pbgt;
    private boolean           readyPBGT = false;
    
    /**
     * Constructor for BumperInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public BumperInterface (PlayerClient pc, short indexOfDevice) {
        super (pc);
        device      = PLAYER_BUMPER_CODE;
        index       = indexOfDevice;
    }
    
    /**
     * Read the bumper values.
     */
    public synchronized void readData () {
    	try {
    	    readHeader ();
            bumperCount = is.readByte ();             /* the number of valid bumper readings  */
            for (int i = 0; i < bumperCount; i++) {
            	bumpers[i] = is.readByte ();          /* array of bumper values */
            }
        } catch (Exception e) {
        	System.err.println ("[Bumper] : Error when reading payload: " + e.toString ());
        }
    }
    
    /**
     * Configuration request: Query geometry.
     *<br><br>
     * See the player_bumper_geom structure from player.h
     */
    public void queryGeometry () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 1);        /* 1 byte payload */
            os.writeByte (PLAYER_BUMPER_GET_GEOM_REQ);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Bumper] : Couldn't send PLAYER_BUMPER_GET_GEOM_REQ command: " + 
                    e.toString ());
        }
    }
    
    /**
     * Returns the bumpers array data values up to PLAYER_BUMPER_MAX_SAMPLES.
     * @return an array filled with the bumper values
     */
    public synchronized byte[] getBumpers      ()  { return bumpers;     }
    /**
     * Returns the number of bumpers specified in the Player world file 
     * (the number of valid bumper readings). 
     * @return the number of bumpers specified in the Player world file as a byte
     */
    public synchronized byte   getBumperCount ()   { return bumperCount; }

    /**
     * Get the geometry data.
     * @return an object of type PlayerBumperGeomT containing the required geometry data
     */
    public PlayerBumperGeomT getPlayerBumperGeom () { return pbgt; }
    
    /**
     * Check if geometry data is available.
     * @return true if ready, false if not ready 
     */
    public boolean isGeomReady () {
        if (readyPBGT) {
            readyPBGT = false;
            return true;
        }
        return false;
    }
    
    /**
     * Handle acknowledgement response messages (threaded mode).
     * @param size size of the payload
     */
    public void handleResponse (int size) {
        if (size == 0) {
            if (isDebugging)
            	System.err.println ("[Bumper][Debug] : Unexpected response of size 0!");
            return;
        }
        try {
            /* each reply begins with a uint8_t subtype field */
            short subtype = is.readByte ();
            switch (subtype) {
                case PLAYER_BUMPER_GET_GEOM_REQ: {
                    pbgt             = new PlayerBumperGeomT ();
                    readyPBGT        = true;
                    pbgt.setBumperCount (is.readShort ());
                    
                    PlayerBumperDefineT[] pbgtData = new PlayerBumperDefineT[PLAYER_BUMPER_MAX_SAMPLES];
                    for (int i = 0; i < PLAYER_BUMPER_MAX_SAMPLES; i++) {
                    	pbgtData[i].setXOffset  (is.readShort ());
                        pbgtData[i].setYOffset  (is.readShort ());
                        pbgtData[i].setThOffset (is.readShort ());
                        pbgtData[i].setLength   (is.readShort ());
                        pbgtData[i].setRadius   (is.readShort ());
                    }
                    pbgt.setData (pbgtData);
            	    break;
                }
                default: {
                    System.err.println ("[Bumper] : Unexpected response " + subtype + 
                            " of size = " + size);
            	    break;
                }
            }
        } catch (Exception e) {
        	System.err.println ("[Bumper] : Error when reading payload " + e.toString ());
        }
    }
}

