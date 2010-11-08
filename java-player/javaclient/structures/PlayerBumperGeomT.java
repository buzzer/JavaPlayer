/*
 *  Player Java Client - PlayerBumperGeomT.java
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
 * $Id: PlayerBumperGeomT.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient.structures;

/**
 * To query the geometry of a bumper array, give the following request, filling in only the 
 * subtype.  The server will repond with the other fields filled in. <br />
 * (see the player_bumper_geom structure from player.h)
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class PlayerBumperGeomT {
    private short bumperCount;                    /* the number of valid bumper definitions */
    /* geometry of each bumper */
    private PlayerBumperDefineT[] data = new PlayerBumperDefineT[javaclient.BumperInterface.PLAYER_BUMPER_MAX_SAMPLES];
    
    /**
     * 
     * @return the number of valid bumper definitions 
     */
    public synchronized short getBumperCount () {
    	return this.bumperCount;
    }
    
    /**
     * 
     * @param newbumpercount the number of valid bumper definitions 
     */
    public synchronized void setBumperCount (short newbumpercount) {
    	this.bumperCount = newbumpercount;
    }
    
    /**
     * 
     * @return an array of bumper geometries 
     */
    public synchronized PlayerBumperDefineT[] getData () {
    	return this.data;
    }
    
    /**
     * 
     * @param newdata array of bumper geometries
     */
    public synchronized void setData (PlayerBumperDefineT[] newdata) {
    	this.data = newdata;
    }
}