/*
 *  Player Java Client - PlayerDeviceIdT.java
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
 * $Id: PlayerDeviceIdT.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient.structures;

/**
 * Devices are differentiated internally in Player by these identifiers, and some messages 
 * contain them. <br />
 * (see the player_device_id structure from player.h)
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class PlayerDeviceIdT {
    private short code;                         /* the interface provided by the device */
    private short index;                        /* the index of the device */
    private short port;                         /* the TCP port of the device */
    
    /**
     * 
     * @return the interface provided by the device
     */
    public synchronized short getCode () {
        return this.code;
    }
    
    /**
     * 
     * @param newcode interface provided by the device
     */
    public synchronized void setCode (short newcode) {
        this.code = newcode;
    }
    
    /**
     * 
     * @return the index of the device 
     */
    public synchronized short getIndex () {
        return this.index;
    }
    
    /**
     * 
     * @param newindex index of the device 
     */
    public synchronized void setIndex (short newindex) {
        this.index = newindex;
    }
    
    /**
     * 
     * @return the TCP port of the device 
     */
    public synchronized short getPort () {
        return this.port;
    }
    
    /**
     * 
     * @param newport TCP port of the device 
     */
    public synchronized void setPort (short newport) {
        this.port = newport;
    }
    
}
