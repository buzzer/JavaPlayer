/*
 *  Player Java Client - LocalizeInterface.java
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
 * $Id: LocalizeInterface.java 29 2005-11-24 12:39:51Z veedee $
 *
 */
package javaclient;

import javaclient.structures.Hypothesis;
import javaclient.structures.PlayerLocalizeConfigT;
/**
 * The localize interface provides pose information for the robot. Generally speaking, 
 * localization drivers will estimate the pose of the robot by comparing observed sensor 
 * readings against a pre-defined map of the environment. See, for the example, the amcl driver, 
 * which implements a probabilistic Monte-Carlo localization algorithm. 
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
public class LocalizeInterface extends PlayerDevice {

    private static final boolean isDebugging = PlayerClient.isDebugging;
    
    private final short PLAYER_LOCALIZE_CODE = PlayerClient.PLAYER_LOCALIZE_CODE; /* localization */
    
    /* the player message types (see player.h) */
    private static final short PLAYER_MSGTYPE_REQ = PlayerClient.PLAYER_MSGTYPE_REQ;
    
    /** the maximum number of pose hypotheses */
    public final short PLAYER_LOCALIZE_MAX_HYPOTHS = 10;
    
    /* request/reply packet subtypes */
    protected final short PLAYER_LOCALIZE_SET_POSE_REQ   = 1;
    protected final short PLAYER_LOCALIZE_GET_CONFIG_REQ = 2;
    protected final short PLAYER_LOCALIZE_SET_CONFIG_REQ = 3;
    
    private short pendingCount    = 0;     /* the number of pending (unprocessed) observations */
    private int   pendingTimeSec  = 0;     /* the time stamp of the last observation processed */
    private int   pendingTimeuSec = 0;
    private int   hypothCount     = 0;     /* the number of pose hypotheses */
    
    /* the array of the hypotheses */
    private Hypothesis[] hypoths = new Hypothesis[PLAYER_LOCALIZE_MAX_HYPOTHS];
    
    private PlayerLocalizeConfigT plct      = new PlayerLocalizeConfigT ();
    private boolean               readyPLCT  = false;
    
    /**
     * Constructor for LocalizeInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public LocalizeInterface (PlayerClient pc, short indexOfDevice) {
        super(pc);
        device    = PLAYER_LOCALIZE_CODE;
        index     = indexOfDevice;
    }
    
    /**
     * Returns the number of hypotheses.
     */
    public int getHypothCount () {
    	return this.hypothCount;
    }
    
    /**
     * Read an array of hypotheses.
     */
    public synchronized void readData () {
        readHeader ();
        try {
            pendingCount    = (short)is.readUnsignedShort ();   /* number of pending observations */
            pendingTimeSec  = is.readInt ();        /* time stamp of the last observation */
            pendingTimeuSec = is.readInt ();
            hypothCount     = is.readInt ();        /* number of pose hypotheses */
            
            for (int i = 0; i < hypothCount; i++) {
            	hypoths[i] = new Hypothesis ();
                int[] localizeMean = new int [3];
                for (int j = 0; j < 3; j++)         /* mean value of the pose estimate */
                	localizeMean[j] = is.readInt ();
                hypoths[i].setMean (localizeMean);
                
                long[][] localizeCov = new long[3][3];
                for (int j = 0; j < 3; j++)         /* covariance matrix pose estimate */
                    for (int k = 0; k < 3; k++)
                        localizeCov[j][k] = is.readLong ();
                hypoths[i].setCov   (localizeCov);    
                hypoths[i].setAlpha (is.readInt ());   /* weight coefficient for linear combination */
            }
        } catch (Exception e) {
            System.err.println ("[Localize] : Error when reading payload: " + e.toString ());
        }
    }
    
    /**
     * Configuration request: Set the robot pose estimate.
     * @param mean the mean value of the pose estimate (mm, mm, arc-seconds)
     * @param cov the covariance matrix pose estimate (mm$^2$, arc-seconds$^2$)
     */
    public void setPose (int[] mean, long[][] cov) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 85);      /* 85 bytes payload */
            os.writeByte (PLAYER_LOCALIZE_SET_POSE_REQ);
            for (int i = 0; i < 3; i++)
                os.writeInt (mean[i]);                /* mean value of the pose estimate */
            for (int i = 0; i < 3; i++)
                for (int j = 0; j < 3; j++)
                    os.writeLong (cov[i][j]);         /* covariance matrix pose estimate */
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Localize] : Couldn't send PLAYER_LOCALIZE_SET_POSE_REQ " +
                    "command: " + e.toString ());
        }
    }
    
    /**
     * Configuration request: Get configuration.
     */
    public void getConfiguration () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 1);      /* 1 byte payload */
            os.writeByte (PLAYER_LOCALIZE_GET_CONFIG_REQ);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Localize] : Couldn't send PLAYER_LOCALIZE_GET_CONFIG_REQ " +
                    "command: " + e.toString ());
        }
    }
    
    /**
     * Configuration request: Set configuration.
     * @param numParticles maximum number of particles (for drivers using particle filters)
     */
    public void setConfiguration (int numParticles) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 5);      /* 5 bytes payload */
            os.writeByte (PLAYER_LOCALIZE_SET_CONFIG_REQ);
            os.writeInt (numParticles);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Localize] : Couldn't send PLAYER_LOCALIZE_GET_CONFIG_REQ " +
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
            	System.err.println ("[Localize][Debug] : Unexpected response of size 0!");
            return;
        }
        try {
            /* each reply begins with a uint8_t subtype field */
            byte subtype = is.readByte ();
            switch (subtype) {
                case PLAYER_LOCALIZE_SET_POSE_REQ:{
                	break;
                }
                case PLAYER_LOCALIZE_GET_CONFIG_REQ:{
                    plct = new PlayerLocalizeConfigT ();
                    plct.setNumParticles (is.readInt ());
                    readyPLCT = true;
                    break;
                }
                case PLAYER_LOCALIZE_SET_CONFIG_REQ:{
                    break;
                }
                default:{
                    System.err.println ("[Localize] : Unexpected response " + subtype + 
                            " of size = " + size);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println ("[Localize] : Error when reading payload " + e.toString ());
        }
    }
    
    /**
     * Check if configuration data is available.
     * @return true if ready, false if not ready 
     */
    public boolean isPLCTReady () {
        if (readyPLCT) {
            readyPLCT = false;
            return true;
        }
        return false;
    }
    
    /**
     * Get the configuration data.
     * @return an object of type PlayerLocalizeConfigT containing the required configuration data 
     */
    public PlayerLocalizeConfigT getPLCT () { return plct; }
}

