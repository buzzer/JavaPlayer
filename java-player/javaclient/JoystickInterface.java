/*
 *  Player Java Client - JoystickInterface.java
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
 * $Id: JoystickInterface.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient;

/**
 * The joystick interface provides access to the state of a joystick. It allows 
 * another driver or a (possibly off-board) client to read and use the state of a joystick.<br />
 * This interface accepts no commands or configuration requests.
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 * </ul>
 */
public class JoystickInterface extends PlayerDevice {
    
    private final short PLAYER_JOYSTICK_CODE = PlayerClient.PLAYER_JOYSTICK_CODE; /* Joystick */

    private short xpos, ypos;       /* current joystick position (unscaled) */ 
    private short xscale, yscale;   /* scaling factors */
    private int   buttons;          /* button states (bitmask) */ 
    
    /**
     * Constructor for JoystickInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public JoystickInterface (PlayerClient pc, short indexOfDevice) {
        super(pc);
        device    = PLAYER_JOYSTICK_CODE;
        index     = indexOfDevice;
    }
    
    /**
     * The joystick data packet, which contains the current state of the joystick.
     */
    public synchronized void readData () {
        readHeader ();
        try {
            xpos    = is.readShort ();  /* current joystick position (unscaled) */
            ypos    = is.readShort ();
            xscale  = is.readShort ();  /* scaling factors */
            yscale  = is.readShort ();
            buttons = is.readUnsignedShort ();  /* button states (bitmask) */
        } catch (Exception e) {
            System.err.println ("[Joystick] : Error when reading payload: " + e.toString ());
        }
    }

    /**
     * Returns the current X joystick position (unscaled). 
     * @return the current X joystick position as a short
     */
    public synchronized short getXPos () { return this.xpos; }

    /**
     * Returns the current Y joystick position (unscaled). 
     * @return the current Y joystick position as a short
     */
    public synchronized short getYPos () { return this.ypos; }

    /**
     * Returns the current X scaling factor. 
     * @return the current X scaling factor as a short
     */
    public synchronized short getXScale () { return this.xscale; }

    /**
     * Returns the current Y scaling factor. 
     * @return the current Y scaling factor as a short
     */
    public synchronized short getYScale () { return this.yscale; }

    /**
     * Returns the current button states (bitmask). 
     * @return the current button states as an integer
     */
    public synchronized int getButtons () { return this.buttons; }
}
