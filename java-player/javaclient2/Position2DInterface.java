/*
 *  Player Java Client 2 - Position2DInterface.java
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
 * $Id: Position2DInterface.java 84 2007-11-25 23:25:15Z veedee $
 *
 */
package javaclient2;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javaclient2.xdr.OncRpcException;
import javaclient2.xdr.XdrBufferDecodingStream;
import javaclient2.xdr.XdrBufferEncodingStream;
import javaclient2.structures.position2d.PlayerPosition2dData;
import javaclient2.structures.position2d.PlayerPosition2dGeom;
import javaclient2.structures.position2d.PlayerPosition2dCmdPos;
import javaclient2.structures.position2d.PlayerPosition2dCmdVel; 
import javaclient2.structures.PlayerMsgHdr;
import javaclient2.structures.PlayerPose;
import javaclient2.structures.PlayerBbox;

/**
 * The position2d interface is used to control a mobile robot bases in 2D. 
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v2.0 - Player 2.0 supported
 * </ul>
 */
public class Position2DInterface extends AbstractPositionDevice {
    
    private static final boolean isDebugging = PlayerClient.isDebugging;

    // Logging support
	private Logger logger = Logger.getLogger (Position2DInterface.class.getName ());

    private PlayerPosition2dData pp2ddata;
    private boolean              readyPp2ddata = false;
	private PlayerPosition2dGeom pp2dgeom; 
    private boolean              readyPp2dgeom = false;
    
    /**
     * Constructor for Position2DInterface.
     * @param pc a reference to the PlayerClient object
     */
    protected Position2DInterface (PlayerClient pc) { super(pc); }
    
    /**
     * Read the position2d data values (state or geom).
     */
    public synchronized void readData (PlayerMsgHdr header) {
        try {
        	switch (header.getSubtype ()) {
        		case PLAYER_POSITION2D_DATA_STATE: {
               this.timestamp = header.getTimestamp();
               
               // Buffer for reading pos, vel and stall
        			byte[] buffer = new byte[12+12+4];
        			// Read pos, vel and stall
        			is.readFully (buffer, 0, 12+12+4);
        			
        			pp2ddata = new PlayerPosition2dData (); 
        			PlayerPose pos = new PlayerPose ();
        			PlayerPose vel = new PlayerPose ();
        			
        			// Begin decoding the XDR buffer
        			XdrBufferDecodingStream xdr = new XdrBufferDecodingStream (buffer);
        			xdr.beginDecoding ();
        			
        			// position [m, m, rad]
        			pos.setPx (xdr.xdrDecodeFloat ());
        			pos.setPy (xdr.xdrDecodeFloat ());
        			pos.setPa (xdr.xdrDecodeFloat ());
        			pp2ddata.setPos (pos);
        			// translational velocities [m/s, m/s, rad/s]
        			vel.setPx (xdr.xdrDecodeFloat ());
        			vel.setPy (xdr.xdrDecodeFloat ());
        			vel.setPa (xdr.xdrDecodeFloat ());
        			pp2ddata.setVel (vel);
        			// motors stall
        			pp2ddata.setStall (xdr.xdrDecodeByte ());
        			xdr.endDecoding   ();
        			xdr.close ();
        			
        			readyPp2ddata = true;
        			break;
        		}
        		case PLAYER_POSITION2D_DATA_GEOM: {
               this.timestamp = header.getTimestamp();
               
               readGeom ();
        	    	readyPp2dgeom = true;
        			break;
        		}
        	}
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Error reading payload: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-decoding payload: " + 
        				e.toString(), e);
        }
    }
    
    /**
     * The position interface accepts new positions for the robot's motors 
     * (drivers may support position control, speed control or both).
     * <br><br>
     * See the player_position2d_cmd_pos structure from player.h
     * @param pos a PlayerPose structure containing the position data 
     * (x, y, yaw) [m, m, rad]
     * @param vel a PlayerPose structure containing the velocity data
     * (velocity at which to move to the position [m/s] or [rad/s]) 
     * @param state motor state (zero is either off or locked, depending on the driver)
     */
    public void setPosition (PlayerPose pos, PlayerPose vel, int state) {
        try {
        	sendHeader (PLAYER_MSGTYPE_CMD, PLAYER_POSITION2D_CMD_POS, 24+4);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (24+4);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeFloat (pos.getPx ());
        	xdr.xdrEncodeFloat (pos.getPy ());
        	xdr.xdrEncodeFloat (pos.getPa ());
        	xdr.xdrEncodeFloat (vel.getPx ());
        	xdr.xdrEncodeFloat (vel.getPy ());
        	xdr.xdrEncodeFloat (vel.getPa ());
        	xdr.xdrEncodeByte ((byte)state);
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't send position command: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding position command: " + 
        				e.toString(), e);
        }
    }

    /**
     * The position interface accepts new positions for the robot's motors 
     * (drivers may support position control, speed control or both).
     * <br><br>
     * See the player_position2d_cmd_pos structure from player.h
     * @param pp2dcp a PlayerPosition2dCmdPos structure holding the required data
     */
    public void setPosition (PlayerPosition2dCmdPos pp2dcp) {
        try {
        	sendHeader (PLAYER_MSGTYPE_CMD, PLAYER_POSITION2D_CMD_POS, 24+4);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (24+4);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeFloat (pp2dcp.getPos ().getPx ());
        	xdr.xdrEncodeFloat (pp2dcp.getPos ().getPy ());
        	xdr.xdrEncodeFloat (pp2dcp.getPos ().getPa ());
        	xdr.xdrEncodeFloat (pp2dcp.getVel ().getPx ());
        	xdr.xdrEncodeFloat (pp2dcp.getVel ().getPy ());
        	xdr.xdrEncodeFloat (pp2dcp.getVel ().getPa ());
        	xdr.xdrEncodeByte ((byte)pp2dcp.getState ());
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't send position command: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding position command: " + 
        				e.toString(), e);
        }
    }

    /**
     * The position interface accepts new velocities for the robot's motors 
     * (drivers may support position control, speed control or both).
     * <br><br>
     * See the player_position2d_cmd_vel structure from player.h
     * @param vel a PlayerPose structure containing the translational 
     * velocities (x, y, yaw) [m/s, m/s, rad/s]
     * @param state motor state (zero is either off or locked, depending on the driver)
     */
    public void setVelocity (PlayerPose vel, int state) {
        try {
        	sendHeader (PLAYER_MSGTYPE_CMD, PLAYER_POSITION2D_CMD_VEL, 12+4);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (12+4);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeFloat (vel.getPx ());
        	xdr.xdrEncodeFloat (vel.getPy ());
        	xdr.xdrEncodeFloat (vel.getPa ());
        	xdr.xdrEncodeByte ((byte)state);
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't send velocity command: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding velocity command: " + 
        				e.toString(), e);
        }
    }
    
    /**
     * The position interface accepts new velocities for the robot's motors 
     * (drivers may support position control, speed control or both).
     * <br><br>
     * See the player_position2d_cmd_vel structure from player.h
     * @param pp2dcv a PlayerPosition2dCmdVel structure holding the required data
     */
    public void setVelocity (PlayerPosition2dCmdVel pp2dcv) {
        try {
        	sendHeader (PLAYER_MSGTYPE_CMD, PLAYER_POSITION2D_CMD_VEL, 12+4);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (12+4);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeFloat (pp2dcv.getVel ().getPx ());
        	xdr.xdrEncodeFloat (pp2dcv.getVel ().getPy ());
        	xdr.xdrEncodeFloat (pp2dcv.getVel ().getPa ());
        	xdr.xdrEncodeByte ((byte)pp2dcv.getState ());
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't send velocity command: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding velocity command: " + 
        				e.toString(), e);
        }
    }

    /**
     * The position interface accepts new carlike velocity (speed and turning angle)
     * for the robot's motors (only supported by some drivers).
     * <br><br>
     * See the player_position2d_cmd_car structure from player.h
     * @param velocity forward velocity (m/s)
     * @param angle turning angle (rad)
     */
    public void setCarCMD (double velocity, double angle) {
        try {
        	sendHeader (PLAYER_MSGTYPE_CMD, PLAYER_POSITION2D_CMD_CAR, 16);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (16);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeDouble (velocity);
        	xdr.xdrEncodeDouble (angle);
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't send carlike command: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding carlike command: " + 
        				e.toString(), e);
        }
    }
    
    private void readGeom () {
    	try {
    		// Buffer for reading pose and size
    		byte[] buffer = new byte[12+8];
    		// Read pose and size
    		is.readFully (buffer, 0, 12+8);
    		
    		pp2dgeom = new PlayerPosition2dGeom (); 
    		PlayerPose pose = new PlayerPose ();
    		PlayerBbox size = new PlayerBbox ();
    		
    		// Begin decoding the XDR buffer
    		XdrBufferDecodingStream xdr = new XdrBufferDecodingStream (buffer);
    		xdr.beginDecoding ();
    		
    		// pose of the robot base [m, m, rad]
    		pose.setPx (xdr.xdrDecodeFloat ());
    		pose.setPy (xdr.xdrDecodeFloat ());
    		pose.setPa (xdr.xdrDecodeFloat ());
    		pp2dgeom.setPose (pose);
    		// dimensions of the base [m, m]
    		size.setSw (xdr.xdrDecodeFloat ());
    		size.setSl (xdr.xdrDecodeFloat ());
    		pp2dgeom.setSize (size);
    		xdr.endDecoding   ();
    		xdr.close ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Error reading geometry data: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-decoding geometry data: " + 
        				e.toString(), e);
        }
    }
    
    /**
     * Request/reply: Query geometry.
     */
    public void queryGeometry () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_GET_GEOM, 0);
            os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't request " + 
        				"PLAYER_POSITION2D_REQ_GET_GEOM: " + 
        				e.toString(), e);
        }
    }

    /**
     * Configuration request: Motor power.
     * <br><br>
     * On some robots, the motor power can be turned on and off from software.
     * <br><br>
     * Be VERY careful with this command! You are very likely to start the robot 
     * running across the room at high speed with the battery charger still attached.
     * @param state 0 for off, 1 for on 
     */
    public void setMotorPower (int state) {
        try {
        	sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_MOTOR_POWER, 4);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (4);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeByte ((byte)state);
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't request " + "" +
        				"PLAYER_POSITION2D_REQ_MOTOR_POWER: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding POWER request: " + 
        				e.toString(), e);
        }
    }
    
    /**
     * Configuration request: Change velocity control.
     * <br><br>
     * Some robots offer different velocity control modes.
     * <br><br>
     * The p2os driver offers two modes of velocity control: separate translational and rotational 
     * control and direct wheel control. When in the separate mode, the robot's microcontroller 
     * internally computes left and right wheel velocities based on the currently commanded 
     * translational and rotational velocities and then attenuates these values to match a nice 
     * predefined acceleration profile. When in the direct mode, the microcontroller simply passes 
     * on the current left and right wheel velocities. Essentially, the separate mode offers 
     * smoother but slower (lower acceleration) control, and the direct mode offers faster but 
     * jerkier (higher acceleration) control. Player's default is to use the direct mode. Set mode 
     * to zero for direct control and non-zero for separate control.
     * <br><br>
     * For the reb driver, 0 is direct velocity control, 1 is for velocity-based heading PD 
     * controller. 
     * @param mode driver-specific mode
     */
    public void setVelocityControl (int mode) {
        try {
        	sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_VELOCITY_MODE, 4);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (4);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeByte ((byte)mode);
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't request " + "" +
        				"PLAYER_POSITION2D_REQ_VELOCITY_MODE: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding VELOCITY_MODE " + 
        				"request: " + e.toString(), e);
        }
    }
    
    /**
     * Configuration request: Reset odometry.
     * <br><br>
     * Resets the robot's odometry to (x,y,theta) = (0,0,0).
     */
    public void resetOdometry () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_RESET_ODOM, 0);
            os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't request " + "" +
        				"PLAYER_POSITION2D_RESET_ODOM_REQ: " + 
        				e.toString(), e);
        }
    }
    
    /**
     * Configuration request: Change control mode.
     * @param state 0 for velocity mode, 1 for position mode
     */
    public void setControlMode (int state) {
        try {
        	sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_POSITION_MODE, 4);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (4);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeInt (state);
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't request " + "" +
        				"PLAYER_POSITION2D_REQ_POSITION_MODE: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding POSITION_MODE " + 
        				"request: " + e.toString(), e);
        }
    }
    
    /**
     * Configuration request: Set odometry.
     * @param pose (x, y, yaw) [m, m, rad]
     */
    public void setOdometry (PlayerPose pose) {
        try {
        	sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_SET_ODOM, 12);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (12);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeFloat (pose.getPx ());
        	xdr.xdrEncodeFloat (pose.getPy ());
        	xdr.xdrEncodeFloat (pose.getPa ());
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't request " + "" +
        				"PLAYER_POSITION2D_REQ_SET_ODOM: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding SET_ODOM request:" + 
        				e.toString(), e);
        }
    }
    
    /**
     * Configuration request: Set velocity PID parameters.
     * @param kp P parameter
     * @param ki I parameter
     * @param kd D parameter
     */
    public void setVelocityPIDParams (float kp, float ki, float kd) {
        try {
        	sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_SPEED_PID, 12);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (12);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeFloat (kp);
        	xdr.xdrEncodeFloat (ki);
        	xdr.xdrEncodeFloat (kd);
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't request " + "" +
        				"PLAYER_POSITION2D_REQ_SPEED_PID: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding SPEED_PID request:" + 
        				e.toString(), e);
        }
    }
    
    /**
     * Configuration request: Set position PID parameters.
     * @param kp P parameter
     * @param ki I parameter
     * @param kd D parameter
     */
    public void setPositionPIDParams (float kp, float ki, float kd) {
        try {
        	sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_POSITION_PID, 12);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (12);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeFloat (kp);
        	xdr.xdrEncodeFloat (ki);
        	xdr.xdrEncodeFloat (kd);
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't request " + "" +
        				"PLAYER_POSITION2D_REQ_POSITION_PID: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding POSITION_PID " +
        				"request:" + e.toString(), e);
        }
    }
    
    /**
     * Configuration request: Set speed profile parameters.
     * @param speed max speed [m/s] 
     * @param acc max acceleration [m/s^2] 
     */
    public void setSpeedProfileParams (float speed, float acc) {
        try {
        	sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_POSITION2D_REQ_SPEED_PROF, 8);
        	XdrBufferEncodingStream xdr = new XdrBufferEncodingStream (8);
        	xdr.beginEncoding (null, 0);
        	xdr.xdrEncodeFloat (speed);
        	xdr.xdrEncodeFloat (acc);
        	xdr.endEncoding ();
        	os.write (xdr.getXdrData (), 0, xdr.getXdrLength ());
        	xdr.close ();
        	os.flush ();
        } catch (IOException e) {
        	throw new PlayerException 
        		("[Position2D] : Couldn't request " + "" +
        				"PLAYER_POSITION2D_REQ_SPEED_PROF: " + 
        				e.toString(), e);
        } catch (OncRpcException e) {
        	throw new PlayerException 
        		("[Position2D] : Error while XDR-encoding SPEED_PROF " +
        				"request:" + e.toString(), e);
        }
    }
    
    
    /**
     * Handle acknowledgement response messages.
     * @param header Player header
     */
    protected void handleResponse (PlayerMsgHdr header) {
        switch (header.getSubtype ()) {
            case PLAYER_POSITION2D_REQ_GET_GEOM: {
            	readGeom ();
    	    	readyPp2dgeom = true;
            	break;
            }
            case PLAYER_POSITION2D_REQ_MOTOR_POWER: {
            	// null response
            	break;
            }
            case PLAYER_POSITION2D_REQ_VELOCITY_MODE: {
            	// null response
            	break;
            }
            case PLAYER_POSITION2D_REQ_POSITION_MODE: {
            	// null response
            	break;
            }
            case PLAYER_POSITION2D_REQ_SET_ODOM: {
            	// null response
                break;
            }
            case PLAYER_POSITION2D_REQ_RESET_ODOM: {
            	// null response
                break;
            }
            case PLAYER_POSITION2D_REQ_SPEED_PID: {
            	// null response
                break;
            }
            case PLAYER_POSITION2D_REQ_POSITION_PID: {
            	// null response
                break;
            }
            case PLAYER_POSITION2D_REQ_SPEED_PROF: {
            	// null response
                break;
            }
            default:{
            	if (isDebugging)
            		logger.log (Level.FINEST, "[Position2D][Debug] : " +
            				"Unexpected response " + header.getSubtype () + 
            				" of size = " + header.getSize ());
                break;
            }
        }
    }
    
    /**
     * Get the Position2D data.
     * @return an object of type PlayerPosition2DData containing the requested data 
     */
    public PlayerPosition2dData getData () { return this.pp2ddata; }
    
    /**
     * Get the geometry data.
     * @return an object of type PlayerPosition2DGeom containing the requested data 
     */
    public PlayerPosition2dGeom getGeom () { return this.pp2dgeom; }
    
    /**
     * Check if data is available.
     * @return true if ready, false if not ready 
     */
    public boolean isDataReady () {
        if (readyPp2ddata) {
        	readyPp2ddata = false;
            return true;
        }
        return false;
    }
    
    /**
     * Check if geometry data is available.
     * @return true if ready, false if not ready 
     */
    public boolean isGeomReady () {
        if (readyPp2dgeom) {
        	readyPp2dgeom = false;
            return true;
        }
        return false;
    }
    
    // used by HeadingControl and PositionControl - to refactorize
    public float getX      () {
    	return this.pp2ddata.getPos ().getPx ();
    }
    
    public float getY      () {
    	return this.pp2ddata.getPos ().getPy ();
    }
    
    public float getYaw    () {
    	return (float)((this.pp2ddata.getPos ().getPa () * 180) / Math.PI);
    }
    
    public void setSpeed (float speed, float turnrate) {
    	PlayerPose pp = new PlayerPose ();
    	pp.setPx (speed);
    	pp.setPa (turnrate);
    	setVelocity (pp, 1);
    }
}
