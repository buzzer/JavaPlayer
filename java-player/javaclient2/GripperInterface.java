/*
 *  Player Java Client 2 - GripperInterface.java
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
 * $Id: GripperInterface.java 84 2007-11-25 23:25:15Z veedee $
 *
 */
package javaclient2;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javaclient2.xdr.OncRpcException;
import javaclient2.xdr.XdrBufferDecodingStream;
import javaclient2.xdr.XdrBufferEncodingStream;

import javaclient2.structures.PlayerMsgHdr;
import javaclient2.structures.PlayerPose;
import javaclient2.structures.PlayerBbox;
import javaclient2.structures.gripper.PlayerGripperGeom;
import javaclient2.structures.gripper.PlayerGripperData;

/**
 * The gripper interface provides access to a robotic gripper. This interface is VERY 
 * Pioneer-specific, and should really be generalized.
 * @author Radu Bogdan Rusu, Maxim Batalin
 * @version
 * <ul>
 *      <li>v2.0 - Player 2.0 supported
 * </ul>
 */
public class GripperInterface extends PlayerDevice {

    private static final boolean isDebugging = PlayerClient.isDebugging;

    // Logging support
	private Logger logger = Logger.getLogger (GripperInterface.class.getName ());

    private PlayerGripperData pgdata;
    private boolean           readyPgdata = false;
    private PlayerGripperGeom pggeom;
    private boolean           readyPggeom = false;
    
    /**
     * Constructor for GripperInterface.
     * @param pc a reference to the PlayerClient object
     */
    public GripperInterface (PlayerClient pc) { super (pc); }
    
    /**
     * The gripper interface returns 2 bytes that represent the current state 
     * of the gripper; the format is given below. Note that the exact 
     * interpretation of this data may vary depending on the details of your 
     * gripper and how it is connected to your robot (e.g., General I/O vs. 
     * User I/O for the Pioneer gripper).
     * <br><br>
     * The following list defines how the data can be interpreted for some 
     * Pioneer robots and Stage:
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
    public synchronized void readData (PlayerMsgHdr header) {
        try {
        	switch (header.getSubtype ()) {
        		case PLAYER_GRIPPER_DATA_STATE: {
               this.timestamp = header.getTimestamp();
               
               pgdata = new PlayerGripperData ();
        			
        			// Buffer for reading gripper data
        			byte[] buffer = new byte[8];
        			// Read gripper data
        			is.readFully (buffer, 0, 8);
        			
        			// Begin decoding the XDR buffer
        			XdrBufferDecodingStream xdr = new XdrBufferDecodingStream (buffer);
        			xdr.beginDecoding ();
        			pgdata.setState (xdr.xdrDecodeInt ());
        			pgdata.setBeams (xdr.xdrDecodeInt ());
        			xdr.endDecoding   ();
        			xdr.close ();
        			
        			readyPgdata = true;
        			break;
        		}
        	}
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Gripper] : Error reading payload: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Gripper] : Error while XDR-decoding payload: " + 
        				e.toString(), e);
        }
    }
    
    /**
     * The gripper interface accepts 2-byte commands, the format of which is 
     * given below. These two bytes are sent directly to the gripper; refer 
     * to Table 3-3 page 10 in the Pioneer 2 Gripper Manual for a list of 
     * commands. The first byte is the command. The second is the argument 
     * for the LIFTcarry and GRIPpress commands, but for all others it is 
     * ignored.
     * <br><br>
     * Possible command codes:
     * <br>
     * <ul>
     * 		<li>GRIPopen  - 1
     * 		<li>GRIPclose - 2
     * 		<li>GRIPstop  - 3
     * 		<li>LIFTup    - 4
     * 		<li>LIFTdown  - 5
     * 		<li>LIFTstop  - 6
     * 		<li>GRIPstore - 7
     * 		<li>GRIPdeploy- 8
     * 		<li>GRIPhalt  - 15
     * 		<li>GRIPpress - 16
     * 		<li>GRIPcarry - 17
     * </ul>
     * @param cmd the command
     * @param arg the argument for the LIFTcarry and GRIPpress commands 
     */
    public void setGripper (int cmd, int arg) {
    	try {
    		sendHeader (PLAYER_MSGTYPE_CMD, PLAYER_GRIPPER_CMD_STATE, 8);
    		XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (8);
    		xdr.beginEncoding (null, 0);
    		xdr.xdrEncodeInt (cmd);
    		xdr.xdrEncodeInt (arg);
    		xdr.endEncoding ();
    		os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
    		xdr.close ();
    		os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Gripper] : Couldn't send state command request: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Gripper] : Error while XDR-encoding state command request: "
        				+ e.toString(), e);
        }
    }

    /**
     * Request/reply: Get geometry.
     * <br><br>
     * THe geometry (pose and size) of the gripper device can be queried by 
     * sending a null PLAYER_GRIPPER_REQ_GET_GEOM request.
     * <br><br>
     * See the player_gripper_geom structure from player.h
     */
    public void queryGeometry () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_GRIPPER_REQ_GET_GEOM, 0);
            os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Gripper] : Couldn't send PLAYER_GRIPPER_REQ_GET_GEOM " +
        				"command: " + e.toString(), e);
        }
    }
    
    /**
     * Handle acknowledgement response messages
     * @param header Player header
     */
    public void handleResponse (PlayerMsgHdr header) {
        try {
            switch (header.getSubtype ()) {
                case PLAYER_GRIPPER_REQ_GET_GEOM: {
                	PlayerPose pp = new PlayerPose ();
                	PlayerBbox pb = new PlayerBbox ();
                	
                	// Buffer for reading configuration data
                	byte[] buffer = new byte[20];
                	// Read configuration data
                	is.readFully (buffer, 0, 20);
                	
                	// Begin decoding the XDR buffer
                	XdrBufferDecodingStream xdr = new XdrBufferDecodingStream (buffer);
                	xdr.beginDecoding ();
                	pp.setPx (xdr.xdrDecodeFloat ());
                	pp.setPy (xdr.xdrDecodeFloat ());
                	pp.setPa (xdr.xdrDecodeFloat ());
                	pb.setSw (xdr.xdrDecodeFloat ());
                	pb.setSl (xdr.xdrDecodeFloat ());
                	xdr.endDecoding   ();
                	xdr.close ();
                	
                	pggeom = new PlayerGripperGeom ();
                	pggeom.setPose (pp);
                	pggeom.setSize (pb);
                	
                	readyPggeom = true;
                	break;
                }
                default:{
                	if (isDebugging)
                		logger.log (Level.FINEST, "[Gripper]Debug] : " +
                				"Unexpected response " + header.getSubtype () + 
                				" of size = " + header.getSize ());
                    break;
                }
            }
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Gripper] : Error reading payload: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Gripper] : Error while XDR-decoding payload: " + 
        				e.toString(), e);
        }
    }

    /**
     * Get the Gripper data.
     * @return an object of type PlayerGripperData containing the requested data 
     */
    public PlayerGripperData getData () { return this.pgdata; }
    
    /**
     * Get the Gripper geometry data.
     * @return an object of type PlayerGripperGeom containing the requested data 
     */
    public PlayerGripperGeom getGeom () { return this.pggeom; }
    
    /**
     * Check if data is available.
     * @return true if ready, false if not ready 
     */
    public boolean isDataReady () {
        if (readyPgdata) {
        	readyPgdata = false;
            return true;
        }
        return false;
    }
    
    /**
     * Check if geometry data is available.
     * @return true if ready, false if not ready 
     */
    public boolean isGeomReady () {
        if (readyPggeom) {
        	readyPggeom = false;
            return true;
        }
        return false;
    }
    
}
