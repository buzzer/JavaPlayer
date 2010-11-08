/*
 *  Player Java Client - EnergyInterface.java
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
 * $Id: EnergyInterface.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient;

/**
 * The energy interface provides data about energy storage, consumption and charging. 
 * This interface accepts no commands. 
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 * </ul>
 */
public class EnergyInterface extends PlayerDevice {

    /* energy charging */
    private final short PLAYER_ENERGY_CODE = PlayerClient.PLAYER_ENERGY_CODE;

    /* the player message types (see player.h) */
    private static final short PLAYER_MSGTYPE_REQ = PlayerClient.PLAYER_MSGTYPE_REQ;

    private int  mjoules;       /* energy stored, in milliJoules */
    private int  mwatts;        /* estimated current energy consumption (negative values) 
                                    or aquisition (positive values), in milliWatts 
                                    (milliJoules/sec). */
    private byte charging;      /* charge exchange status: if 1, the device is currently 
                                    receiving charge from another energy device. If -1 the 
                                    device is currently providing charge to another energy device. 
                                    If 0, the device is not exchanging charge with an another 
                                    device. */
    
    /**
     * Constructor for EnergyInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public EnergyInterface (PlayerClient pc, short indexOfDevice) {
        super(pc);
        device    = PLAYER_ENERGY_CODE;
        index     = indexOfDevice;
    }
    
    /**
     * The energy interface reports the amount of energy stored, current rate of 
     * energy consumption or aquisition, and whether or not the device is connected 
     * to a charger.
     */
    public synchronized void readData () {
        readHeader ();
        try {
            mjoules  = is.readInt  ();       /* energy stored */
            mwatts   = is.readInt  ();       /* energy consumption/aquisition */
            charging = is.readByte ();       /* charge exchange status */ 
        } catch (Exception e) {
            System.err.println ("[Energy] : Error when reading payload: " + e.toString ());
        }
    }
    
    /**
     * Returns the energy stored, in milliJoules.
     * @return the energy stored in milliJoules as an integer
     */
    public synchronized int getMJoules () { return this.mjoules; }

    /**
     * Returns the estimated current energy consumption (negative values) or aquisition 
     * (positive values), in milliWatts (milliJoules/sec).
     * @return the estimated current energy consumption/aquisition as an integer
     */
    public synchronized int getMWatts () { return this.mwatts; }
    
    /**
     * Returns the charge exchange status: if 1, the device is currently receiving 
     * charge from another energy device. If -1 the device is currently providing 
     * charge to another energy device. If 0, the device is not exchanging charge 
     * with an another device.
     * @return the charge exchange status as a byte
     */
    public synchronized byte getCharging () { return this.charging; }

    /**
     * Configuration request: Controll recharging.
     * <br><br>
     * See the player_energy_command structure from player.h
     * @param enable_input boolean controlling recharging. If FALSE, recharging is 
     * disabled. Defaults to TRUE 
     * @param enable_output boolean controlling whether others can recharge from this 
     * device. If FALSE, charging others is disabled. Defaults to TRUE.
     */
    public void getMapData (byte enable_input, byte enable_output) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 2);     /* 2 byte payload */
            os.writeByte (enable_input);
            os.writeByte (enable_output);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Energy] : Couldn't send command: " + e.toString ());
        }
    }
}
