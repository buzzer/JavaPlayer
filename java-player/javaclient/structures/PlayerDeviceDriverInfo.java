/*
 *  Player Java Client - PlayerDeviceDriverInfo.java
 *  Copyright (C) 2002-2005 Maxim A. Batalin, Esben H. Ostergaard & Radu Bogdan Rusu
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
 * $Id: PlayerDeviceDriverInfo.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient.structures;

/**
 * To get a name, set the subtype to PLAYER_PLAYER_DRIVERINFO_REQ and set the id field. Player 
 * will return the driver info. <br />
 * (see the player_device_driverinfo structure from player.h)
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class PlayerDeviceDriverInfo {
    private PlayerDeviceIdT devID;              /* the device identifier */
    private String          driverName;         /* the driver name */
    
    /**
     * 
     * @return the device identifier 
     */
    public synchronized PlayerDeviceIdT getDevID () {
    	return this.devID;
    }
    
    /**
     * 
     * @param newdevid device identifier
     */
    public synchronized void setDevID (PlayerDeviceIdT newdevid) {
    	this.devID = newdevid;
    }
    
    /**
     * 
     * @return the driver name
     */
    public synchronized String getDriverName () {
    	return this.driverName;
    }
    
    /**
     * 
     * @param newdrivername driver name
     */
    public synchronized void setDriverName (String newdrivername) {
    	this.driverName = newdrivername;
    }
}
