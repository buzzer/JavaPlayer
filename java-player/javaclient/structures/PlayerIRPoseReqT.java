/*
 *  Player Java Client - PlayerIRPoseReqT.java
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
 * $Id: PlayerIRPoseReqT.java 21 2005-06-01 19:05:51Z veedee $
 *
 */
package javaclient.structures;

/**
 * To query the pose of the IRs, use this request, filling in only the subtype.  
 * The server will respond with the other fields filled in.<br/>
 * (see the player_ir_pose_req structure from player.h)
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 * </ul>
 */
public class PlayerIRPoseReqT {
    /* the number of ir samples returned by this robot */
    private short poseCount;
    
    /* the pose of each IR detector on this robot (mm, mm, degrees) */
    private short poses[][] = new short[javaclient.IRInterface.PLAYER_IR_MAX_SAMPLES][3];
    
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
     * @return the pose of each IR detector on this robot (mm, mm, degrees) 
     */
    public synchronized short[][] getPoses () {
        return this.poses;
    }
    
    /**
     * 
     * @param newposes pose of each IR detector on this robot (mm, mm, degrees) 
     */
    public synchronized void setPoses (short[][] newposes) {
        this.poses = newposes;
    }
}