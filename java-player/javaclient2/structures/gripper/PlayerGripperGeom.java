/*
 *  Player Java Client 2 - PlayerGripperGeom.java
 *  Copyright (C) 2006 Radu Bogdan Rusu
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
 * $Id: PlayerGripperGeom.java 34 2006-02-15 17:51:14Z veedee $
 *
 */

package javaclient2.structures.gripper;

import javaclient2.structures.*;

/**
 * Request/reply: get geometry
 * The geometry (pose and size) of the gripper device can be queried
 * by sending a null PLAYER_GRIPPER_REQ_GET_GEOM request.
 * 
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v2.0 - Player 2.0 supported
 * </ul>
 */
public class PlayerGripperGeom implements PlayerConstants {

    // Gripper pose, in robot cs (m, m, rad). 
    private PlayerPose pose;
    // Gripper dimensions (m, m). 
    private PlayerBbox size;


    /**
     * @return  Gripper pose, in robot cs (m, m, rad). 
     **/
    public synchronized PlayerPose getPose () {
        return this.pose;
    }

    /**
     * @param newPose  Gripper pose, in robot cs (m, m, rad). 
     *
     */
    public synchronized void setPose (PlayerPose newPose) {
        this.pose = newPose;
    }
    /**
     * @return  Gripper dimensions (m, m). 
     **/
    public synchronized PlayerBbox getSize () {
        return this.size;
    }

    /**
     * @param newSize  Gripper dimensions (m, m). 
     *
     */
    public synchronized void setSize (PlayerBbox newSize) {
        this.size = newSize;
    }

}