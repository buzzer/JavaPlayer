/*
 *  Player Java Client - LaserInterface.java
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
 * $Id: LaserInterface.java 12 2005-05-14 21:16:23Z veedee $
 *
 */
package javaclient;

import javaclient.structures.PlayerLaserConfigT;
import javaclient.structures.PlayerLaserGeomT;

/**
 * The laser interface provides access to a single-origin scanning range sensor, such as a 
 * SICK laser range-finder (e.g., sicklms200).
 * <br><br>
 * Devices supporting the laser interface can be configured to scan at different angles and 
 * resolutions. As such, the data returned by the laser interface can take different forms. To 
 * make interpretation of the data simple, the laser data packet contains some extra fields 
 * before the actual range data. These fields tell the client the starting and ending angles of 
 * the scan, the angular resolution of the scan, and the number of range readings included. Scans 
 * proceed counterclockwise about the laser (0 degrees is forward). The laser can return a maximum 
 * of 401 readings; this limits the valid combinations of scan width and angular resolution.
 * <br><br>
 * This interface accepts no commands. 
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class LaserInterface extends PlayerDevice {
	
    private static final boolean isDebugging = PlayerClient.isDebugging;
    
    /* scanning range-finder */
    private short PLAYER_LASER_CODE = PlayerClient.PLAYER_LASER_CODE;
	
    /* the player message types (see player.h) */
    private final short PLAYER_MSGTYPE_REQ = PlayerClient.PLAYER_MSGTYPE_REQ;

    /* request types */
    protected final short PLAYER_LASER_GET_GEOM     = 1;
    protected final short PLAYER_LASER_SET_CONFIG   = 2;
    protected final short PLAYER_LASER_GET_CONFIG   = 3;
    protected final short PLAYER_LASER_POWER_CONFIG = 4;

    /** maximum number of laser range values */
    public final int PLAYER_LASER_MAX_SAMPLES = 401;
	
    private short minAngle     = 0;  /* start angle for the laser scan (in units of 0.01 degrees) */
    private short maxAngle     = 0;  /* end angle for the laser scan (in units of 0.01 degrees). */
    private int   resolution   = 0;  /* angular resolution (in units of 0.01 degrees) */
    private int   rangeRes     = 0;  /* range resolution. ranges should be multipled by this */
    private int   samplesCount = 0;  /* number of range/intensity readings */
    private int   range[]      = new int[PLAYER_LASER_MAX_SAMPLES];   /* range readings (mm) */
    private int   intensity[]  = new int[PLAYER_LASER_MAX_SAMPLES];   /* intensity readings */
    
	private boolean newInfo    = false;
	
    /* object containing player_laser_geom in case of a threaded call */
    private PlayerLaserGeomT plgt;
    private boolean          readyPLGT;
    
    /* object containing player_laser_config in case of a threaded call */
    private PlayerLaserConfigT plct;
    private boolean            readyPLCT;
    /**
     * Constructor for LaserInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public LaserInterface (PlayerClient pc, short indexOfDevice) {
        super (pc);
        device    = PLAYER_LASER_CODE;
        index     = indexOfDevice;

        for (int i = 0; i < range.length; i++) {
            range[i]     = 0;
            intensity[i] = 0;
        }
	}

    /**
     * Read the laser data packet.
     */
    public synchronized void readData () {
        readHeader ();
        try {
            minAngle     = is.readShort ();             /* start angle for the laser scan */
            maxAngle     = is.readShort ();             /* end angle for the laser scan */
            resolution   = is.readUnsignedShort ();     /* angular resolution */
            rangeRes     = is.readUnsignedShort ();     /* range resolution */
            samplesCount = is.readUnsignedShort ();     /* number of range/intensity readings */
            
            for (int i = 0; i < PLAYER_LASER_MAX_SAMPLES; i++)
                range[i] = is.readUnsignedShort ();     /* range readings (mm) */
            for (int i = 0; i < PLAYER_LASER_MAX_SAMPLES; i++)
                intensity[i] = is.readUnsignedByte ();  /* intensity readings */
        } catch (Exception e) {
        	System.err.println ("[Laser] : Error when reading payload: " + e.toString ());
        }
    }
    
    /**
     * Return the start angle for the laser scan in units of 0.01 degrees.
     * @return the start angle for the laser scan as a short
     */
    public synchronized short getMinAngle     () { return minAngle;     }
    
    /**
     * Return the end angle for the laser scan in units of 0.01 degrees.
     * @return the end angle for the laser scan as a short
     */
    public synchronized short getMaxAngle     () { return maxAngle;     }
    
    /**
     * Return the angular resolution in units of 0.01 degrees.
     * @return the angular resolution as an integer
     */
    public synchronized int   getResolution   () { return resolution;   }
    
    /**
     * Return the range resolution.
     * @return the range resolution as an integer
     */
    public synchronized int   getRangeRes     () { return rangeRes;     }
    
    /**
     * Return the number of range/intensity readings.
     * @return the number of range/intensity readings as an integer
     */
    public synchronized int   getSamplesCount () { return samplesCount; }
    
    /**
     * Return the range readings in mm.
     * @return the range readings as an array of integers
     */
    public synchronized int[] getRanges       () { return range;        }
    
    /**
     * Return the intensity readings.
     * @return the intensity readings as an array of integers
     */
    public synchronized int[] getIntensity    () { return intensity;    }
    
    /**
     * Configuration request: Get geometry.
     * <br><br>
     * See the player_laser_geom structure from player.h
     */
    public void getGeometry () {
    	try {
            sendHeader (PLAYER_MSGTYPE_REQ, 1);        /* 1 byte payload */
            os.writeByte (PLAYER_LASER_GET_GEOM);
            os.flush ();
        } catch (Exception e) {
        	System.err.println ("[Laser] : Couldn't send PLAYER_LASER_GET_GEOM command: " + 
                    e.toString ());
        }
    }
    
    /**
     * Configuration request: Set scan properties.
     * @param minA start angle for the laser scan
     * @param maxA end angle for the laser scan
     * @param res scan resolution
     * @param range range resolution
     * @param intent enable reflection intensity data
     */
    public void setScanProperties (short minA, short maxA, short res, short range, byte intent) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 10);        /* 10 bytes payload */
            os.writeByte (PLAYER_LASER_SET_CONFIG);
            os.writeShort (minA);                       /* start angle for the laser scan */
            os.writeShort (maxA);                       /* end angle for the laser scan */
            os.writeShort (res);                        /* scan resolution */
            os.writeShort (range);                      /* range resolution */
            os.writeByte  (intent);                     /* enable reflection intensity data */
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Laser] : Couldn't send PLAYER_LASER_SET_CONFIG command: " +
                    e.toString ());
        }
    }

    /**
     * Configuration request: Get scan properties.
     *
     */
    public void getScanProperties () {
    	try {
            sendHeader (PLAYER_MSGTYPE_REQ, 1);        /* 1 byte payload */
            os.writeByte (PLAYER_LASER_GET_CONFIG);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Laser] : Couldn't send PLAYER_LASER_GET_CONFIG command: " + 
                    e.toString ());
        }
    }
    
    /**
     * Configuration request: Turn power on/off.
     * @param value 0 to turn laser off, 1 to turn laser on 
     */
    public void setPower (byte value) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 2);        /* 2 bytes payload */
            os.writeByte (PLAYER_LASER_POWER_CONFIG);
            os.writeByte (value);
            os.flush ();
        } catch (Exception e) {
        	System.err.println ("[Laser] : Couldn't send PLAYER_LASER_POWER_CONFIG command: " +
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
            	System.err.println ("[Laser][Debug] : Unexpected response of size 0!");
            return;
        }
        try {
            /* each reply begins with a uint8_t subtype field */
            short subtype = is.readByte ();
            switch (subtype) {
                case PLAYER_LASER_GET_GEOM: {
                    plgt = new PlayerLaserGeomT ();
                    
                    short laserPose[] = new short[2];   /* laser pose, in robot cs (mm, mm, degrees) */
                    short laserSize[] = new short[3];   /* laser dimensions (mm, mm) */
                    
                    laserPose[0] = is.readShort ();     /* get the laser pose */
                    laserPose[1] = is.readShort ();
                    laserPose[2] = is.readShort ();
                    laserSize[0] = is.readShort ();     /* get the laser dimensions */
                    laserSize[1] = is.readShort ();
                    plgt.setPose (laserPose);
                    plgt.setSize (laserSize);
                    
                    readyPLGT = true;
                	break;
                }
                case PLAYER_LASER_SET_CONFIG: {
                	break;
                }
                case PLAYER_LASER_GET_CONFIG: {
                    plct = new PlayerLaserConfigT ();
                    
                    plct.setMinAngle   (is.readShort ());        /* start angle for the laser scan */
                    plct.setMaxAngle   (is.readShort ());        /* end angle for the laser scan */
                    plct.setResolution (is.readUnsignedShort ());/* scan resolution */
                    plct.setRangeRes   (is.readUnsignedShort ());/* range resolution */
                    plct.setIntensity  (is.readByte ());         /* enable reflection intensity data */
                    
                    readyPLCT = true;
                	break;
                }
                case PLAYER_LASER_POWER_CONFIG: {
                	break;
                }
                default:{
                    System.err.println ("[Laser] : Unexpected response " + subtype + 
                            " of size = " + size);
                    break;
                }
            }
        } catch (Exception e) {
        	System.err.println ("[Laser] : Error when reading payload " + e.toString ());
        }
    }
    
    /**
     * Get the laser geometry after a PLAYER_LASER_GET_GEOM request.
     * @return an object of PlayerLaserGeomT type containing the laser geometry
     * @see #isReadyPLGT()
     */
    public PlayerLaserGeomT   getPlayerLaserGeom   () { return plgt; }
    
    /**
     * Get the laser configuration after a PLAYER_LASER_GET_CONFIG request.
     * @return an object of PlayerLaserConfigT type containing the laser configuration
     * @see #isReadyPLCT()
     */
    public PlayerLaserConfigT getPlayerLaserConfig () { return plct; }
    
    /**
     * Check if the geometry data is available.
     * @return true if ready, false if not ready
     * @see #getPlayerLaserGeom()
     */
    public boolean isReadyPLGT () {
        if (readyPLGT) {
            readyPLGT = false;
            return true;
        }
        return false;
    }
    
    /**
     * Check if the configuration data is available.
     * @return true if ready, false if not ready
     * @see #getPlayerLaserConfig()
     */
    public boolean isReadyPLCT () {
        if (readyPLCT) {
            readyPLCT = false;
            return true;
        }
        return false;
    }
}
