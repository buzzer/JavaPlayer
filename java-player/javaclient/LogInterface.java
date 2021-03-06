/*
 *  Player Java Client - LogInterface.java
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
 * $Id: LogInterface.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient;

/**
 * The log interface provides start/stop control of data logging/playback.<br /><br />
 * The log interface produces no data and accepts no commands. 
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 * </ul>
 */
public class LogInterface extends PlayerDevice {

    private static final boolean isDebugging = PlayerClient.isDebugging;
    
    /* log R/W control */
    private final short PLAYER_LOG_CODE    = PlayerClient.PLAYER_LOG_CODE;

    /* the player message types (see player.h) */
    private static final short PLAYER_MSGTYPE_REQ = PlayerClient.PLAYER_MSGTYPE_REQ;

    /* the subtypes for config requests */
    protected static final short PLAYER_LOG_SET_WRITE_STATE_REQ = 1;
    protected static final short PLAYER_LOG_SET_READ_STATE_REQ  = 2;
    protected static final short PLAYER_LOG_GET_STATE_REQ       = 3;
    protected static final short PLAYER_LOG_SET_READ_REWIND_REQ = 4;
    protected static final short PLAYER_LOG_SET_FILENAME        = 5;
    
    /* types of log devices */
    protected static final short PLAYER_LOG_TYPE_READ  = 1;
    protected static final short PLAYER_LOG_TYPE_WRITE = 2;
    
    /* the logging/playback state and type */
    private byte     state;
    private byte     type;
    private boolean  readyState = false;

    /**
     * Constructor for LogInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public LogInterface (PlayerClient pc, short indexOfDevice) {
        super(pc);
        device    = PLAYER_LOG_CODE;
        index     = indexOfDevice;
    }
    
    /**
     * Configuration request: Set logging state.
     * <br><br>
     * Start/stop data logging.<br /><br />
     * See the player_log_set_write_state structure from player.h
     * @param state 0=disabled, 1=enabled 
     */
    public void setLoggingState (int state) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 2);     /* 2 byte payload */
            os.writeByte (PLAYER_LOG_SET_WRITE_STATE_REQ);
            os.writeByte ((byte)state);             /* 0=disabled, 1=enabled */
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Log] : Couldn't send PLAYER_LOG_SET_WRITE_STATE_REQ " +
                    "command: " + e.toString ());
        }
    }

    /**
     * Configuration request: Set playback state.
     * <br><br>
     * Start/stop data playback.<br /><br />
     * See the player_log_set_read_state structure from player.h
     * @param state 0=disabled, 1=enabled 
     */
    public void setPlaybackState (int state) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 2);     /* 2 byte payload */
            os.writeByte (PLAYER_LOG_SET_READ_STATE_REQ);
            os.writeByte ((byte)state);             /* 0=disabled, 1=enabled */
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Log] : Couldn't send PLAYER_LOG_SET_READ_STATE_REQ " +
                    "command: " + e.toString ());
        }
    }
    
    /**
     * Configuration request: Rewind playback.
     * <br><br>
     * Rewind log playback to beginning of logfile; does not affect playback 
     * state (i.e., whether it is started or stopped).<br /><br />
     * See the player_log_set_read_rewind structure from player.h
     */
    public void rewindPlayback () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 1);     /* 1 byte payload */
            os.writeByte (PLAYER_LOG_SET_READ_REWIND_REQ);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Log] : Couldn't send PLAYER_LOG_SET_READ_REWIND_REQ " +
                    "command: " + e.toString ());
        }
    }
    
    /**
     * Configuration request: Get state.
     * <br><br>
     * Find out whether logging/playback is enabled or disabled.<br /><br />
     * See the player_log_get_state structure from player.h
     */
    public void readState () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 1);     /* 1 byte payload */
            os.writeByte (PLAYER_LOG_GET_STATE_REQ);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Log] : Couldn't send PLAYER_LOG_GET_STATE_REQ " +
                    "command: " + e.toString ());
        }
    }
    
    /**
     * Configuration request: Set filename.
     * <br><br>
     * Set the name of the file to write to when logging.<br /><br />
     * See the player_log_set_filename structure from player.h
     * @param fileName the name of the file (max 255 chars + terminating NULL) 
     */
    public void setFileName (String fileName) {
        if (fileName.length () > 255)
            fileName = fileName.substring(0, 255);
        try {
            int size = 2 + fileName.length ();
            sendHeader (PLAYER_MSGTYPE_REQ, size);     /* payload */
            os.writeByte (PLAYER_LOG_SET_FILENAME);
            for (int i = 0; i < fileName.length (); i++)
                os.writeByte (fileName.toCharArray ()[i]);
            os.writeByte (0);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Log] : Couldn't send PLAYER_LOG_SET_FILENAME " +
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
            	System.err.println ("[Log][Debug] : Unexpected response of size 0!");
            return;
        }
        try {
            /* each reply begins with a uint8_t subtype field */
            byte subtype = is.readByte ();
            switch (subtype) {
                case PLAYER_LOG_SET_WRITE_STATE_REQ: {
                    break;
                }
                case PLAYER_LOG_SET_READ_STATE_REQ: {
                    break;
                }
                case PLAYER_LOG_GET_STATE_REQ: {
                    type       = is.readByte ();    /* ignore the type */
                    state      = is.readByte ();    /* logging/playback state: 
                                                       0=disabled, 1=enabled */
                    readyState = true; 
                    break;
                }
                case PLAYER_LOG_SET_READ_REWIND_REQ: {
                    break;
                }
                case PLAYER_LOG_SET_FILENAME: {
                	break;
                }
                default:{
                    System.err.println ("[Log] : Unexpected response " + subtype + 
                            " of size = " + size);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println ("[Log] : Error when reading payload " + e.toString ());
        }
    }

    /**
     * Get the type of log device.
     * @return the type of log device, either PLAYER_LOG_TYPE_READ or 
     * PLAYER_LOG_TYPE_WRITE 
     */
    public byte getType () { return this.type; }
    
    /**
     * Get the logging/playback state.
     * @return the logging/playback state, 0=disabled, 1=enabled
     */
    public byte getState () { return this.state; }
    
    /**
     * Check if state data is available.
     * @return true if ready, false if not ready 
     */
    public boolean isStateReady () {
        if (readyState) {
            readyState = false;
            return true;
        }
        return false;
    }
    
}
