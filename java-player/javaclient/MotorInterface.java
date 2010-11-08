/*
 *  Player Java Client - MotorInterface.java
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
 * $Id: MotorInterface.java 29 2005-11-24 12:39:51Z veedee $
 *
 */
package javaclient;

/**
 * The motor interface is used to control a single motor.   
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 * </ul>
 */
public class MotorInterface extends PlayerDevice {

    /* motor interface */
    private final short PLAYER_MOTOR_CODE = PlayerClient.PLAYER_MOTOR_CODE;

    /* the player message types (see player.h) */
    private static final short PLAYER_MSGTYPE_CMD = PlayerClient.PLAYER_MSGTYPE_CMD;
    private static final short PLAYER_MSGTYPE_REQ = PlayerClient.PLAYER_MSGTYPE_REQ;

    /* the various configuration request types */ 
    protected final short PLAYER_MOTOR_MOTOR_POWER_REQ        = 2;
    protected final short PLAYER_MOTOR_VELOCITY_MODE_REQ      = 3;
    protected final short PLAYER_MOTOR_RESET_ODOM_REQ         = 4;
    protected final short PLAYER_MOTOR_POSITION_MODE_REQ      = 5;
    protected final short PLAYER_MOTOR_SPEED_PID_REQ          = 6;
    protected final short PLAYER_MOTOR_POSITION_PID_REQ       = 7;
    protected final short PLAYER_MOTOR_SPEED_PROF_REQ         = 8;
    protected final short PLAYER_MOTOR_SET_ODOM_REQ           = 9;
    protected final short PLAYER_MOTOR_SET_GEAR_REDUCTION_REQ = 10;
    protected final short PLAYER_MOTOR_SET_TICS_REQ           = 11;

    private int  theta;         /* Theta in mrad (1 milliradian = ~.06 degrees) */
    private int  thetaSpeed;    /* Angular velocity in mrad/sec */
    private byte stall;         /* Are the motors stalled?  */
    
    /**
     * Constructor for MotorInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public MotorInterface (PlayerClient pc, short indexOfDevice) {
        super(pc);
        device    = PLAYER_MOTOR_CODE;
        index     = indexOfDevice;
    }
    
    /**
     * The motor interface returns data regarding the position and velocity 
     * of the motor, as well as stall information.
     */
    public synchronized void readData () {
        try {
            readHeader ();
            theta      = is.readInt ();
            thetaSpeed = is.readInt ();
            stall      = is.readByte ();
        } catch (Exception e) {
            System.err.println ("[Motor] : Error when reading payload: " + e.toString());
        }
    }

    /**
     * Get theta in mrad (1 milliradian = ~.06 degrees).
     * @return theta in mrad as an integer
     */
    public synchronized int getTheta      () { return theta;      }

    /**
     * Get angular velocity in mrad/sec.
     * @return angular velocity in mrad/sec as an integer
     */
    public synchronized int getThetaSpeed () { return thetaSpeed; }

    /**
     * Get motors status.
     * @return stalled or not?
     */
    public synchronized int getStall      () { return stall;      }

    /**
     * The motor interface accepts new positions and/or velocities for the motors 
     * (drivers may support position control, speed control, or both). 
     * <br><br>
     * See the player_motor_cmd structure from player.h
     * @param thetaP theta in mrad
     * @param thetaS angular velocities, in mrad/sec
     * @param state motor state (zero is either off or locked, depending on the driver)
     * @param type command type; 0 = velocity, 1 = position
     */
    public void setMotor (int thetaP, int thetaS,
                          int state, int type) {
        try {
            sendHeader (PLAYER_MSGTYPE_CMD, 10);    /* 10 bytes payload */
            os.writeInt (thetaP);
            os.writeInt (thetaS);
            os.writeByte ((byte)state);
            os.writeByte ((byte)type);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Motor] : Couldn't send position commands: " + e.toString ());
        }
    }

    /**
     * Configuration request: Change position control.
     * @param mode 0 for velocity mode, 1 for position mode
     */
    public void changePositionControl (byte mode) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 2);     /* 2 bytes payload */
            os.writeByte (PLAYER_MOTOR_POSITION_MODE_REQ);
            os.writeByte (mode);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Motor] : Couldn't send " +
                    "PLAYER_MOTOR_POSITION_MODE_REQ command: " + e.toString ());
        }
    }

    /**
     * Configuration request: Change velocity control mode.
     * <br><br>
     * Some motors offer different velocity control modes. It can be changed by sending 
     * a request with the format given below, including the appropriate mode. No matter 
     * which mode is used, the external client interface to the motor device remains 
     * the same. 
     * @param mode driver-specific
     */
    public void setVelocityControl (byte mode) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 2);     /* 2 bytes payload */
            os.writeByte (PLAYER_MOTOR_VELOCITY_MODE_REQ);
            os.writeByte (mode);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Motor] : Couldn't send " +
                    "PLAYER_MOTOR_VELOCITY_MODE_REQ command: " + e.toString ());
        }
    }
    
    /**
     * Configuration request: Reset odometry.
     * <br><br>
     * Resets the motor's odometry to theta = 0.
     */
    public void resetOdometry () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 1);     /* 1 byte payload */
            os.writeByte (PLAYER_MOTOR_RESET_ODOM_REQ);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Motor] : Couldn't send " +
                    "PLAYER_MOTOR_RESET_ODOM_REQ command: " + e.toString ());
        }
    }

    /**
     * Configuration request: Set odometry.
     * @param theta theta in mrad 
     */
    public void setOdometry (int xT, int yT, int theta) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 5);        /* 5 bytes payload */
            os.writeByte (PLAYER_MOTOR_SET_ODOM_REQ);
            os.writeInt (theta);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Motor] : Couldn't send " +
                    "PLAYER_MOTOR_SET_ODOM_REQ command: " + e.toString ());
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
            sendHeader (PLAYER_MSGTYPE_REQ, 13);        /* 13 bytes payload */
            os.writeByte (PLAYER_MOTOR_SPEED_PID_REQ);
            os.writeInt (kp);
            os.writeInt (ki);
            os.writeInt (kd);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Motor] : Couldn't send " +
                    "PLAYER_MOTOR_SPEED_PID_REQ command: " + e.toString ());
        }
    }
    
    /**
     * Configuration request: Set motor PID parameters.
     * @param kp P parameter
     * @param ki I parameter
     * @param kd D parameter
     */
    public void setMotorPIDParams (int kp, int ki, int kd) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 13);        /* 13 bytes payload */
            os.writeByte (PLAYER_MOTOR_POSITION_PID_REQ);
            os.writeInt (kp);
            os.writeInt (ki);
            os.writeInt (kd);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Motor] : Couldn't send " +
                    "PLAYER_MOTOR_POSITION_PID_REQ command: " + e.toString ());
        }
    }

    /**
     * Configuration request: Set speed profile parameters.<br /><br />
     * This is usefull in position control mode when you want to ramp your 
     * acceleration and deacceleration.
     * @param sp max speed 
     * @param acc max acceleration 
     */
    public void setSpeedProfileParams (int sp, int acc) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 9);     /* 9 bytes payload */
            os.writeByte (PLAYER_MOTOR_SPEED_PROF_REQ);
            os.writeInt (sp);
            os.writeInt (acc);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Motor] : Couldn't send " +
                    "PLAYER_MOTOR_SPEED_PROF_REQ command: " + e.toString ());
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
            sendHeader (PLAYER_MSGTYPE_REQ, 2);     /* 2 bytes payload */
            os.writeByte (PLAYER_MOTOR_MOTOR_POWER_REQ);
            os.writeByte (state);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Motor] : Couldn't send " +
                    "PLAYER_MOTOR_MOTOR_POWER_REQ command: " + e.toString ());
        }
    }

    /**
     * Handle Negative Acknowledgement Response messages.
     */
    public void handleNARMessage () {
        try {
            int size = is.readInt ();    /* read the packet size */
            System.err.println ("[Motor] : Handling NAR of size = " + size);
        } catch (Exception e) {
            System.err.println ("[Motor] : handleResponsePosition ERROR " + e.toString ());
        }
    }
    
    /**
     * Handle Error Acknowledgement Response messages.
     */
    public void handleEARMessage () {
        try {
            int size = is.readInt ();    /* read the packet size */
            System.err.println ("[Motor] : Handling EAR of size = " + size);
        } catch (Exception e) {
            System.err.println ("[Motor] : handleResponsePosition ERROR " + e.toString ());
        }
    }
}
