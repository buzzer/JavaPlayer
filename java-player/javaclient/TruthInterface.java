/*
 *  Player Java Client - TruthInterface.java
 *  Copyright (C) 2002-2005 Maxim Batalin & Radu Bogdan Rusu
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
 * $Id: TruthInterface.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient;

/**
 * The truth interface provides access to the absolute state of entities. Note that, unless your 
 * robot has superpowers, truth devices are only avilable in simulation. 
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class TruthInterface extends PlayerDevice {

    private static final boolean isDebugging = PlayerClient.isDebugging;

    /* ground-truth (via Stage) */
    private final short PLAYER_TRUTH_CODE = PlayerClient.PLAYER_TRUTH_CODE;
    
    /* the player message types (see player.h) */
    private static final short PLAYER_MSGTYPE_REQ = PlayerClient.PLAYER_MSGTYPE_REQ;
   
    /* request packet subtypes */
    protected final short PLAYER_TRUTH_GET_POSE         = 0;
    protected final short PLAYER_TRUTH_SET_POSE         = 1;
    protected final short PLAYER_TRUTH_SET_POSE_ON_ROOT = 2;
    protected final short PLAYER_TRUTH_GET_FIDUCIAL_ID  = 3;
    protected final short PLAYER_TRUTH_SET_FIDUCIAL_ID  = 4;
    
    /* object position in the world (x, y, z) in mm */
    private int xPos = 0;
    private int yPos = 0;
    private int zPos = 0;
    /* object orientation in the world (r, p, y) in millirad */
    private int rHead = 0;
    private int pHead = 0;
    private int yHead = 0;
    
    private boolean isTeleported = false;
    
    /**
     * Constructor for TruthInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public TruthInterface (PlayerClient pc, short indexOfDevice) {
        super(pc);
        device    = PLAYER_TRUTH_CODE;
        index     = indexOfDevice;
    }
    
    /**
     * Read the current state of the entity.
     */
    public synchronized void readData () {
        readHeader ();
        try {
            xPos  = is.readInt ();      /* X position in the world in mm */
            yPos  = is.readInt ();      /* Y position in the world in mm */
            zPos  = is.readInt ();      /* Z position in the world in mm */
            rHead = is.readInt ();      /* R orientation in the world in millirad */
            pHead = is.readInt ();      /* P orientation in the world in millirad */
            yHead = is.readInt ();      /* Y orientation in the world in millirad */
        } catch (Exception e) {
            System.err.println ("[Truth] : Error when reading payload: " + e.toString ());
        }
    }
    
    /**
     * Get X position in mm.
     * @return X position in mm
     */
    public synchronized int getXpos () { return xPos; }

    /**
     * Get Y position in mm.
     * @return Y position in mm
     */
    public synchronized int getYpos () { return yPos; }

    /**
     * Get Z position in mm.
     * @return Z position in mm
     */
    public synchronized int getZpos () { return zPos; }

    /**
     * Get R orientation in millirad.
     * @return R orientation in millirad
     */
    public synchronized int getRhead () { return rHead; }

    /**
     * Get P orientation in millirad.
     * @return P orientation in millirad
     */
    public synchronized int getPhead () { return pHead; }

    /**
     * Get Y orientation in millirad.
     * @return Y orientation in millirad
     */
    public synchronized int getYhead () { return yHead; }
    
    /**
     * Set the pose of the current entity.
     * @param x X position in mm
     * @param y Y position in mm
     */
    public void teleport (int x, int y) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 25);      /* 25 bytes payload */
            os.writeByte (PLAYER_TRUTH_SET_POSE);
            os.writeInt (x);
            os.writeInt (y);
            os.writeInt (zPos);
            os.writeInt (rHead);
            os.writeInt (pHead);
            os.writeInt (yHead);
            os.flush ();
        } catch (Exception e) {
        	System.err.println ("[Truth] : Couldn't send PLAYER_TRUTH_SET_POSE command: " +
                    e.toString ());
        }
        isTeleported = !isTeleported;
    }
    
    /**
     * Set the pose of the current entity.
     * @param x X position in mm
     * @param y Y position in mm
     * @param z Z position in mm
     */
    public void teleport (int x, int y, int z) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 25);      /* 25 bytes payload */
            os.writeByte (PLAYER_TRUTH_SET_POSE);
            os.writeInt (x);
            os.writeInt (y);
            os.writeInt (z);
            os.writeInt (rHead);
            os.writeInt (pHead);
            os.writeInt (yHead);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Truth] : Couldn't send PLAYER_TRUTH_SET_POSE command: " +
                    e.toString ());
        }
        isTeleported = !isTeleported;
    }
    
    /**
     * Set the pose of the current entity.
     * @param xp X position in mm
     * @param yp Y position in mm
     * @param zp Z position in mm
     * @param ro R orientation in millirad
     * @param po P orientation in millirad
     * @param yo Y orientation in millirad
     */
    public void teleport (int xp, int yp, int zp, int ro, int po, int yo) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 25);      /* 25 bytes payload */
            os.writeByte (PLAYER_TRUTH_SET_POSE);
            os.writeInt (xp);
            os.writeInt (yp);
            os.writeInt (zp);
            os.writeInt (ro);
            os.writeInt (po);
            os.writeInt (yo);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Truth] : Couldn't send PLAYER_TRUTH_SET_POSE command: " +
                    e.toString ());
        }
        isTeleported = !isTeleported;
    }
    
    /**
     * Check if the teleportation (PLAYER_TRUTH_SET_POSE) was successful or not.
     * @return true if successful, false if not
     */
    public boolean isTeleported () { return isTeleported; }
    
    /**
     * Handle acknowledgement response messages (threaded mode).
     * @param size size of the payload
     */
    public void handleResponse (int size) {
        if (size == 0) {
            if (isDebugging)
            	System.err.println ("[Truth][Debug] : Unexpected response of size 0!");
            return;
        }
        try {
            
        } catch (Exception e) {
        	System.err.println ("[Truth] : Error when reading payload " + e.toString ());
        }
    }
}
