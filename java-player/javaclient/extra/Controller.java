/*
 *  Player Java Client - Controller.java
 *  Copyright (C) 2005 Radu Bogdan Rusu
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
 * $Id: Controller.java 17 2005-05-23 13:13:22Z veedee $
 *
 */
package javaclient.extra;

/**
 * Abstract controller implementation. Used as a starting point for P, PI, PD, PID controllers.
 * @author Radu Bogdan Rusu
 */
public abstract class Controller {

    /** set the controller's goal */
    protected double goal;
    
    /** the sum of all errors so far */
    protected double eSum;
    
    /** current error */
    protected double currE;
    /** last error */
    protected double lastE;

    /**
     * Set a new goal for the controller.
     * @param newGoal the new goal for the controller
     */
    public void setGoal (double newGoal) {
        this.goal = newGoal;
    }

    
    /**
     * Get the difference between the current error and the last error.
     * @return the difference between the current error and the last error
     */
    protected double deltaE () {
        return (currE - lastE);
    }
}
