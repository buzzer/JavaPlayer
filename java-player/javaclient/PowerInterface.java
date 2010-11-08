/*
 *  Player Java Client - PowerInterface.java
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
 * $Id: PowerInterface.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient;

/**
 * The power interface provides access to a robot's power subsystem. This interface seems to be 
 * deprecated.
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class PowerInterface extends PlayerDevice {

    private final short PLAYER_POWER_CODE = PlayerClient.PLAYER_POWER_CODE; /* power subsystem */ 
    
    /* the player message types (see player.h) */
    private final short PLAYER_MSGTYPE_REQ = PlayerClient.PLAYER_MSGTYPE_REQ;
    
    /* request types */
    protected final short PLAYER_MAIN_POWER_REQ = 14;
    
    /* battery voltage, in decivolts */
    private int charge;
    
    /**
     * Constructor for PowerInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public PowerInterface (PlayerClient pc, short indexOfDevice) {
    	super (pc);
        device = PLAYER_POWER_CODE;
        index  = indexOfDevice;
    }

    /**
     * Read the battery voltage value.
     */
    public synchronized void readData () {
    	try {
    	    readHeader ();
            charge = is.readUnsignedShort();
        } catch (Exception e) {
    	    System.err.println ("[Power] : Error when reading payload: " + e.toString ());
        }
    }
    
    /**
     * Get the battery charge value.
     * @return battery voltage as an integer
     */
    public synchronized int getCharge () { return charge; }
    
    /**
     * Request a battery voltage charge value from the Player server.
     */
    public void requestCharge () {
    	try {
    	    sendHeader (PLAYER_MSGTYPE_REQ, 1);       /* 1 byte payload */
            os.writeByte (PLAYER_MAIN_POWER_REQ);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Power] : Couldn't send PLAYER_MAIN_POWER_REQ command: " + 
                    e.toString ());
        }
    }
}
