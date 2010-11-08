/*
 *  Player Java Client - PlayerSimulationPose2D.java
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
 * $Id: PlayerSimulationPose2D.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient.structures;

/**
 * To set or get the pose of an object in a simulator, use this message type. If the subtype 
 * is PLAYER_SIMULATION_SET_POSE2D, the server will ask the simulator to move the named 
 * object to the location specified by (x,y,a) and return ACK. If the subtype is 
 * PLAYER_SIMULATION_GET_POSE2D, the server will attempt to locate the named object and reply 
 * with the same packet with (x,y,a) filled in. For all message subtypes, if the named object 
 * does not exist, or some other error occurs, the request should reply NACK. <br />
 * (see the player_simulation_pose2d_req structure from player.h)
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 * </ul>
 */
public class PlayerSimulationPose2D {
    private String name;                 /* the identifier of the object we want to locate */
    private int x, y, theta;             /* the desired pose or returned pose in (mm,mm,degrees) */
    
    /**
     * 
     * @return the identifier of the object we want to locate
     */
    public synchronized String getName () {
    	return this.name;
    }
    
    /**
     * 
     * @param newname identifier of the object we want to locate
     */
    public synchronized void setName (String newname) {
    	this.name = newname;
    }
    
    /**
     * 
     * @return the desired pose or returned pose (X)
     */
    public synchronized int getX () {
    	return this.x;
    }
    
    /**
     * 
     * @param newx desired pose or returned pose (X)
     */
    public synchronized void setX (int newx) {
    	this.x = newx;
    }
    
    /**
     * 
     * @return the desired pose or returned pose (Y)
     */
    public synchronized int getY () {
        return this.y;
    }
    
    /**
     * 
     * @param newy desired pose or returned pose (Y)
     */
    public synchronized void setY (int newy) {
        this.y = newy;
    }
    
    /**
     * 
     * @return the desired pose or returned pose (Theta)
     */
    public synchronized int getTheta () {
        return this.theta;
    }
    
    /**
     * 
     * @param newtheta desired pose or returned pose (Theta)
     */
    public synchronized void setTheta (int newtheta) {
        this.theta = newtheta;
    }
}
