/*
 *  Player Java Client - PositionInterface.java
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
 * $Id: PositionInterface.java 22 2005-06-14 09:41:16Z veedee $
 *
 */
package javaclient;

import javaclient.structures.PlayerPositionGeomT;

/**
 * The position interface is used to control mobile robot bases in 2D.
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 * 	<li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 * 	<li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class PositionInterface extends AbstractPositionDevice {

    private static final boolean isDebugging = PlayerClient.isDebugging;

    /* device that moves about */
    private short PLAYER_POSITION_CODE = PlayerClient.PLAYER_POSITION_CODE;
    
    /* the player message types (see player.h) */
    private static final short PLAYER_MSGTYPE_CMD = PlayerClient.PLAYER_MSGTYPE_CMD;
    private static final short PLAYER_MSGTYPE_REQ = PlayerClient.PLAYER_MSGTYPE_REQ;

    /* the various configuration request types */ 
    protected final short PLAYER_POSITION_GET_GEOM_REQ      = 1;
    protected final short PLAYER_POSITION_MOTOR_POWER_REQ   = 2;
    protected final short PLAYER_POSITION_VELOCITY_MODE_REQ = 3;
    protected final short PLAYER_POSITION_RESET_ODOM_REQ    = 4;
    protected final short PLAYER_POSITION_POSITION_MODE_REQ = 5;
    protected final short PLAYER_POSITION_SPEED_PID_REQ     = 6;
    protected final short PLAYER_POSITION_POSITION_PID_REQ  = 7;
    protected final short PLAYER_POSITION_SPEED_PROF_REQ    = 8;
    protected final short PLAYER_POSITION_SET_ODOM_REQ      = 9;

    /* These are possible Segway RMP config commands; see the status command in
    the RMP manual */
    protected final short PLAYER_POSITION_RMP_VELOCITY_SCALE  = 51;  
    protected final short PLAYER_POSITION_RMP_ACCEL_SCALE     = 52;
    protected final short PLAYER_POSITION_RMP_TURN_SCALE      = 53;
    protected final short PLAYER_POSITION_RMP_GAIN_SCHEDULE   = 54;
    protected final short PLAYER_POSITION_RMP_CURRENT_LIMIT   = 55;
    protected final short PLAYER_POSITION_RMP_RST_INTEGRATORS = 56;
    protected final short PLAYER_POSITION_RMP_SHUTDOWN        = 57;

    /* These are used for resetting the Segway RMP's integrators. */
    protected final short PLAYER_POSITION_RMP_RST_INT_RIGHT   = 1;
    protected final short PLAYER_POSITION_RMP_RST_INT_LEFT    = 2;
    protected final short PLAYER_POSITION_RMP_RST_INT_YAW     = 3;
    protected final short PLAYER_POSITION_RMP_RST_INT_FOREAFT = 4;
    
    /* see player.h / player_position_data for additional explanations */
    private int  x        = 0;        /* X position in mm */
    private int  y        = 0;        /* Y position in mm */
    private int  yaw      = 0;        /* Yaw in degrees */
    private int  xSpeed   = 0;        /* X translational velocity in mm/sec */
    private int  ySpeed   = 0;        /* Y translational velocity in mm/sec */
    private int  yawSpeed = 0;        /* angular velocity in degrees/sec */
    private byte stalls   = 0;        /* are the motors stalled? */

    /* object containing player_position_geom in case of a threaded call */
    private PlayerPositionGeomT ppgt;
    private boolean             readyPPGT = false;
	
    /**
     * Constructor for PositionInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public PositionInterface (PlayerClient pc, short indexOfDevice) {
        super (pc);
        device    = PLAYER_POSITION_CODE;
        index     = indexOfDevice;
    }
	
    /**
     * Read the position data values (x, y, yaw, xSpeed, ySpeed, yawSpeed, stalls).
     */
    public synchronized void readData () {
        try {
            readHeader ();
            x        = is.readInt ();
            y        = is.readInt ();
            yaw      = is.readInt ();
            xSpeed   = is.readInt ();
            ySpeed   = is.readInt ();
            yawSpeed = is.readInt ();
            stalls   = is.readByte ();
        } catch (Exception e) {
            System.err.println ("[Position] : Error when reading payload: " + e.toString());
        }
    }
	
    /**
     * The position interface accepts new positions and/or velocities for the robot's motors 
     * (drivers may support position control, speed control or both).
     * <br><br>
     * See the player_position_cmd structure from player.h
     * @param xP X position in mm
     * @param yP Y position in mm
     * @param yawT Yaw in degrees
     * @param xS X translational velocity in mm/sec
     * @param yS Y translational velocity in mm/sec
     * @param yawS angular velocity in degrees/sec
     * @param state motor state (zero is either off or locked, depending on the driver)
     * @param type command type; 0 = velocity, 1 = position
     */
    public void setPosition (int xP, int yP, int yawT,
                             int xS, int yS, int yawS,
                             byte state, byte type) {
        try {
            sendHeader (PLAYER_MSGTYPE_CMD, 26);	/* 26 bytes payload */
            os.writeInt (xP);
            os.writeInt (yP);
            os.writeInt (yawT);
            os.writeInt (xS);
            os.writeInt (yS);
            os.writeInt (yawS);
            os.writeByte (state);
            os.writeByte (type);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position] : Couldn't send position commands: " + e.toString ());
        }
    }

    /** Send position commands.
     * @param xP X position in mm
     * @param yP Y position in mm
     * @param yawT Yaw in degrees 
     */
    public void setPosition (int xP, int yP, int yawT) {
        setPosition (xP, yP, yawT, 
                     this.xSpeed, this.ySpeed, this.yawSpeed, (byte)1, (byte)1);
    }
    
    /** Send the heading of the robot.
     * @param yawT Yaw in degrees 
     */
    public void setHeading (int yawT) {
        setPosition (this.x, this.y, yawT, 
                     this.xSpeed, this.ySpeed, this.yawSpeed, (byte)1, (byte)1);
    }
    
    /**
     * Set speed and turnrate.
     * @param speed X translational velocity in mm/sec
     * @param turnrate angular velocity in degrees/sec
     */
    public void setSpeed (int speed, int turnrate) {
        setPosition (this.x, this.y, this.yaw, 
                     speed, this.ySpeed, turnrate, (byte)1, (byte)0);
    }
	
    /**
     * Set speed, turnrate and sideSpeed.
     * @param speed X translational velocity in mm/sec
     * @param turnrate angular velocity in degrees/sec
     * @param sideSpeed Y translational velocity in mm/sec
     */
    public void setSpeed (int speed, int turnrate, int sideSpeed) {
        setPosition(this.x, this.y, this.yaw, speed, sideSpeed, turnrate, (byte)1, (byte)0);
    }
	
    /**
     * Configuration request: Query geometry.
     *
     */
    public void queryGeometry () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 1);		/* 1 byte payload */
            os.write (PLAYER_POSITION_GET_GEOM_REQ);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position] : Couldn't send PLAYER_POSITION_GET_GEOM_REQ " +
                    "command: " + e.toString ());
        }
    }
	
    /**
     * Check if geometry data is available.
     * @return true if ready, false if not ready 
     */
    public boolean isGeomReady () {
        if (readyPPGT) {
            readyPPGT = false;
            return true;
        }
        return false;
    }
	
    /**
     * Get the geometry data.
     * @return an object of type PlayerPositionGeomT containing the required geometry data 
     */
    public synchronized PlayerPositionGeomT getGeom () { return ppgt; }
	
    /**
     * Get X position in mm.
     * @return X position in mm
     */
    public synchronized int  getX      () { return x;      }
    
    /**
     * Get Y position in mm.
     * @return Y position in mm
     */
    public synchronized int  getY      () { return y;      }
    
    /**
     * Get Heading in degrees.
     * @return heading in degrees
     */
    public synchronized int  getYaw    () { return yaw;    }
    
    /**
     * Get X translational velocity in mm/sec.
     * @return X translational velocity in mm/sec
     */
    public synchronized int  getXSpeed   () { return xSpeed;   }
    
    /**
     * Get Y translational velocity in mm/sec.
     * @return Y translational velocity in mm/sec
     */
    public synchronized int  getYSpeed   () { return ySpeed;   }
    
    /**
     * Get angular velocity in degrees/sec.
     * @return angular velocity in degrees/sec
     */
    public synchronized int  getYawSpeed () { return yawSpeed; }
    
    /**
     * Get motors status.
     * @return stalled or not?
     */
    public synchronized byte getStall  () { return stalls; }
	
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
            sendHeader (PLAYER_MSGTYPE_REQ, 2);		/* 2 bytes payload */
            os.writeByte (PLAYER_POSITION_MOTOR_POWER_REQ);
            os.writeByte ((byte)state);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position] : Couldn't send PLAYER_POSITION_MOTOR_POWER_REQ " +
                    "command: " + e.toString ());
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
            sendHeader (PLAYER_MSGTYPE_REQ, 2);		/* 2 bytes payload */
            os.writeByte (PLAYER_POSITION_VELOCITY_MODE_REQ);
            os.writeByte ((byte)mode);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position] : Couldn't send PLAYER_POSITION_VELOCITY_MODE_REQ " +
                    "command: " + e.toString ());
        }
    }
	
    /**
     * Configuration request: Reset odometry.
     * <br><br>
     * Resets the robot's odometry to (x,y,theta) = (0,0,0).
     */
    public void resetOdometry () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 1);		/* 1 byte payload */
            os.writeByte (PLAYER_POSITION_RESET_ODOM_REQ);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position] : Couldn't send PLAYER_POSITION_RESET_ODOM_REQ " +
                    "command: " + e.toString ());
        }
    }

    /**
     * Configuration request: Change control mode.
     * @param mode 0 for velocity mode, 1 for position mode
     */
    public void setControlMode (int mode) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 2);		/* 2 bytes payload */
            os.writeByte (PLAYER_POSITION_POSITION_MODE_REQ);
            os.writeByte ((byte)mode);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position] : Couldn't send PLAYER_POSITION_POSITION_MODE_REQ " +
                    "command: " + e.toString ());
        }
    }
	
    /**
     * Configuration request: Set odometry.
     * @param xT X in mm 
     * @param yT Y in m
     * @param theta Heading in degrees 
     */
    public void setOdometry (int xT, int yT, int theta) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 13);        /* 13 bytes payload */
            os.writeByte (PLAYER_POSITION_SET_ODOM_REQ);
            os.writeInt (xT);
            os.writeInt (yT);
            os.writeInt (theta);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position] : Couldn't send PLAYER_POSITION_SET_ODOM_REQ " +
                    "command: " + e.toString ());
        }
    }
    
    /**
     * Configuration request: Set velocity PID parameters.
     * @param kp P parameter
     * @param ki I parameter
     * @param kd D parameter
     */
    public void setVelocityPIDParams (int kp, int ki, int kd) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 13);		/* 13 bytes payload */
            os.writeByte (PLAYER_POSITION_SPEED_PID_REQ);
            os.writeInt (kp);
            os.writeInt (ki);
            os.writeInt (kd);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position] : Couldn't send PLAYER_POSITION_SPEED_PID_REQ " +
                    "command: " + e.toString ());
        }
    }

    /**
     * Configuration request: Set position PID parameters.
     * @param kp P parameter
     * @param ki I parameter
     * @param kd D parameter
     */
    public void setPositionPIDParams (int kp, int ki, int kd) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 13);		/* 13 bytes payload */
            os.writeByte (PLAYER_POSITION_POSITION_PID_REQ);
            os.writeInt (kp);
            os.writeInt (ki);
            os.writeInt (kd);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position] : Couldn't send PLAYER_POSITION_POSITION_PID_REQ " +
                    "command: " + e.toString ());
        }
    }
	
    /**
     * Configuration request: Set speed profile parameters.
     * @param sp max speed 
     * @param acc max acceleration 
     */
    public void setSpeedProfileParams (short sp, short acc) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 5);		/* 5 bytes payload */
            os.writeByte (PLAYER_POSITION_SPEED_PROF_REQ);
            os.writeShort (sp);
            os.writeShort (acc);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position] : Couldn't send PLAYER_POSITION_SPEED_PROF_REQ " +
                    "command: " + e.toString ());
        }
    }
	
    /**
     * Configuration request: Segway RMP-specific configuration.
     * <br><br>
     * These are possible Segway RMP config commands; see the status command in the RMP manual:
     * <ul>
     * 			<li>PLAYER_POSITION_RMP_VELOCITY_SCALE  = 51  
     * 			<li>PLAYER_POSITION_RMP_ACCEL_SCALE     = 52
     * 			<li>PLAYER_POSITION_RMP_TURN_SCALE      = 53
     * 			<li>PLAYER_POSITION_RMP_GAIN_SCHEDULE   = 54
     * 			<li>PLAYER_POSITION_RMP_CURRENT_LIMIT   = 55
     * 			<li>PLAYER_POSITION_RMP_RST_INTEGRATORS = 56
     * 			<li>PLAYER_POSITION_RMP_SHUTDOWN        = 57
     *          <li>PLAYER_POSITION_RMP_RST_INT_RIGHT   = 1
     *          <li>PLAYER_POSITION_RMP_RST_INT_LEFT    = 2
     *          <li>PLAYER_POSITION_RMP_RST_INT_YAW     = 3
     *          <li>PLAYER_POSITION_RMP_RST_INT_FOREAFT = 4
     * </ul>
     * @param subtype must be of PLAYER_POSITION_RMP_*
     * @param value holds various values depending on the type of config. See the "Status" 
     * command in the Segway manual.
     */
    public void setSegwayRPMparams (byte subtype, short value) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 3);		/* 3 bytes payload */
            os.writeByte  (subtype);
            os.writeShort (value);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position] : Couldn't send Segway RPM configuration request: " +
                    e.toString ());
        }
    }
	
    /**
     * Handle acknowledgement response messages (threaded mode).
     * @param size size of the payload
     */
    public void handleResponse (int size) {
        if (size == 0) {
            if (isDebugging)
                System.err.println ("[Position][Debug] : Unexpected response of size 0!");
            return;
        }
        try {
            /* each reply begins with a uint8_t subtype field */
            byte subtype = is.readByte ();
            switch (subtype) {
                case PLAYER_POSITION_GET_GEOM_REQ: {
                    ppgt = new PlayerPositionGeomT ();
                    
                    /* pose of the robot base, in the robot cs (mm, mm, degrees) */
                    short[] ppose = new short[3];
                    /* dimensions of the base (mm, mm) */
                    short[] psize = new short[2];

                    ppose[0] = is.readShort ();     /* X pos in mm */
                    ppose[1] = is.readShort ();     /* Y pos in mm */
                    ppose[2] = is.readShort ();     /* Yaw in degrees */
                    
                    psize[0] = (short)is.readUnsignedShort ();  /* X base dimensions in mm */
                    psize[1] = (short)is.readUnsignedShort ();  /* Y base dimensions in mm */
                    
                    ppgt.setPose (ppose);
                    ppgt.setSize (psize);
                    readyPPGT = true;
                    break;
                }
                case PLAYER_POSITION_MOTOR_POWER_REQ: {
                    break;
                }
                case PLAYER_POSITION_VELOCITY_MODE_REQ: {
                	break;
                }
                case PLAYER_POSITION_RESET_ODOM_REQ: {
                	break;
                }
                case PLAYER_POSITION_POSITION_MODE_REQ: {
                	break;
                }
                case PLAYER_POSITION_SPEED_PID_REQ: {
                	break;
                }
                case PLAYER_POSITION_POSITION_PID_REQ: {
                	break;
                }
                case PLAYER_POSITION_SPEED_PROF_REQ: {
                	break;
                }
                /* Possible Segway RMP config commands. */
                case PLAYER_POSITION_RMP_VELOCITY_SCALE: {
                	break;
                }
                case PLAYER_POSITION_RMP_ACCEL_SCALE: {
                    break;
                }
                case PLAYER_POSITION_RMP_TURN_SCALE: {
                    break;
                }
                case PLAYER_POSITION_RMP_GAIN_SCHEDULE: {
                    break;
                }
                case PLAYER_POSITION_RMP_CURRENT_LIMIT: {
                    break;
                }
                case PLAYER_POSITION_RMP_RST_INTEGRATORS: {
                    break;
                }
                case PLAYER_POSITION_RMP_SHUTDOWN: {
                    break;
                }
                default:{
                    System.err.println ("[Position] : Unexpected response " + subtype + 
                            " of size = " + size);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println ("[Position] : Error when reading payload " + e.toString ());
        }
    }
    
    /**
     * Handle Negative Acknowledgement Response messages.
     */
    public void handleNARMessage () {
        try {
            int size = is.readInt ();    /* read the packet size */
            System.err.println ("[Position] : Handling NAR of size = " + size);
        } catch (Exception e) {
            System.err.println ("[Position] : handleResponsePosition ERROR " + e.toString ());
        }
    }
	
    /**
     * Handle Error Acknowledgement Response messages.
     */
    public void handleEARMessage () {
        try {
            int size = is.readInt ();    /* read the packet size */
            System.err.println ("[Position] : Handling EAR of size = " + size);
        } catch (Exception e) {
            System.err.println ("[Position] : handleResponsePosition ERROR " + e.toString ());
        }
    }

}
