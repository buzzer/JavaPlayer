/*
 *  Player Java Client - GripperInterface.java
 *  Copyright (C) 2003-2005 Maxim Batalin & Radu Bogdan Rusu
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
 * $Id: GripperInterface.java 14 2005-05-17 08:59:35Z veedee $
 *
 */
package javaclient;

/**
 * The gripper interface provides access to a robotic gripper. This interface is VERY 
 * Pioneer-specific, and should really be generalized.
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class GripperInterface extends PlayerDevice {

    private final short PLAYER_GRIPPER_CODE = PlayerClient.PLAYER_GRIPPER_CODE; /* gripper */ 

    /* the player message types (see player.h) */
    private final short PLAYER_MSGTYPE_CMD = PlayerClient.PLAYER_MSGTYPE_CMD;
    
    /* the current gripper lift and breakbeam state */ 
    private byte state;
    
    /* the current gripper lift and breakbeam state */
    private byte beams;
    
    /**
     * Constructor for GripperInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public GripperInterface (PlayerClient pc, short indexOfDevice) {
        super (pc);
        device = PLAYER_GRIPPER_CODE;
        index  = indexOfDevice;
    }
    
    /**
     * Read the current state of the gripper; the format is given below. Note that the exact 
     * interpretation of this data may vary depending on the details of your gripper and how it is 
     * connected to your robot (e.g., General I/O vs. User I/O for the Pioneer gripper).
     * <br><br>
     * The following list defines how the data can be interpreted for some Pioneer robots and Stage:
     * <br>
     * <ul>
     *          <li>state (unsigned byte)
     *          <ul>
     *              <li>bit 0: Paddles open
     *              <li>bit 1: Paddles closed
     *              <li>bit 2: Paddles moving
     *              <li>bit 3: Paddles error
     *              <li>bit 4: Lift is up
     *              <li>bit 5: Lift is down
     *              <li>bit 6: Lift is moving
     *              <li>bit 7: Lift error
     *          </ul>
     *          <li>beams (unsigned byte)
     *          <ul>
     *              <li>bit 0: Gripper limit reached
     *              <li>bit 1: Lift limit reached
     *              <li>bit 2: Outer beam obstructed
     *              <li>bit 3: Inner beam obstructed
     *              <li>bit 4: Left paddle open
     *              <li>bit 5: Right paddle open
     * </ul>
     */
    public synchronized void readData () {
        try {
            readHeader ();
            state = is.readByte ();     /* the current gripper lift and breakbeam state */
            beams = is.readByte ();     /* the current gripper lift and breakbeam state */
        } catch (Exception e) {
    	    System.err.println ("[Gripper] : Error when reading payload: " + e.toString ());
        }
    }
    
    /**
     * The gripper interface accepts 2-byte commands, the format of which is given below. These two 
     * bytes are sent directly to the gripper; refer to Table 3-3 page 10 in the Pioneer 2 Gripper 
     * Manual for a list of commands. The first byte is the command. The second is the argument for 
     * the LIFTcarry and GRIPpress commands, but for all others it is ignored.
     * @param cmd the command
     * @param arg the argument for the LIFTcarry and GRIPpress commands 
     */
    public void setGripper (int cmd, int arg) {
    	try {
    	    sendHeader (PLAYER_MSGTYPE_CMD, 2);       /* 2 bytes payload */
            os.writeByte ((byte)cmd);
            os.writeByte ((byte)arg);
            os.flush ();
        } catch (Exception e) {
        	System.err.println ("[Gripper] : Couldn't send gripper command request: " + 
                    e.toString ());
        }
    }
    
    /**
     * Get the gripper's state. 
     * @return the gripper's state as a byte
     */
    public synchronized byte getState () { return state; }
    /**
     * Get the gripper's beams.
     * @return the gripper's beams as a byte
     */
    public synchronized byte getBeams () { return beams; }
    
}
