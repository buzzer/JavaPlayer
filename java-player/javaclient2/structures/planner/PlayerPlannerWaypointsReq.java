/*
 *  Player Java Client 2 - PlayerPlannerWaypointsReq.java
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
 * $Id: PlayerPlannerWaypointsReq.java 34 2006-02-15 17:51:14Z veedee $
 *
 */

package javaclient2.structures.planner;

import javaclient2.structures.*;

/**
 * Request/reply: Get waypoints 
 * To retrieve the list of waypoints, send a null
 * PLAYER_PLANNER_REQ_GET_WAYPOINTS request.
 * 
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v2.0 - Player 2.0 supported
 * </ul>
 */
public class PlayerPlannerWaypointsReq implements PlayerConstants {

    // Number of waypoints to follow 
    private int waypoints_count;
    // The waypoints 
    private PlayerPose[] waypoints = new PlayerPose[PLAYER_PLANNER_MAX_WAYPOINTS];


    /**
     * @return  Number of waypoints to follow 
     **/
    public synchronized int getWaypoints_count () {
        return this.waypoints_count;
    }

    /**
     * @param newWaypoints_count  Number of waypoints to follow 
     *
     */
    public synchronized void setWaypoints_count (int newWaypoints_count) {
        this.waypoints_count = newWaypoints_count;
    }
    /**
     * @return  The waypoints 
     **/
    public synchronized PlayerPose[] getWaypoints () {
        return this.waypoints;
    }

    /**
     * @param newWaypoints  The waypoints 
     *
     */
    public synchronized void setWaypoints (PlayerPose[] newWaypoints) {
        this.waypoints = newWaypoints;
    }

}