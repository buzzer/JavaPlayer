/*
 *  Player Java Client - PlayerDeviceDevlistT.java
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
 * $Id: PlayerDeviceDevlistT.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient.structures;

/**
 * It's useful for applications such as viewer programs and test suites that tailor behave 
 * differently depending on which devices are available.  To request the list, set the subtype to
 * PLAYER_PLAYER_DEVLIST_REQ and leave the rest of the fields blank. Player will return a packet 
 * with subtype PLAYER_PLAYER_DEVLIST_REQ with the fields filled in. <br />
 * (see the player_device_devlist structure from player.h)
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class PlayerDeviceDevlistT {
    private short             deviceCount;      /* the number of devices */
    private PlayerDeviceIdT[] devList;          /* the list of available devices */
    
    /**
     * 
     * @return the number of devices 
     */
    public synchronized short getDeviceCount () {
    	return this.deviceCount;
    }
    
    /**
     * 
     * @param newdevicecount number of devices 
     */
    public synchronized void setDeviceCount (short newdevicecount) {
    	this.deviceCount = newdevicecount;
    }
    
    /**
     * 
     * @return the list of available devices
     */
    public synchronized PlayerDeviceIdT[] getDevList () {
    	return this.devList;
    }
    
    /**
     * 
     * @param newdevlist list of available devices
     */
    public synchronized void setDevList (PlayerDeviceIdT[] newdevlist) {
    	this.devList = newdevlist;
    }
}