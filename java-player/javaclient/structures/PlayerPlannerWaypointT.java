/*
 *  Player Java Client - PlayerPlannerWaypointT.java
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
 * $Id: PlayerPlannerWaypointT.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient.structures;

/**
 * Waypoint structure. <br />
 * (see the player_planner_waypoint structure from player.h)
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 * </ul>
 */
public class PlayerPlannerWaypointT {
    private int x, y, a;
    
    public synchronized int getX () {
        return this.x;
    }

    public synchronized void setX (int newx) {
        this.x = newx;
    }

    public synchronized int getY () {
        return this.y;
    }

    public synchronized void setY (int newy) {
        this.y = newy;
    }

    public synchronized int getA () {
        return this.a;
    }

    public synchronized void setA (int newa) {
        this.a = newa;
    }
}
