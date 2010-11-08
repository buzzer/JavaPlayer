/*
 *  Player Java Client - Position3DInterface.java
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
 * $Id: Position3DInterface.java 22 2005-06-14 09:41:16Z veedee $
 *
 */
package javaclient;

import javaclient.structures.PlayerPosition3DGeomT;

/**
 * The position3d interface is used to control mobile robot bases in 
 * 3D (i.e., pitch and roll are important).  
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 * </ul>
 */
public class Position3DInterface extends AbstractPositionDevice {

    private static final boolean isDebugging = PlayerClient.isDebugging;

    /* 3-D position */
    private final short PLAYER_POSITION3D_CODE = PlayerClient.PLAYER_POSITION3D_CODE;
    
    /* the player message types (see player.h) */
    private static final short PLAYER_MSGTYPE_CMD = PlayerClient.PLAYER_MSGTYPE_CMD;
    private static final short PLAYER_MSGTYPE_REQ = PlayerClient.PLAYER_MSGTYPE_REQ;

    /* the various configuration request types */ 
    protected final short PLAYER_POSITION3D_GET_GEOM_REQ      = 1;
    protected final short PLAYER_POSITION3D_MOTOR_POWER_REQ   = 2;
    protected final short PLAYER_POSITION3D_VELOCITY_MODE_REQ = 3;
    protected final short PLAYER_POSITION3D_RESET_ODOM_REQ    = 4;
    protected final short PLAYER_POSITION3D_POSITION_MODE_REQ = 5;
    protected final short PLAYER_POSITION3D_SPEED_PID_REQ     = 6;
    protected final short PLAYER_POSITION3D_POSITION_PID_REQ  = 7;
    protected final short PLAYER_POSITION3D_SPEED_PROF_REQ    = 8;
    protected final short PLAYER_POSITION3D_SET_ODOM_REQ      = 9;
    
    /* see player.h / player_position3d_data for additional explanations */
    private int  xPos       = 0;        /* X position in mm */
    private int  yPos       = 0;        /* Y position in mm */
    private int  zPos       = 0;        /* Y position in mm */
    private int  roll       = 0;        /* roll in mrad */
    private int  pitch      = 0;        /* pitch in mrad */
    private int  yaw        = 0;        /* yaw in mrad */
    private int  xSpeed     = 0;        /* X translational velocity in mm/sec */
    private int  ySpeed     = 0;        /* Y translational velocity in mm/sec */
    private int  zSpeed     = 0;        /* Z translational velocity in mm/sec */
    private int  pitchSpeed = 0;        /* angular velocity in mrad/sec */
    private int  rollSpeed  = 0;        /* angular velocity in mrad/sec */
    private int  yawSpeed   = 0;        /* angular velocity in mrad/sec */
    private byte stalls     = 0;        /* are the motors stalled? */
    
    /* object containing player_position3d_geom in case of a threaded call */
    private PlayerPosition3DGeomT ppgt;
    private boolean               readyPPGT = false;

    /**
     * Constructor for Position3DInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public Position3DInterface (PlayerClient pc, short indexOfDevice) {
        super(pc);
        device    = PLAYER_POSITION3D_CODE;
        index     = indexOfDevice;
    }
    
    /**
     * This interface returns data regarding the odometric pose and velocity of 
     * the robot, as well as motor stall information (xPos, yPos, zPos, roll, pitch, yaw, 
     * xSpeed, ySpeed, zSpeed, rollSpeed, pitchSpeed, yawSpeed, stall).
     */
    public synchronized void readData () {
        try {
            readHeader ();
            xPos       = is.readInt ();
            yPos       = is.readInt ();
            zPos       = is.readInt ();
            roll       = is.readInt ();
            pitch      = is.readInt ();
            yaw        = is.readInt ();
            xSpeed     = is.readInt ();
            ySpeed     = is.readInt ();
            zSpeed     = is.readInt ();
            rollSpeed  = is.readInt ();
            pitchSpeed = is.readInt ();
            yawSpeed   = is.readInt ();
            stalls     = is.readByte ();
        } catch (Exception e) {
            System.err.println ("[Position3D] : Error when reading payload: " + e.toString());
        }
    }

    /**
     * Get X position in mm.
     * @return X position in mm
     */
    public synchronized int  getX   () { return xPos;   }
    
    /**
     * Get Y position in mm.
     * @return Y position in mm
     */
    public synchronized int  getY   () { return yPos;   }
    
    /**
     * Get Z position in mm.
     * @return Z position in mm
     */
    public synchronized int  getZ   () { return zPos;   }
    
    /**
     * Get angular velocity (roll) in mrad.
     * @return angular velocity (roll) in mrad
     */
    public synchronized int  getRoll   () { return roll;    }
    
    /**
     * Get angular velocity (pitch) in mrad.
     * @return angular velocity (pitch) in mrad
     */
    public synchronized int  getPitch  () { return pitch;   }

    /**
     * Get angular velocity (yaw) in mrad.
     * @return angular velocity (yaw) in mrad
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
     * Get Z translational velocity in mm/sec.
     * @return Z translational velocity in mm/sec
     */
    public synchronized int  getZSpeed   () { return zSpeed;   }
    
    /**
     * Get angular velocity (roll) in mrad/sec.
     * @return angular velocity (roll) in mrad/sec
     */
    public synchronized int  getRollSpeed  () { return rollSpeed;  }

    /**
     * Get angular velocity (pitch) in mrad/sec.
     * @return angular velocity (pitch) in mrad/sec
     */
    public synchronized int  getPitchSpeed () { return pitchSpeed; }
    
    /**
     * Get angular velocity (yaw) in mrad/sec.
     * @return angular velocity (yaw) in mrad/sec
     */
    public synchronized int  getYawSpeed   () { return yawSpeed;   }
    
    /**
     * Get motors status.
     * @return stalled or not?
     */
    public synchronized byte getStall  () { return stalls; }

    /**
     * The position interface accepts new positions and/or velocities for the robot's motors 
     * (drivers may support position control, speed control or both).
     * <br><br>
     * See the player_position3d_cmd structure from player.h
     * @param xP X position in mm
     * @param yP Y position in mm
     * @param zP Z position in mm
     * @param pitchP angular velocity (pitch) in mrad
     * @param rollP angular velocity (roll) in mrad
     * @param yawP angular velocity (yaw) in mrad
     * @param xS X translational velocity in mm/sec
     * @param yS Y translational velocity in mm/sec
     * @param zS Z translational velocity in mm/sec
     * @param pitchS angular velocity (pitch) in mrad/sec
     * @param rollS angular velocity (roll) in mrad/sec
     * @param yawS angular velocity (yaw) in mrad/sec
     * @param state motor state (zero is either off or locked, depending on the driver)
     * @param type command type; 0 = velocity, 1 = position
     */
    public void setPosition (int xP, int yP, int zP,
                             int pitchP, int rollP, int yawP,
                             int xS, int yS, int zS,
                             int pitchS, int rollS, int yawS,
                             byte state, byte type) {
        try {
            sendHeader (PLAYER_MSGTYPE_CMD, 50);    /* 50 bytes payload */
            
            /* X position in mm */
            os.writeInt (xP);
            
            /* Y position in mm */
            os.writeInt (yP);
            
            /* Z position in mm */
            os.writeInt (zP);
            
            /* angular velocity (pitch) in mrad */
            os.writeInt (pitchP);
            
            /* angular velocity (roll) in mrad */
            os.writeInt (rollP);
            
            /* angular velocity (yaw) in mrad */
            os.writeInt (yawP);
            
            /* X translational velocity in mm/sec */
            os.writeInt (xS);
            
            /* Y translational velocity in mm/sec */
            os.writeInt (yS);
            
            /* Z translational velocity in mm/sec */
            os.writeInt (zS);
            
            /* angular velocity (pitch) in mrad/sec */
            os.writeInt (pitchS);
            
            /* angular velocity (roll) in mrad/sec */
            os.writeInt (rollS);
            
            /* angular velocity (yaw) in mrad/sec */
            os.writeInt (yawS);
            
            os.writeByte (state);
            os.writeByte (type);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position3D] : Couldn't send position commands: " + e.toString ());
        }
    }

    /** Send position commands.
     * @param xP X position in mm
     * @param yP Y position in mm
     * @param zP Z position in mm
     */
    public void setPosition (int xP, int yP, int zP) {
        setPosition (xP, yP, zP, this.pitch, this.roll, this.yaw,
                     this.xSpeed, this.ySpeed, this.zSpeed, 
                     this.pitchSpeed, this.rollSpeed, this.yawSpeed,
                     (byte)1, (byte)1);
    }
    
    /** Send position commands.
     * @param xP X position in mm
     * @param yP Y position in mm
     * @param zP Z position in mm
     * @param pitchP angular velocity (pitch) in mrad
     * @param rollP angular velocity (roll) in mrad
     * @param yawP angular velocity (yaw) in mrad
     */
    public void setPosition (int xP, int yP, int zP, int pitchP, int rollP, int yawP) {
        setPosition (xP, yP, zP, pitchP, rollP, yawP, 
                     this.xSpeed, this.ySpeed, this.zSpeed,
                     this.pitchSpeed, this.rollSpeed, this.yawSpeed, (byte)1, (byte)1);
    }
    
    /**
     * Set speed and turnrate.
     * @param xS X translational velocity in mm/sec
     * @param yS Y translational velocity in mm/sec
     */
    public void setSpeed (int xS, int yS) {
        setPosition (this.xPos, this.yPos, this.zPos, this.pitch, this.roll, this.yaw,
                     xS, yS, this.zSpeed, this.pitchSpeed, this.rollSpeed, this.yawSpeed, 
                     (byte)1, (byte)0);
    }
    
    /**
     * Set speed and turnrate.
     * @param xS X translational velocity in mm/sec
     * @param yS Y translational velocity in mm/sec
     * @param zS Z translational velocity in mm/sec
     */
    public void setSpeed (int xS, int yS, int zS) {
        setPosition (this.xPos, this.yPos, this.zPos, this.pitch, this.roll, this.yaw,
        		     xS, yS, zS, this.pitchSpeed, this.rollSpeed, this.yawSpeed, 
                     (byte)1, (byte)0);
    }
    
    /**
     * Set speed, turnrate and sideSpeed.
     * @param xS X translational velocity in mm/sec
     * @param yS Y translational velocity in mm/sec
     * @param zS Z translational velocity in mm/sec
     * @param pitchS angular velocity (pitch) in mrad/sec
     * @param rollS angular velocity (roll) in mrad/sec
     * @param yawS angular velocity (yaw) in mrad/sec
     */
    public void setSpeed (int xS, int yS, int zS, int pitchS, int rollS, int yawS) {
        setPosition (this.xPos, this.yPos, this.zPos, this.pitch, this.roll, this.yaw,
        		     xS, yS, zS, pitchS, rollS, yawS, (byte)1, (byte)0);
    }

    /**
     * Configuration request: Query geometry.
     *
     */
    public void queryGeometry () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 1);     /* 1 byte payload */
            os.write (PLAYER_POSITION3D_GET_GEOM_REQ);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position3D] : Couldn't send " +
                    "PLAYER_POSITION3D_GET_GEOM_REQ command: " + e.toString ());
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
     * @return an object of type PlayerPosition3DGeomT containing the required geometry data 
     */
    public synchronized PlayerPosition3DGeomT getGeom () { return ppgt; }

    /**
     * Handle acknowledgement response messages (threaded mode).
     * @param size size of the payload
     */
    public void handleResponse (int size) {
        if (size == 0) {
            if (isDebugging)
                System.err.println ("[Position2D][Debug] : Unexpected response of size 0!");
            return;
        }
        try {
            /* each reply begins with a uint8_t subtype field */
            byte subtype = is.readByte ();
            switch (subtype) {
                case PLAYER_POSITION3D_GET_GEOM_REQ: {
                    ppgt = new PlayerPosition3DGeomT ();
                    
                    /* pose of the robot base, in the robot cs (mm, mm, mm, mrad, mrad, mrad) */
                    short[] ppose = new short[6];
                    /* dimensions of the base (mm, mm, mm) */
                    short[] psize = new short[3];

                    ppose[0] = is.readShort ();     /* X pos in mm */
                    ppose[1] = is.readShort ();     /* Y pos in mm */
                    ppose[2] = is.readShort ();     /* Z pos in mm */
                    ppose[3] = is.readShort ();     /* Pitch in mrad */
                    ppose[4] = is.readShort ();     /* Roll in mrad */
                    ppose[5] = is.readShort ();     /* Yaw in mrad */
                    
                    psize[0] = (short)is.readUnsignedShort ();  /* X base dimensions in mm */
                    psize[1] = (short)is.readUnsignedShort ();  /* Y base dimensions in mm */
                    psize[2] = (short)is.readUnsignedShort ();  /* Z base dimensions in mm */
                    
                    ppgt.setPose (ppose);
                    ppgt.setSize (psize);
                    readyPPGT = true;
                    break;
                }
                case PLAYER_POSITION3D_MOTOR_POWER_REQ: {
                    break;
                }
                case PLAYER_POSITION3D_VELOCITY_MODE_REQ: {
                    break;
                }
                case PLAYER_POSITION3D_RESET_ODOM_REQ: {
                    break;
                }
                case PLAYER_POSITION3D_POSITION_MODE_REQ: {
                    break;
                }
                case PLAYER_POSITION3D_SPEED_PID_REQ: {
                    break;
                }
                case PLAYER_POSITION3D_POSITION_PID_REQ: {
                    break;
                }
                case PLAYER_POSITION3D_SPEED_PROF_REQ: {
                    break;
                }
                default:{
                    System.err.println ("[Position3D] : Unexpected response " + subtype + 
                            " of size = " + size);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println ("[Position3D] : Error when reading payload " + e.toString ());
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
            os.writeByte (PLAYER_POSITION3D_MOTOR_POWER_REQ);
            os.writeByte ((byte)state);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position3D] : Couldn't send " +
                    "PLAYER_POSITION3D_MOTOR_POWER_REQ command: " + e.toString ());
        }
    }
    
    /**
     * Configuration request: Change position control.
     * <br><br>
     * @param state 0 for velocity mode, 1 for position mode
     */
    public void changePositionControl (byte state) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 2);     /* 2 bytes payload */
            os.writeByte (PLAYER_POSITION3D_POSITION_MODE_REQ);
            os.writeByte (state);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position3D] : Couldn't send " +
                    "PLAYER_POSITION3D_POSITION_MODE_REQ command: " + e.toString ());
        }
    }
    
    /**
     * Configuration request: Change velocity control.
     * <br><br>
     * Some robots offer different velocity control modes. It can be changed by sending 
     * a request with the format given below, including the appropriate mode. No matter 
     * which mode is used, the external client interface to the position3d device 
     * remains the same.
     * <br><br>
     * @param value driver-specific
     */
    public void setVelocityControl (byte value) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 2);     /* 2 bytes payload */
            os.writeByte (PLAYER_POSITION3D_VELOCITY_MODE_REQ);
            os.writeByte (value);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position3D] : Couldn't send " +
                    "PLAYER_POSITION3D_VELOCITY_MODE_REQ command: " + e.toString ());
        }
    }
    
    /**
     * Configuration request: Set odometry.
     * @param xP X in mm 
     * @param yP Y in mm
     * @param zP Z in mm
     * @param rollP Heading in mrad 
     * @param pitchP Heading in mrad 
     * @param yawP Heading in mrad 
     */
    public void setOdometry (int xP, int yP, int zP, 
                             int rollP, int pitchP, int yawP) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 25);        /* 25 bytes payload */
            os.writeByte (PLAYER_POSITION3D_SET_ODOM_REQ);
            os.writeInt (xP);
            os.writeInt (yP);
            os.writeInt (zP);
            os.writeInt (rollP);
            os.writeInt (pitchP);
            os.writeInt (yawP);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position3D] : Couldn't send " +
                    "PLAYER_POSITION3D_SET_ODOM_REQ command: " + e.toString ());
        }
    }

    /**
     * Configuration request: Reset odometry.
     * <br><br>
     * Resets the robot's odometry to (x,y,theta) = (0,0,0).
     */
    public void resetOdometry () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 1);     /* 1 byte payload */
            os.writeByte (PLAYER_POSITION3D_RESET_ODOM_REQ);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position3D] : Couldn't send " +
                    "PLAYER_POSITION3D_RESET_ODOM_REQ command: " + e.toString ());
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
            os.writeByte (PLAYER_POSITION3D_SPEED_PID_REQ);
            os.writeInt (kp);
            os.writeInt (ki);
            os.writeInt (kd);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position3D] : Couldn't send " +
                    "PLAYER_POSITION3D_SPEED_PID_REQ command: " + e.toString ());
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
            sendHeader (PLAYER_MSGTYPE_REQ, 13);        /* 13 bytes payload */
            os.writeByte (PLAYER_POSITION3D_POSITION_PID_REQ);
            os.writeInt (kp);
            os.writeInt (ki);
            os.writeInt (kd);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position3D] : Couldn't send " +
                    "PLAYER_POSITION3D_POSITION_PID_REQ command: " + e.toString ());
        }
    }

    /**
     * Configuration request: Set speed profile parameters.
     * @param sp max speed (in mrad/s)
     * @param acc max acceleration (in mrad/s/s)
     */
    public void setSpeedProfileParams (int sp, int acc) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 9);     /* 9 bytes payload */
            os.writeByte (PLAYER_POSITION3D_SPEED_PROF_REQ);
            os.writeInt (sp);
            os.writeInt (acc);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Position3D] : Couldn't send " +
                    "PLAYER_POSITION3D_SPEED_PROF_REQ command: " + e.toString ());
        }
    }
    
    /**
     * Handle Negative Acknowledgement Response messages.
     */
    public void handleNARMessage () {
        try {
            int size = is.readInt ();    /* read the packet size */
            System.err.println ("[Position3D] : Handling NAR of size = " + size);
        } catch (Exception e) {
            System.err.println ("[Position3D] : handleResponsePosition ERROR " + e.toString ());
        }
    }
    
    /**
     * Handle Error Acknowledgement Response messages.
     */
    public void handleEARMessage () {
        try {
            int size = is.readInt ();    /* read the packet size */
            System.err.println ("[Position3D] : Handling EAR of size = " + size);
        } catch (Exception e) {
            System.err.println ("[Position3D] : handleResponsePosition ERROR " + e.toString ());
        }
    }
}
