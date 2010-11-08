/*
 *  Player Java Client - FiducialInterface.java
 *  Copyright (C) 2002-2005 Maxim A. Batalin & Radu Bogdan Rusu
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
 * $Id: FiducialInterface.java 27 2005-08-11 15:44:48Z veedee $
 *
 */
package javaclient;

import javaclient.structures.PlayerFiducialItem;

/**
 * The fiducial interface provides access to devices that detect coded fiducials 
 * (markers) placed in the environment. It can also be used for devices the 
 * detect natural landmarks.
 * @author Maxim A. Batalin, Radu Bogdan Rusu & Moshe Sayag
 * @version
 * <ul>
 *      <li>v1.6.4 - Player 1.6.4 bug fixes
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class FiducialInterface extends PlayerDevice {

    private static final boolean isDebugging = PlayerClient.isDebugging;

    /* fiducial detector */
    private final short PLAYER_FIDUCIAL_CODE = PlayerClient.PLAYER_FIDUCIAL_CODE;

    /* the player message types (see player.h) */
    private static final short PLAYER_MSGTYPE_REQ = PlayerClient.PLAYER_MSGTYPE_REQ;

    /** The maximum number of fiducials that can be detected at one time */ 
    public static final short PLAYER_FIDUCIAL_MAX_SAMPLES = 32;
    /** The maximum size of a data packet exchanged with a fiducial at one time */ 
    public static final short PLAYER_FIDUCIAL_MAX_MSG_LEN = 32;

    /* request packet subtypes */ 
    protected final short PLAYER_FIDUCIAL_GET_GEOM     = 1;
    protected final short PLAYER_FIDUCIAL_GET_FOV      = 2;
    protected final short PLAYER_FIDUCIAL_SET_FOV      = 3;
    protected final short PLAYER_FIDUCIAL_SEND_MSG     = 4;
    protected final short PLAYER_FIDUCIAL_RECV_MSG     = 5;
    protected final short PLAYER_FIDUCIAL_EXCHANGE_MSG = 6;
    protected final short PLAYER_FIDUCIAL_GET_ID       = 7;
    protected final short PLAYER_FIDUCIAL_SET_ID       = 8;

    /* the number of detected fiducials */
    private int count; 
    /* list of detected fiducials */
    private PlayerFiducialItem[] fiducials = new PlayerFiducialItem[PLAYER_FIDUCIAL_MAX_SAMPLES];
    
    /* pose of the detector in the robot cs (x, y, orient) in units if (mm, mm, degrees) */ 
    private int[] detectPose    = new int[3];
    /* size of the detector in units of (mm, mm) */
    private int[] detectSize    = new int[2];
    /* dimensions of the fiducials in units of (mm, mm) */
    private int[] fiducial_size = new int[2];
    
    private boolean geometryReady, fovReady, idReady;
    
    /* the minimum and maximum ranges of the sensor in mm */
    private int minRange, maxRange;
    /* the receptive angle of the sensor in degrees */
    private int viewAngle;
    
    private int id;     /* the value displayed */
    
    /**
     * Constructor for FiducialInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public FiducialInterface (PlayerClient pc, short indexOfDevice) {
        super(pc);
        device    = PLAYER_FIDUCIAL_CODE;
        index     = indexOfDevice;
        
        // create the new items
        for (int i = 0; i < fiducials.length; i++) {
            fiducials[i] = new PlayerFiducialItem();
        }
    }

    /**
     * Read the fiducial data packet (all fiducials).
     */
    public synchronized void readData () {
        readHeader ();
        try {
            count = is.readUnsignedShort ();        /* the number of detected fiducials */ 
            for (int i = 0; i < count; i++) {
                /* the fiducial id */
                fiducials[i].setID (is.readShort ());
                
                int[] pos  = new int[3];
                for (int j = 0; j < 3; j++) {
                    pos[j] = is.readInt ();         /* fiducial position */
                }
                fiducials[i].setPos (pos);
                
                int[] rot  = new int[3];
                for (int j = 0; j < 3; j++)
                    rot[j] = is.readInt ();         /* fiducial orientation */
                fiducials[i].setRot (rot);
                
                int[] upos = new int[3];
                for (int j = 0; j < 3; j++)
                    upos[j] = is.readInt ();        /* uncertainty in the measured pose */
                fiducials[i].setUPos (upos);
                
                int[] urot = new int[3];
                for (int j = 0; j < 3; j++)
                    urot[j] = is.readInt ();        /* uncertainty in fiducial orientation */
                fiducials[i].setURot (urot);
            }
        } catch (Exception e) {
            System.err.println ("[Fiducial] : Error when reading payload: " + e.toString ());
        }
    }
   
    /**
     * Configuration request: Get geometry.
     * <br><br>
     * See the player_fiducial_geom structure from player.h
     */
    public void getGeometry () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 1);     /* 1 byte payload */
            os.writeByte (PLAYER_FIDUCIAL_GET_GEOM);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Fiducial] : Couldn't send PLAYER_FIDUCIAL_GET_GEOM " +
                    "command: " + e.toString ());
        }
    }

    /**
     * Handle acknowledgement response messages (threaded mode).
     * @param size size of the payload
     */
    public void handleResponse (int size) {
        if (size == 0) {
            if (isDebugging)
                System.err.println ("[Fiducial][Debug] : Unexpected response of size 0!");
            return;
        }
        try {
            /* each reply begins with a uint8_t subtype field */
            byte subtype = is.readByte ();
            switch (subtype) {
                case PLAYER_FIDUCIAL_GET_GEOM: {
                    /* pose of the detector in the robot cs (x, y, orient) 
                     * in units if (mm, mm, degrees) */ 
                    for (int i = 0; i < 3; i++)
                        detectPose[i] = is.readShort ();
                    
                    /* size of the detector in units of (mm, mm) */
                    for (int i = 0; i < 2; i++)
                        detectSize[i] = is.readUnsignedShort ();
                    
                    /* dimensions of the fiducials in units of (mm, mm) */
                    for (int i = 0; i < 2; i++)
                        fiducial_size[i] = is.readUnsignedShort ();
                    
                    geometryReady = true;
                    break;
                }
                case PLAYER_FIDUCIAL_GET_FOV: {
                    /* the minimum range of the sensor in mm */
                    minRange  = is.readUnsignedShort ();
                    
                    /* the maximum range of the sensor in mm */
                    maxRange  = is.readUnsignedShort ();
                    
                    /* the receptive angle of the sensor in degrees */
                    viewAngle = is.readUnsignedShort ();
                    
                    fovReady = true;
                	break;
                }
                case PLAYER_FIDUCIAL_SET_FOV: {
                	break;
                }
                case PLAYER_FIDUCIAL_GET_ID: {
                	id = is.readInt ();    /* the value displayed */

                    idReady = true;
                    break;
                }
                default:{
                    System.err.println ("[Fiducial] : Unexpected response " + subtype + 
                            " of size = " + size);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println ("[Fiducial] : Error when reading payload " + e.toString ());
        }
    }
    
    /**
     * Check if geometry data is available.
     * @return true if ready, false if not ready 
     */
    public boolean isGeomReady () {
        if (geometryReady) {
            geometryReady = false;
            return true;
        }
        return false;
    }
    
    /**
     * Get the pose of the detector in the robot cs (x, y, orient) 
     * in units if (mm, mm, degrees).
     * @return an array of integers filled with the pose of the detector 
     */
    public synchronized int[] getDetectPose () { return detectPose; }

    /**
     * Get the size of the detector in units of (mm, mm).
     * @return the size of the detector in units of (mm, mm) as an integer array.
     */
    public synchronized int[] getDetectSize () { return detectSize; }

    /**
     * Get the dimensions of the fiducials in units of (mm, mm).
     * @return the dimensions of the fiducials in units of (mm, mm) as an integer
     * array.
     */
    public synchronized int[] getFiducialSize () { return fiducial_size; }
    
    /**
     * Configuration request: Get sensor field of view.
     * <br><br>
     * The field of view of the fiducial device can be set using the 
     * PLAYER_FIDUCIAL_SET_FOV request, and queried using the 
     * PLAYER_FIDUCIAL_GET_FOV request. The device replies to a SET request 
     * with the actual FOV achieved. In both cases the request and reply 
     * packets have the same format.
     * <br><br>
     * See the player_fiducial_fov structure from player.h
     */
    public void getFOV () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 1);     /* 1 byte payload */
            os.writeByte (PLAYER_FIDUCIAL_GET_FOV);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Fiducial] : Couldn't send PLAYER_FIDUCIAL_GET_FOV " +
                    "command: " + e.toString ());
        }
    }
    
    /**
     * Check if FOV data is available.
     * @return true if ready, false if not ready 
     */
    public boolean isFOVReady () {
        if (fovReady) {
            fovReady = false;
            return true;
        }
        return false;
    }

    /**
     * Get the minimum range of the sensor in mm.
     * @return the minimum range of the sensor in mm as an integer
     */
    public synchronized int getMinRange () { return minRange; }

    /**
     * Get the maximum range of the sensor in mm.
     * @return the maximum range of the sensor in mm as an integer
     */
    public synchronized int getMaxRange () { return maxRange; }
    
    /**
     * Get the receptive angle of the sensor in degrees.
     * @return the receptive angle of the sensor in degrees as an integer
     */
    public synchronized int getViewAngle () { return viewAngle; }
    
    /**
     * Configuration request: Set sensor field of view.
     * <br><br>
     * The field of view of the fiducial device can be set using the 
     * PLAYER_FIDUCIAL_SET_FOV request, and queried using the 
     * PLAYER_FIDUCIAL_GET_FOV request. The device replies to a SET request 
     * with the actual FOV achieved. In both cases the request and reply 
     * packets have the same format.
     * <br><br>
     * See the player_fiducial_fov structure from player.h
     */
    public void setFOV (int newMinRange, int newMaxRange, int newViewAngle) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 7);     /* 7 byte payload */
            os.writeByte (PLAYER_FIDUCIAL_SET_FOV);
            /* the minimum range of the sensor in mm */
            os.writeShort (newMinRange);
            /* the maximum range of the sensor in mm */
            os.writeShort (newMaxRange);
            /* the receptive angle of the sensor in degrees */
            os.writeShort (newViewAngle);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Fiducial] : Couldn't send PLAYER_FIDUCIAL_SET_FOV " +
                    "command: " + e.toString ());
        }
    }

    /**
     * Configuration request: Get fiducial value.
     * <br><br>
     * Some fiducial finder devices display their own fiducial. They can use the 
     * PLAYER_FIDUCIAL_GET_ID config to report the identifier displayed by the 
     * fiducial. Make the request using the player_fiducial_id_t structure. The 
     * device replies with the same structure with the id field set. 
     * <br><br>
     * Some devices can dynamically change the identifier they display. They can 
     * use the PLAYER_FIDUCIAL_SET_ID config to allow a client to set the currently 
     * displayed value. Make the request with the player_fiducial_id_t structure. 
     * The device replies with the same structure with the id field set to the value 
     * it actually used. You should check this value, as the device may not be able 
     * to display the value you requested.
     * <br><br>
     * Currently supported by the stg_fiducial driver.
     * <br><br>
     * See the player_fiducial_id structure from player.h
     */
    public void getFiducialVal () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 1);     /* 1 byte payload */
            os.writeByte (PLAYER_FIDUCIAL_GET_ID);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Fiducial] : Couldn't send PLAYER_FIDUCIAL_GET_ID " +
                    "command: " + e.toString ());
        }
    }
    
    /**
     * Check if fiducial value data is available.
     * @return true if ready, false if not ready 
     */
    public boolean isIDReady () {
        if (idReady) {
            idReady = false;
            return true;
        }
        return false;
    }

    /**
     * Get the fiducial data value displayed.
     * @return the fiducial data value as an integer
     */
    public synchronized int getID () { return id; }

    /**
     * Configuration request: Get fiducial value.
     * <br><br>
     * Some fiducial finder devices display their own fiducial. They can use the 
     * PLAYER_FIDUCIAL_GET_ID config to report the identifier displayed by the 
     * fiducial. Make the request using the player_fiducial_id_t structure. The 
     * device replies with the same structure with the id field set. 
     * <br><br>
     * Some devices can dynamically change the identifier they display. They can 
     * use the PLAYER_FIDUCIAL_SET_ID config to allow a client to set the currently 
     * displayed value. Make the request with the player_fiducial_id_t structure. 
     * The device replies with the same structure with the id field set to the value 
     * it actually used. You should check this value, as the device may not be able 
     * to display the value you requested.
     * <br><br>
     * Currently supported by the stg_fiducial driver.
     * <br><br>
     * See the player_fiducial_id structure from player.h
     */
    public void setFiducialVal (int newID) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 5);     /* 5 byte payload */
            os.writeByte (PLAYER_FIDUCIAL_SET_ID);
            os.writeInt (newID);                    /* the value displayed */
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Fiducial] : Couldn't send PLAYER_FIDUCIAL_SET_ID " +
                    "command: " + e.toString ());
        }
    }

    /**
     * Get the number of fiducials.
     * @return the number of fiducials as an integer.
     */
    public int getFiducialCount() {
    	return this.count; 
    }

    /**
     * Get the fiducials.
     * @return an array of PlayerFiducialItem.
     */
    public PlayerFiducialItem[] getFiducials () {
    	return this.fiducials;
    }
    
    // TO DO: implement fiducial messaging once Player supports it
}
