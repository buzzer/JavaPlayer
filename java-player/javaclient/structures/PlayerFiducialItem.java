/*
 *  Player Java Client - PlayerFiducialItem.java
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
 * $Id: PlayerFiducialItem.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient.structures;

/**
 * Info on a single detected fiducial.<br />
 * (see the player_fiducial_item structure from player.h)
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class PlayerFiducialItem {

    /* The fiducial id. Fiducials that cannot be identified get id -1. */
    private short id;
    
    /* Fiducial position relative to the detector (x, y, z) in mm. */
    private int[] pos = new int [3];

    /* Fiducial orientation relative to the detector (r, p, y) in millirad. */
    private int[] rot = new int [3];
 
    /* Uncertainty in the measured pose (x, y, z) in mm. */
    private int[] upos = new int [3];
    
    /* Uncertainty in fiducial orientation relative to the detector (r, p, y) in millirad. */
    private int[] urot = new int [3];
    
    /**
     * 
     * @return The fiducial id. Fiducials that cannot be identified get id -1.
     */
    public synchronized int getID () {
        return this.id;
    }
    
    /**
     * 
     * @param newID the fiducial id
     */
    public synchronized void setID (int newID) {
        this.id = (short)newID;
    }

    /**
     * 
     * @return Fiducial position relative to the detector (x, y, z) in mm.
     */
    public synchronized int[] getPos () {
        return this.pos;
    }
    
    /**
     * 
     * @param newPos fiducial position relative to the detector (x, y, z) in mm
     */
    public synchronized void setPos (int[] newPos) {
        this.pos = newPos;
    }
    
    /**
     * 
     * @return Fiducial orientation relative to the detector (r, p, y) in millirad.
     */
    public synchronized int[] getRot () {
        return this.rot;
    }
    
    /**
     * 
     * @param newRot fiducial orientation relative to the detector (r, p, y) in millirad
     */
    public synchronized void setRot (int[] newRot) {
        this.rot = newRot;
    }

    /**
     * 
     * @return Uncertainty in the measured pose (x, y, z) in mm.
     */
    public synchronized int[] getUPos () {
        return this.upos;
    }
    
    /**
     * 
     * @param newUPos Uncertainty in the measured pose (x, y, z) in mm
     */
    public synchronized void setUPos (int[] newUPos) {
        this.upos = newUPos;
    }

    /**
     * 
     * @return Uncertainty in fiducial orientation relative to the detector (r, p, y)
     * in millirad.
     */
    public synchronized int[] getURot () {
        return this.urot;
    }
    
    /**
     * 
     * @param newURot uncertainty in fiducial orientation relative to the detector 
     * (r, p, y) in millirad
     */
    public synchronized void setURot (int[] newURot) {
        this.urot = newURot;
    }
}
