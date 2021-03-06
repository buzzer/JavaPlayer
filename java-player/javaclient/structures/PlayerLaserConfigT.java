/*
 *  Player Java Client - PlayerLaserConfigT.java
 *  Copyright (C) 2002-2005 Maxim A. Batalin & Radu Bogdan Rusu
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
 * $Id: PlayerLaserConfigT.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient.structures;

/**
 * The scan configuration (resolution, aperture, etc) can be queried using the 
 * PLAYER_LASER_GET_CONFIG request and modified using the PLAYER_LASER_SET_CONFIG request.  
 * Read the documentation for your driver to determine what configuration values are permissible.<br />
 * (see the player_laser_config structure from player.h)
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class PlayerLaserConfigT {
    private short minAngle;     /* start angle (0.01 degrees). Valid range is -9000 to +9000 */
    private short maxAngle;     /* end angle (0.01 degrees). Valid range is -9000 to +9000 */
    private int   resolution;   /* scan resolution (0.01 degrees). Valid resolutions are 25, 50, 100 */
    private int   rangeRes;     /* range resolution. Valid: 1, 10, 100 (For mm, cm, dm) */
    private byte  intensity;    /* enable reflection intensity data */
    
    /**
     * 
     * @return the start angle
     */
    public synchronized short getMinAngle () {
        return this.minAngle;
    }

    /**
     * 
     * @param newminangle start angle (0.01 degrees). Valid range is -9000 to +9000
     */
    public synchronized void setMinAngle (short newminangle) {
        this.minAngle = newminangle;
    }

    /**
     * 
     * @return the end angle
     */
    public synchronized short getMaxAngle () {
        return this.maxAngle;
    }

    /**
     * 
     * @param newmaxangle end angle (0.01 degrees). Valid range is -9000 to +9000
     */
    public synchronized void setMaxAngle (short newmaxangle) {
        this.maxAngle = newmaxangle;
    }

    /**
     * 
     * @return the scan resolution
     */
    public synchronized int getResolution () {
        return this.resolution;
    }

    /**
     * 
     * @param newresolution scan resolution (0.01 degrees). Valid resolutions are 25, 50, 100
     */
    public synchronized void setResolution (int newresolution) {
        this.resolution = newresolution;
    }
    
    /**
     * 
     * @return the range resolution
     */
    public synchronized int getRangeRes () {
        return this.rangeRes;
    }
    
    /**
     * 
     * @param newrangeres range resolution. Valid: 1, 10, 100 (For mm, cm, dm)
     */
    public synchronized void setRangeRes (int newrangeres) {
        this.rangeRes = newrangeres;
    }
    
    /**
     * 
     * @return whether the reflection intensity data is disabled or enabled (0/1) 
     */
    public synchronized byte getIntensity () {
        return this.intensity;
    }

    /**
     * 
     * @param newintensity enable reflection intensity data
     */
    public synchronized void setIntensity (byte newintensity) {
        this.intensity = newintensity;
    }

}
