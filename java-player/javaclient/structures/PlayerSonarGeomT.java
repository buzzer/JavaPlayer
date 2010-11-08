/*
 *  Player Java Client - PlayerSonarGeomT.java
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
 * $Id: PlayerSonarGeomT.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient.structures;

/**
 * To query the geometry of the sonar transducers, use this request, but only fill in the subtype.
 * The server will reply with the other fields filled in. <br />
 * (see the player_sonar_geom structure from player.h)
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class PlayerSonarGeomT {
    private short poseCount;            /* the number of valid poses */
    /* pose of each sonar, in robot cs (mm, mm, degrees) */
    private short poses[][] = new short[javaclient.SonarInterface.PLAYER_SONAR_MAX_SAMPLES][3];
    
    /**
     * 
     * @return the number of valid poses
     */
    public synchronized short getPoseCount () {
    	return this.poseCount;
    }
    
    /**
     * 
     * @param newposecount number of valid poses
     */
    public synchronized void setPoseCount (short newposecount) {
    	this.poseCount = newposecount;
    }
    
    /**
     * 
     * @return the pose of each sonar, in robot cs (mm, mm, degrees) 
     */
    public synchronized short[][] getPoses () {
    	return this.poses;
    }
    
    /**
     * 
     * @param newposes pose of each sonar, in robot cs (mm, mm, degrees) 
     */
    public synchronized void setPoses (short[][] newposes) {
    	this.poses = newposes;
    }
}