/*
 *  Player Java Client - PtzInterface.java
 *  Copyright (C) 2004-2005 Maxim Batalin & Radu Bogdan Rusu
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
 * $Id: PtzInterface.java 17 2005-05-23 13:13:22Z veedee $
 *
 */
package javaclient;

/**
 * The ptz interface is used to control a pan-tilt-zoom unit, such as a camera.
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class PtzInterface extends PlayerDevice {

    private static final boolean isDebugging = PlayerClient.isDebugging;

    private final short PLAYER_PTZ_CODE = PlayerClient.PLAYER_PTZ_CODE;  /* pan-tilt-zoom unit */
    
    /* the player message types (see player.h) */
    private static final short PLAYER_MSGTYPE_CMD = PlayerClient.PLAYER_MSGTYPE_CMD;
    private static final short PLAYER_MSGTYPE_REQ = PlayerClient.PLAYER_MSGTYPE_REQ;

    /** maximum command length for use with PLAYER_PTZ_GENERIC_CONFIG_REQ, based on the 
     * Sony EVID30 camera right now
     */
    public final short PLAYER_PTZ_MAX_CONFIG_LEN = 32;
    
    /* code for generic configuration request */
    protected final short PLAYER_PTZ_GENERIC_CONFIG_REQ = 1;
    /* code for control mode configuration request */
    protected final short PLAYER_PTZ_CONTROL_MODE_REQ   = 2;
    /* code for autoservo configuration request */
    protected final short PLAYER_PTZ_AUTOSERVO          = 3;
    
    private short pan;              /* pan (degrees) */
    private short tilt;             /* tilt (degrees) */
    private short zoom;             /* field of view (degrees) */
    private short panSpeed;         /* current pan velocity (deg/sec) */
    private short tiltSpeed;        /* current tilt velocity (deg/sec) */
    
    /**
     * Constructor for PtzInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public PtzInterface (PlayerClient pc, short indexOfDevice) {
        super(pc);
        device    = PLAYER_PTZ_CODE;
        index     = indexOfDevice;
    }
    
    /**
     * Read the data reflecting the current state of the Pan-Tilt-Zoom unit.
     */
    public synchronized void readData () {
        readHeader ();
        try {
            pan       = is.readShort ();        /* pan (degrees): -100..100 */
            tilt      = is.readShort ();        /* tilt (degrees): -25..25 */ 
            zoom      = is.readShort ();        /* field of view (degrees): 0..1023 */
            panSpeed  = is.readShort ();        /* current pan velocity (deg/sec) */
            tiltSpeed = is.readShort ();        /* current tilt velocity (deg/sec) */
        } catch (Exception e) {
            System.err.println ("[Ptz] : Error when reading payload: " + e.toString ());
        }
    }
    
    /**
     * The ptz interface accepts commands that set change the state of the unit. Note that the 
     * commands are absolute, not relative.
     * @param pan pan (degrees): -100..100
     * @param tilt tilt (degrees): -25..25
     * @param zoom field of view (degrees): 0..1023
     * @param panspeed current pan velocity (deg/sec)
     * @param tiltspeed current tilt velocity (deg/sec)
     */
    public void setPTZ (int pan, int tilt, int zoom, int panspeed, int tiltspeed) {
    	try {
            sendHeader (PLAYER_MSGTYPE_CMD, 10);    /* 10 byte payload */
            os.writeShort ((short)pan);             /* pan (degrees */
            os.writeShort ((short)tilt);            /* tilt (degrees) */ 
            os.writeShort ((short)zoom);            /* field of view (degrees) */
            os.writeShort ((short)panspeed);        /* current pan velocity (deg/sec) */
            os.writeShort ((short)tiltspeed);       /* current tilt velocity (deg/sec) */
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Ptz] : Couldn't send set PTZ parameters command: " +
                    e.toString ());
        }
    }
    
    /**
     * Configuration request: Generic request.
     * This ioctl allows the client to send a unit-specific command to the unit. Whether data 
     * is returned depends on the command that was sent.
     * @param length length of data in config buffer
     * @param buf buffer for command/reply
     */
    public void genericRequest (int length, byte[] buf) {
        if (length > PLAYER_PTZ_MAX_CONFIG_LEN)
            length = PLAYER_PTZ_MAX_CONFIG_LEN;
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, length + 3);
            os.writeByte (PLAYER_PTZ_GENERIC_CONFIG_REQ);
            os.writeShort ((short)length);     /* length of data in config buffer */ 
            
            for (int i = 0; i < length; i++) {
            	os.writeByte (buf[i]);         /* buffer for command/reply */
            }
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Ptz] : Couldn't send PLAYER_PTZ_GENERIC_CONFIG_REQ command: " +
                    e.toString ());
        }
    }
    
    /**
     * Configuration request: Control mode.
     * This ioctl allows the client to switch between position and velocity control, for those 
     * drivers that support it. Note that this request changes how the driver interprets 
     * forthcoming commands from all clients.
     * @param mode mode to use: must be either PLAYER_PTZ_VELOCITY_CONTROL (0) or 
     * PLAYER_PTZ_POSITION_CONTROL (1)
     */
    public void controlRequest (int mode) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 2);     /* 2 bytes payload */
            os.writeByte (PLAYER_PTZ_CONTROL_MODE_REQ);
            os.writeByte ((byte)mode);              /* 0 for velocity, 1 for position */ 
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Ptz] : Couldn't send PLAYER_PTZ_CONTROL_MODE_REQ command: " +
                    e.toString ());
        }
    }
    
    /**
     * Configuration request: Set AutoServo mode.
     * Enable/Disable AutoServo mode on cameras such as the CMUcam2.
     * @param mode 0=disabled, 1=enabled
     */
    public void setAutoServo (int mode) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 2);     /* 2 bytes payload */
            os.writeByte (PLAYER_PTZ_AUTOSERVO);
            os.writeByte ((byte)mode);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Ptz] : Couldn't send PLAYER_PTZ_AUTOSERVO command: " +
                    e.toString ());
        }
    }
    
    /**
     * Get pan in degrees.
     * @return pan (degrees) as a short
     */
    public synchronized short getPan       () { return pan;       }

    /**
     * Get tilt in degrees.
     * @return tilt (degrees) as a short
     */
    public synchronized short getTilt      () { return tilt;      }

    /**
     * Get field of view in degrees.
     * @return field of view (degrees) as a short
     */
    public synchronized short getZoom      () { return zoom;      }

    /**
     * Get current pan velocity in deg/sec.
     * @return current pan velocity (deg/sec) as a short
     */
    public synchronized short getPanSpeed  () { return panSpeed;  }

    /**
     * Get current tilt velocity in deg/sec.
     * @return current tilt velocity (deg/sec) as a short
     */
    public synchronized short getTiltSpeed () { return tiltSpeed; }
    
    /**
     * Handle acknowledgement response messages (threaded mode).
     * @param size size of the payload
     */
    public void handleResponse (int size) {
        if (size == 0) {
            if (isDebugging)
                System.err.println ("[Ptz][Debug] : Unexpected response of size 0!");
            return;
        }
        try {
            /* each reply begins with a uint8_t subtype field */
            byte subtype = is.readByte ();
            switch (subtype) {
                case PLAYER_PTZ_GENERIC_CONFIG_REQ: {
                    break;
                }
                case PLAYER_PTZ_CONTROL_MODE_REQ: {
                    break;
                }
                case PLAYER_PTZ_AUTOSERVO: {
                    break;
                }
                default:{
                    System.err.println ("[Ptz] : Unexpected response " + subtype + 
                            " of size = " + size);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println ("[Ptz] : Error when reading payload " + e.toString ());
        }
    }
    
}
