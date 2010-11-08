/*
 *  Player Java Client - PositionGeometryTools.java
 *  Copyright (C) 2005 Marius Borodi & Radu Bogdan Rusu
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
 * $Id: PositionGeometryTools.java 24 2005-06-17 10:16:30Z veedee $
 *
 */

package javaclient.extra;

import java.awt.*;

/**
 * Several methods for position geometric calculus.
 * @author Marius Borodi & Radu Bogdan Rusu
 */

public class PositionGeometryTools { 

    /**
     * Calculate the distance between two X and Y points assuming 
     * that their counterparts are 0 using Pitagora's theorem.
     * @param x X's coordonate of the first point (Y=0)
     * @param y Y's coordonate of the second point (X=0)
     * @return distance between [X, 0] and [0, Y] as an integer
     */
    public static int calcDist (int x, int y) {
        double d  = (Math.sqrt (Math.pow(x, 2) + Math.pow (y, 2)));
        return (int)Math.round (d);
    }
    
    /**
     * Calculate the distance between two points (p1 and p2).
     * @param p1 first point
     * @param p2 second point
     * @return the distance between p1 and p2
     */
    public static int calcDist (Point p1, Point p2) {
        int x = p2.x - p1.x;
        int y = p2.y - p1.y;
        return calcDist (x, y);
    }
    
    /**
     * Calculate the X coordinate of a point situated at distance 
     * <i>dist</i>, angle <i>angle</i> from a given point <i>initP</i>.
     * @param initP reference point
     * @param dist distance from the reference point to the desired point 
     * @param angle angle from the reference point to the desired point
     * @return the X coordinate of the point
     */
    public static int calcX (Point initP, int dist, int angle) {
        double tmp = (dist * Math.cos (Math.toRadians (angle)));
        return (initP.x +  (int) Math.round (tmp));
    }
    
    /**
     * Calculate the Y coordinate of a point situated at distance 
     * <i>dist</i>, angle <i>angle</i> from a given point <i>initP</i>.
     * @param initP reference point
     * @param dist distance from the reference point to the desired point 
     * @param angle angle from the reference point to the desired point
     * @return the Y coordinate of the point
     */
    public static int calcY (Point initP,int dist,int angle) {
        double tmp = (dist * Math.sin (Math.toRadians (angle)));
        return (initP.y +  (int) Math.round (tmp));
    }
    
    /**
     * Calculate the coordinates of a point situated at distance 
     * <i>dist</i>, angle <i>angle</i> from a given point <i>initP</i>.
     * @param initP reference point
     * @param dist distance from the reference point to the desired point 
     * @param angle angle from the reference point to the desired point
     * @return the coordinates of the new point as a Point (AWT)
     */
    public static Point calcDistPoint (Point initP, int dist, int angle) {
        double tmp = (dist * Math.cos (Math.toRadians (angle)));
        int x = initP.x +  (int) Math.round (tmp);
        tmp   = (dist * Math.sin (Math.toRadians (angle)));
        int y = initP.y +  (int) Math.round (tmp);
        return new Point (x,y);
    }
    
    /**
     * Calculate the angle between the line determined by the two 
     * points and the horizontal axis.
     * @param p1 First point
     * @param p2 Second point
     * @return the angle as an integer
     */
    public static int calcAngle (Point p1, Point p2) {
    	return (int)Math.round (Math.toDegrees (
                Math.atan2 ((p2.y - p1.y), (p2.x - p1.x))));
    }
}
