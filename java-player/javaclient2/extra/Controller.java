/*
 *  Player Java Client 2 - Controller.java
 *  Copyright (C) 2002-2006 Radu Bogdan Rusu, Maxim Batalin
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
 * $Id: Controller.java 47 2006-03-06 08:33:31Z veedee $
 *
 */
package javaclient2.extra;

/**
 * Abstract controller implementation. Used as a starting point for P, PI, PD, PID controllers.
 * @author Radu Bogdan Rusu
 */
public abstract class Controller {

    /** set the controller's goal */
    protected float goal;
    
    /** the sum of all errors so far */
    protected float eSum;
    
    /** current error */
    protected float currE;
    /** last error */
    protected float lastE;

    /**
     * Set a new goal for the controller.
     * @param newGoal the new goal for the controller
     */
    public void setGoal (float newGoal) {
        this.goal = newGoal;
    }

    
    /**
     * Get the difference between the current error and the last error.
     * @return the difference between the current error and the last error
     */
    protected float deltaE () {
        return (currE - lastE);
    }
}
