/*
 *  Player Java Client - BlinkenlightInterface.java
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
 * $Id: BlinkenlightInterface.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient;

/**
 * The blinkenlight interface is used to switch on and off a flashing indicator 
 * light, and to set it's flash period.<br /><br />
 * This interface accepts no configuration requests.
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 * </ul>
 */
public class BlinkenlightInterface extends PlayerDevice {

    /* blinking lights */
    private final short PLAYER_BLINKENLIGHT_CODE = PlayerClient.PLAYER_BLINKENLIGHT_CODE;

    /* the player message types (see player.h) */
    private final short PLAYER_MSGTYPE_CMD = PlayerClient.PLAYER_MSGTYPE_CMD;

    private byte enable;        /* zero: disabled, non-zero: enabled */
    private int  period_ms;     /* flash period (one whole on-off cycle) in milliseconds */
    
    /**
     * Constructor for BlinkenlightInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public BlinkenlightInterface (PlayerClient pc, short indexOfDevice) {
        super(pc);
        device    = PLAYER_BLINKENLIGHT_CODE;
        index     = indexOfDevice;
    }
    
    /**
     * The blinkenlight data provides the current state of the indicator light.
     */
    public synchronized void readData () {
        readHeader ();
        try {
            enable    = (byte)is.readUnsignedByte ();   /* enable/disable */
            period_ms = is.readUnsignedShort ();        /* flash period in ms */
        } catch (Exception e) {
            System.err.println ("[Blinkenlight] : Error when reading payload: " + e.toString ());
        }
    }
    
    /**
     * Returns the current blinking lights state (zero: disabled, non-zero: enabled). 
     * @return the current blinking lights state as a byte
     */
    public synchronized byte getEnable () { return this.enable; }

    /**
     * Returns the flash period (one whole on-off cycle) in milliseconds.
     * @return the flash period as an integer
     */
    public synchronized int getPeriod () { return this.period_ms; }

    /**
     * Set the blinkenlight state and period. 
     * @param ena zero: disabled, non-zero: enabled
     * @param period flash period (one whole on-off cycle) in milliseconds
     */
    public void set (byte ena, int period) {
        try {
            sendHeader (PLAYER_MSGTYPE_CMD, 3);         /* 3 bytes payload */
            os.writeByte (ena);                         /* enable/disable */
            os.writeShort (period);                     /* flash period in ms */
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Blinkenlight] : Couldn't send set command request: " + 
                    e.toString ());
        }
    }

}
