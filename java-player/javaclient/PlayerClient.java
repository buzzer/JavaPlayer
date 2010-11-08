/*
 *  Player Java Client - PlayerClient.java
 *  Copyright (C) 2002-2005 Maxim A. Batalin, Esben H. Ostergaard & Radu Bogdan Rusu
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
 * $Id: PlayerClient.java 29 2005-11-24 12:39:51Z veedee $
 *
 */
package javaclient;

import java.net.Socket;
import java.net.SocketException;
import java.io.DataInputStream;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import javaclient.structures.PlayerDeviceDriverInfo;
import javaclient.structures.PlayerDeviceDevlistT;
import javaclient.structures.PlayerDeviceIdT;

/**
 * The PlayerClient is the main Javaclient class. It contains methods for interacting with the 
 * player device. The player device represents the server itself, and is used in configuring 
 * the behavior of the server. There is only one such device (with index 0) and it is always 
 * open.
 * @author Maxim A. Batalin, Esben H. Ostergaard & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 * 	<li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 * 	<li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class PlayerClient extends Thread {
    
    public static final boolean isDebugging = 
		(System.getProperty ("PlayerClient.debug") != null) ? true : false;

    private static final boolean stopOnEOFException = 
        (System.getProperty ("PlayerClient.stopOnEOFException") != null) ? false : true;
    
    /* the message start signifier */
    protected static final short PLAYER_STXX                   = 0x5878; 
    /* string to match the currently assigned devices */
    protected static final short PLAYER_MAX_DEVICE_STRING_LEN  = 64;
	

    /* the request subtypes (see player.h) */
    protected static final short PLAYER_PLAYER_DEVLIST_REQ     = 1;
    protected static final short PLAYER_PLAYER_DRIVERINFO_REQ  = 2;
    protected static final short PLAYER_PLAYER_DEV_REQ         = 3;
    protected static final short PLAYER_PLAYER_DATA_REQ        = 4;
    protected static final short PLAYER_PLAYER_DATAMODE_REQ    = 5;
    protected static final short PLAYER_PLAYER_DATAFREQ_REQ    = 6;
    protected static final short PLAYER_PLAYER_AUTH_REQ        = 7;
    protected static final short PLAYER_PLAYER_NAMESERVICE_REQ = 8;


    /* the player message types (see player.h) */
    protected static final short PLAYER_MSGTYPE_DATA           = 1;
    protected static final short PLAYER_MSGTYPE_CMD            = 2;
    protected static final short PLAYER_MSGTYPE_REQ            = 3;
    protected static final short PLAYER_MSGTYPE_RESP_ACK       = 4;
    protected static final short PLAYER_MSGTYPE_SYNCH          = 5;
    protected static final short PLAYER_MSGTYPE_RESP_NACK      = 6;
    protected static final short PLAYER_MSGTYPE_RESP_ERR       = 7;

    /* the current assigned interface codes for Player 1.6.2 */
    protected static final short PLAYER_NULL_CODE                = 256; // /dev/null analogue
    protected static final short PLAYER_PLAYER_CODE              = 1;   // the server itself
    protected static final short PLAYER_POWER_CODE               = 2;   // power subsystem
    protected static final short PLAYER_GRIPPER_CODE             = 3;   // gripper
    protected static final short PLAYER_POSITION_CODE            = 4;   // device that moves
    protected static final short PLAYER_SONAR_CODE               = 5;   // fixed range-finder
    protected static final short PLAYER_LASER_CODE               = 6;   // scanning range-finder
    protected static final short PLAYER_BLOBFINDER_CODE          = 7;   // visual blobfinder
    protected static final short PLAYER_PTZ_CODE                 = 8;   // pan-tilt-zoom unit
    protected static final short PLAYER_AUDIO_CODE               = 9;   // audio I/O
    protected static final short PLAYER_FIDUCIAL_CODE            = 10;  // fiducial detector
    protected static final short PLAYER_SPEECH_CODE              = 12;  // speech I/O
    protected static final short PLAYER_GPS_CODE                 = 13;  // GPS unit
    protected static final short PLAYER_BUMPER_CODE              = 14;  // bumper array
    protected static final short PLAYER_TRUTH_CODE               = 15;  // ground-truth (Stage)
    protected static final short PLAYER_IDARTURRET_CODE          = 16;  // ranging + comms
    protected static final short PLAYER_IDAR_CODE                = 17;  // ranging + comms
    protected static final short PLAYER_DESCARTES_CODE           = 18;  // Descartes platform
    protected static final short PLAYER_DIO_CODE                 = 20;  // digital I/O
    protected static final short PLAYER_AIO_CODE                 = 21;  // analog I/O
    protected static final short PLAYER_IR_CODE                  = 22;  // IR array
    protected static final short PLAYER_WIFI_CODE                = 23;  // wifi card status
    protected static final short PLAYER_WAVEFORM_CODE            = 24;  // fetch raw waveforms
    protected static final short PLAYER_LOCALIZE_CODE            = 25;  // localization
    protected static final short PLAYER_MCOM_CODE                = 26;  // multicoms
    protected static final short PLAYER_SOUND_CODE               = 27;  // sound file playback
    protected static final short PLAYER_AUDIODSP_CODE            = 28;  // audio dsp I/O
    protected static final short PLAYER_AUDIOMIXER_CODE          = 29;  // audio I/O
    protected static final short PLAYER_POSITION3D_CODE          = 30;  // 3-D position
    protected static final short PLAYER_SIMULATION_CODE          = 31;  // simulators
    protected static final short PLAYER_SERVICE_ADV_CODE         = 32;  // LAN advertisement
    protected static final short PLAYER_BLINKENLIGHT_CODE        = 33;  // blinking lights
    protected static final short PLAYER_NOMAD_CODE               = 34;  // Nomad robot
    protected static final short PLAYER_CAMERA_CODE              = 40;  // camera device(gazebo)
    protected static final short PLAYER_MAP_CODE                 = 42;  // get a map
    protected static final short PLAYER_PLANNER_CODE             = 44;  // 2D motion planner
    protected static final short PLAYER_LOG_CODE                 = 45;  // log R/W control
    protected static final short PLAYER_ENERGY_CODE              = 46;  // energy charging
    protected static final short PLAYER_MOTOR_CODE               = 47;  // motor interface
    protected static final short PLAYER_POSITION2D_CODE          = 48;  // 2-D position
    protected static final short PLAYER_JOYSTICK_CODE            = 49;  // Joystick
    protected static final short PLAYER_SPEECH_RECOGNITION_CODE  = 50;  // speech recognitionI/O
    protected static final short PLAYER_OPAQUE_CODE              = 51;  // plugin interface
    
    /* the device access modes */
    public static final short PLAYER_READ_MODE                = 114;   // 'r'
    public static final short PLAYER_WRITE_MODE               = 119;   // 'w'
    public static final short PLAYER_ALL_MODE                 = 97;    // 'a'
    public static final short PLAYER_CLOSE_MODE               = 99;    // 'c'
    public static final short PLAYER_ERROR_MODE               = 101;   // 'e'
	 
    /* total number of assigned interface codes (see player.h) */ 
    protected static int N_DEVICES   = 52;
    /** maximum devices of the same type */
    public static int MAX_DEVICES = 10;
    
    /* maximum size for request/reply.
     * this is a convenience so that the PlayerQueue can used fixed size elements.
     */
    protected static final short PLAYER_MAX_REQREP_SIZE = 4096; /* 4KB */
    
    /**
     * Data delivery mode: Send data at a fixed rate (default 10Hz; see 
     * requestDataDeliveryFrequency() below to change the rate) from ALL subscribed devices , 
     * regardless of whether the data is new or old. A PLAYER_MSGTYPE_SYNCH packet follows 
     * each set of data. Rarely used.
     * @see #requestDataDeliveryFrequency(short)
     */
    public static final byte PLAYER_DATAMODE_PUSH_ALL   = 0;
    /**
     * Data delivery mode: Only on request (see requestData () request below), send data from 
     * ALL subscribed devices, regardless of whether the data is new or old. A PLAYER_MSGTYPE_SYNCH 
     * packet follows each set of data.  Rarely used.
     * @see #requestData()
     */
    public static final byte PLAYER_DATAMODE_PULL_ALL   = 1;
    /**
     * Data delivery mode: Send data at a fixed rate (default 10Hz; see 
     * requestDataDeliveryFrequency() below to change the rate) only from those subscribed devices 
     * that have produced new data since the last time data was pushed to this client. A 
     * PLAYER_MSGTYPE_SYNCH packet follows each set of data.  This is the default mode.
     * @see #requestDataDeliveryFrequency(short)
     */
    public static final byte PLAYER_DATAMODE_PUSH_NEW   = 2;
    /**
     * Data delivery mode: Only on request (see requestData () request below), send data only 
     * from those subscribed devices that have produced new data since the last time data was pushed
     * to this client. Use this mode if your client runs slowly or at an upredictable rate (e.g., a 
     * GUI). 
     * A PLAYER_MSGTYPE_SYNCH packet follows each set of data.
     * @see #requestData()
     */
    public static final byte PLAYER_DATAMODE_PULL_NEW   = 3;
    /**
     * Data delivery mode: When a subscribed device produces new data, send it. This is the 
     * lowest-latency delivery mode; when a device produces data, the server (almost) immediately 
     * sends it on the client.  So the client may receive data at an arbitrarily high rate. 
     * PLAYER_MSGTYPE_SYNCH packets are still sent, but at a fixed rate (see 
     * requestDataDeliveryFrequency () to change this rate) that is unrelated to rate at which 
     * data are delivered from devices.
     * @see #requestDataDeliveryFrequency(short)
     */
    public static final byte PLAYER_DATAMODE_PUSH_ASYNC = 4;
    
    protected PlayerDevice deviceList[][]    = new PlayerDevice[N_DEVICES][MAX_DEVICES];
        
    private PlayerDeviceDriverInfo pddi;
    private PlayerDeviceDevlistT pddt;
    private boolean readyPDDT              = false;
    private boolean readyPDDI              = false;
    private boolean receivedAuthentication = false;
    private boolean readyPortNumber        = false;
    
    protected Socket socket;
    protected BufferedOutputStream buffer;
    /**
     * The input stream for the socket connected to the player server.
     */
    public    DataInputStream is;
    /**
     * The output stream for the socket connected to the player server. It's buffered, so remember
     * to flush()!
     */
    public    DataOutputStream os;

    private int portNumber;
	
    private long    millis;
    private int     nanos;
    private boolean isThreaded;
	
    /* used for creating PlayerDevice type objects on requestDeviceAccess () */
    private PlayerDevice newpd;
    
    private final int DIFFERENCE_SYNCH_FACTOR = 10;
    private int index = 0;
    
    /**
     * The PlayerClient constructor. Once called, it will create a socket with the Player server
     * running on host <b>servername</b> on port <b>portNumber</b>.
     * @param serverName url of the host running Player
     * @param portNumber the port number of the Player server
     */
    public PlayerClient (String serverName, int portNumber) {
        try {
            /* initialize network connection */
            socket = new Socket (serverName, portNumber);
            /* open the proper streams (I/O) */
            is     = new DataInputStream (socket.getInputStream ());
            buffer = new BufferedOutputStream (socket.getOutputStream (), 128);
            os     = new DataOutputStream (new DataOutputStream (buffer));
            /* write the player version number (manual says version string is 32 chars) */
            for (int i = 0; i < 32; i++) {
                char c = (char)is.readByte ();
                System.err.print (c);
            }
            System.err.println ();
            /* initialize the device list */
            for (int i = 0; i < N_DEVICES; i++)
                for (int j = 0; j < MAX_DEVICES; j++) 
                    deviceList[i][j] = null;
        } catch (Exception e) {
            System.err.println ("[PlayerClient] : Error in PlayerClient init: " + e.toString ());
            System.exit (1);
        }
    }
	
    /**
     * The PlayerClient "destructor". Once called, it will close all the open streams/sockets 
     * with the Player server.
     */
    public void close () {
        try {
            /* close all sockets */
        	os.close     ();
            buffer.close ();
            is.close     ();
            socket.close ();
        } catch (Exception e) {
            System.err.println ("[PlayerClient] : Error in PlayerClient stop: " + e.toString ());
            System.exit (1);
        }
    }

    /**
     * Change the mode Javaclient runs to non-threaded.
     */
    public void setNotThreaded() {
    	isThreaded = false;
    }
    
    /**
     * Start a threaded copy of Javaclient.
     * @param millis number of miliseconds to sleep between calls 
     * @param nanos number of nanoseconds to sleep between calls
     */
    public void runThreaded (long millis, int nanos) {
        if (isThreaded) {
            System.err.println ("[PlayerClient] : A second call for runThreaded, ignoring!");
            return;
        }
	    this.millis = millis;
	    this.nanos  = nanos;
	    isThreaded  = true;
	    this.start ();
	}
	
    /**
     * Start the Javaclient thread. Ran automatically from runThreaded ().
     */
    public void run () {
        try {
            while (isThreaded) {
                while (read () != PLAYER_MSGTYPE_SYNCH && isThreaded);
                if (millis < 0)
                    Thread.yield ();
                else
                    if (nanos <= 0)
                        Thread.sleep (millis);
                    else
                        Thread.sleep (millis, nanos);
            }
        } catch (Exception e) { e.printStackTrace (); }
    }
	 
    /**
     * Request a Power device. 
     * @param index the device index
     * @param r access mode
     * @return a Power device if successful, null otherwise
     */
    public PowerInterface requestInterfacePower (int index, char r) {
        return (PowerInterface)requestDeviceAccess (PLAYER_POWER_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a Gripper device. 
     * @param index the device index
     * @param r access mode
     * @return a Gripper device if successful, null otherwise
     */
    public GripperInterface requestInterfaceGripper (int index, char r) {
        return (GripperInterface)requestDeviceAccess (PLAYER_GRIPPER_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a Position device. 
     * @param index the device index
     * @param r access mode
     * @return a Position device if successful, null otherwise
     */
    public PositionInterface requestInterfacePosition (int index, char r) {
        return (PositionInterface)requestDeviceAccess (PLAYER_POSITION_CODE,   
                (short)index, r);
    }
	
    /**
     * Request a Sonar device. 
     * @param index the device index
     * @param r access mode
     * @return a Sonar device if successful, null otherwise
     */
    public SonarInterface requestInterfaceSonar (int index, char r) {
        return (SonarInterface)requestDeviceAccess (PLAYER_SONAR_CODE, 
                (short)index, r);
    }
	
    /**
     * Request a Laser device. 
     * @param index the device index
     * @param r access mode
     * @return a Laser device if successful, null otherwise
     */
    public LaserInterface requestInterfaceLaser (int index, char r) {
        return (LaserInterface)requestDeviceAccess (PLAYER_LASER_CODE, 
                (short)index, r);
    }
	
    /**
     * Request a Blobfinder device. 
     * @param index the device index
     * @param r access mode
     * @return a Blobfinder device if successful, null otherwise
     */
    public BlobfinderInterface requestInterfaceBlobfinder (int index, char r) {
        return (BlobfinderInterface)requestDeviceAccess (PLAYER_BLOBFINDER_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a Ptz device. 
     * @param index the device index
     * @param r access mode
     * @return a Ptz device if successful, null otherwise
     */
    public PtzInterface requestInterfacePtz (int index, char r) {
        return (PtzInterface)requestDeviceAccess (PLAYER_PTZ_CODE, 
                (short)index, r);
    }
    
    /**
     * Request an Audio device. 
     * @param index the device index
     * @param r access mode
     * @return an Audio device if successful, null otherwise
     */
    public AudioInterface requestInterfaceAudio (int index, char r) {
        return (AudioInterface)requestDeviceAccess (PLAYER_AUDIO_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a Fiducial device. 
     * @param index the device index
     * @param r access mode
     * @return a Fiducial device if successful, null otherwise
     */
    public FiducialInterface requestInterfaceFiducial (int index, char r) {
        return (FiducialInterface)requestDeviceAccess (PLAYER_FIDUCIAL_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a Speech device. 
     * @param index the device index
     * @param r access mode
     * @return a Speech device if successful, null otherwise
     */
    public SpeechInterface requestInterfaceSpeech (int index, char r) {
        return (SpeechInterface)requestDeviceAccess (PLAYER_SPEECH_CODE, 
                (short)index, r);
    }

    /**
     * Request a GPS device. 
     * @param index the device index
     * @param r access mode
     * @return a GPS device if successful, null otherwise
     */
    public GPSInterface requestInterfaceGPS (int index, char r) {
        return (GPSInterface)requestDeviceAccess (PLAYER_GPS_CODE, 
                (short)index, r);
    }

    /**
     * Request a Bumper device. 
     * @param index the device index
     * @param r access mode
     * @return a Bumper device if successful, null otherwise
     */
    public BumperInterface requestInterfaceBumper (int index, char r) {
        return (BumperInterface)requestDeviceAccess (PLAYER_BUMPER_CODE, 
                (short)index, r);
    }

    /**
     * Request a Truth device. 
     * @param index the device index
     * @param r access mode
     * @return a Truth device if successful, null otherwise
     */
    public TruthInterface requestInterfaceTruth (int index, char r) {
        return (TruthInterface)requestDeviceAccess (PLAYER_TRUTH_CODE, 
                (short)index, r);
    }

    /**
     * Request a DIO device. 
     * @param index the device index
     * @param r access mode
     * @return a DIO device if successful, null otherwise
     */
    public DIOInterface requestInterfaceDIO (int index, char r) {
        return (DIOInterface)requestDeviceAccess (PLAYER_DIO_CODE, 
                (short)index, r);
    }

    /**
     * Request an AIO device. 
     * @param index the device index
     * @param r access mode
     * @return an AIO device if successful, null otherwise
     */
    public AIOInterface requestInterfaceAIO (int index, char r) {
        return (AIOInterface)requestDeviceAccess (PLAYER_AIO_CODE, 
                (short)index, r);
    }

    /**
     * Request an IR device. 
     * @param index the device index
     * @param r access mode
     * @return an IR device if successful, null otherwise
     */
    public IRInterface requestInterfaceIR (int index, char r) {
        return (IRInterface)requestDeviceAccess (PLAYER_IR_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a WiFi device. 
     * @param index the device index
     * @param r access mode
     * @return a WiFi device if successful, null otherwise
     */
    public WiFiInterface requestInterfaceWiFi (int index, char r) {
        return (WiFiInterface)requestDeviceAccess (PLAYER_WIFI_CODE, 
                (short)index, r);
    }

    /**
     * Request a Waveform device. 
     * @param index the device index
     * @param r access mode
     * @return a Waveform device if successful, null otherwise
     */
    public WaveformInterface requestInterfaceWaveform (int index, char r) {
        return (WaveformInterface)requestDeviceAccess (PLAYER_WAVEFORM_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a Localize device. 
     * @param index the device index
     * @param r access mode
     * @return a Localize device if successful, null otherwise
     */
    public LocalizeInterface requestInterfaceLocalize (int index, char r) {
        return (LocalizeInterface)requestDeviceAccess (PLAYER_LOCALIZE_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a MComm device. 
     * @param index the device index
     * @param r access mode
     * @return a MComm device if successful, null otherwise
     */
    public MComInterface requestInterfaceMCom (int index, char r) {
        return (MComInterface)requestDeviceAccess (PLAYER_MCOM_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a Sound device. 
     * @param index the device index
     * @param r access mode
     * @return a Sound device if successful, null otherwise
     */
    public SoundInterface requestInterfaceSound (int index, char r) {
        return (SoundInterface)requestDeviceAccess (PLAYER_SOUND_CODE, 
                (short)index, r);
    }
    
    /**
     * Request an AudioDSP device. 
     * @param index the device index
     * @param r access mode
     * @return an AudioDSP device if successful, null otherwise
     */
    public AudioDSPInterface requestInterfaceAudioDSP (int index, char r) {
        return (AudioDSPInterface)requestDeviceAccess (PLAYER_AUDIODSP_CODE, 
                (short)index, r);
    }
    
    /**
     * Request an AudioMixer device. 
     * @param index the device index
     * @param r access mode
     * @return an AudioMixer device if successful, null otherwise
     */
    public AudioMixerInterface requestInterfaceAudioMixer (int index, char r) {
        return (AudioMixerInterface)requestDeviceAccess (PLAYER_AUDIOMIXER_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a Position3D device. 
     * @param index the device index
     * @param r access mode
     * @return a Position3D device if successful, null otherwise
     */
    public Position3DInterface requestInterfacePosition3D (int index, char r) {
        return (Position3DInterface)requestDeviceAccess (PLAYER_POSITION3D_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a Simulation device. 
     * @param index the device index
     * @param r access mode
     * @return a Simulation device if successful, null otherwise
     */
    public SimulationInterface requestInterfaceSimulation (int index, char r) {
        return (SimulationInterface)requestDeviceAccess (PLAYER_SIMULATION_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a Blinkenlight device. 
     * @param index the device index
     * @param r access mode
     * @return a Blinkenlight device if successful, null otherwise
     */
    public BlinkenlightInterface requestInterfaceBlinkenlight (int index, char r) {
        return (BlinkenlightInterface)requestDeviceAccess (PLAYER_BLINKENLIGHT_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a Nomad device. 
     * @param index the device index
     * @param r access mode
     * @return a Nomad device if successful, null otherwise
     */
    public NomadInterface requestInterfaceNomad (int index, char r) {
        return (NomadInterface)requestDeviceAccess (PLAYER_NOMAD_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a Camera device. 
     * @param index the device index
     * @param r access mode
     * @return a Camera device if successful, null otherwise
     */
    public CameraInterface requestInterfaceCamera (int index, char r) {
        return (CameraInterface)requestDeviceAccess (PLAYER_CAMERA_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a Map device. 
     * @param index the device index
     * @param r access mode
     * @return a Map device if successful, null otherwise
     */
    public MapInterface requestInterfaceMap (int index, char r) {
        return (MapInterface)requestDeviceAccess (PLAYER_MAP_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a Planner device. 
     * @param index the device index
     * @param r access mode
     * @return a Planner device if successful, null otherwise
     */
    public PlannerInterface requestInterfacePlanner (int index, char r) {
        return (PlannerInterface)requestDeviceAccess (PLAYER_PLANNER_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a Log device. 
     * @param index the device index
     * @param r access mode
     * @return a Log device if successful, null otherwise
     */
    public LogInterface requestInterfaceLog (int index, char r) {
        return (LogInterface)requestDeviceAccess (PLAYER_LOG_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a Energy device. 
     * @param index the device index
     * @param r access mode
     * @return a Energy device if successful, null otherwise
     */
    public EnergyInterface requestInterfaceEnergy (int index, char r) {
        return (EnergyInterface)requestDeviceAccess (PLAYER_ENERGY_CODE, 
                (short)index, r);
    }

    /**
     * Request a Motor device. 
     * @param index the device index
     * @param r access mode
     * @return a Motor device if successful, null otherwise
     */
    public MotorInterface requestInterfaceMotor (int index, char r) {
        return (MotorInterface)requestDeviceAccess (PLAYER_MOTOR_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a Position2D device. 
     * @param index the device index
     * @param r access mode
     * @return a Position2D device if successful, null otherwise
     */
    public Position2DInterface requestInterfacePosition2D (int index, char r) {
        return (Position2DInterface)requestDeviceAccess (PLAYER_POSITION2D_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a Joystick device. 
     * @param index the device index
     * @param r access mode
     * @return a Joystick device if successful, null otherwise
     */
    public JoystickInterface requestInterfaceJoystick (int index, char r) {
        return (JoystickInterface)requestDeviceAccess (PLAYER_JOYSTICK_CODE, 
                (short)index, r);
    }
    
    /**
     * Request a Speech Recognition device. 
     * @param index the device index
     * @param r access mode
     * @return a Speech Recognition device if successful, null otherwise
     */
    public SpeechRecognitionInterface requestInterfaceSpeechRecognition (int index, char r) {
        return (SpeechRecognitionInterface)requestDeviceAccess (PLAYER_SPEECH_RECOGNITION_CODE, 
                (short)index, r);
    }
    
    /**
     * Sends a Player message header filled with the given values.
     * @param type type of message (DATA, CMD, REQ, RESP_ACK, SYNCH, RESP_NACK, RESP_ERR)
     * @param device type of device
     * @param index the device index
     * @param size size in bytes of the payload to follow
     */
    private void sendHeader (short type, short device, short index, int size) {
        try {
            /* see player.h / player_msghdr for additional explanations */
            os.writeShort (PLAYER_STXX);  /* 0x5878 - the message start signifier */
            os.writeShort (type);         /* DATA, CMD, REQ, RESP_ACK, SYNCH, RESP_NACK, RESP_ERR */
            os.writeShort (device);       /* what kind of device */
            os.writeShort (index);        /* which device of what kind */
            os.writeInt (0);              /* server's current time (seconds) */
            os.writeInt (0);              /* server's current time (microseconds) */
            os.writeInt (0);              /* time when the current data was generated (seconds) */
            os.writeInt (0);              /* time when the current data was generated (u seconds) */
            os.writeInt (0);              /* reserved */
            os.writeInt (size);           /* size in bytes of the payload to follow */
        } catch (Exception e) {
            System.err.println ("[PlayerClient] : Error when reading header: " + e.toString ());
        }
    }
	
    /**
     * Configuration request: Get the list of available devices.
     * <br><br>
     * It's useful for applications such as viewer programs and test suites that tailor behave 
     * differently depending on which devices are available.
     */
    public void requestDeviceList () {
       try {
       	    sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_PLAYER_CODE, (short)0, 2); /* 2 bytes payload */ 
       	    os.writeShort (PLAYER_PLAYER_DEVLIST_REQ);
       	    os.flush ();
       } catch (Exception e) {
       	    System.err.println ("[PlayerClient] : Couldn't request device list " + e.toString ());
       }
    }
    
    /**
     * Configuration request: Get the driver name for a particular device.
     * @param device the device identifier
     */
    public void requestDriverName (short device) {
       try {
            sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_PLAYER_CODE, (short)0, 4); /* 4 bytes payload */ 
            os.writeShort (PLAYER_PLAYER_DRIVERINFO_REQ);
            os.writeShort (device);
            os.flush ();
       } catch (Exception e) {
            System.err.println ("[PlayerClient] : Couldn't request driver name " + e.toString ());
       }
    }
    
    /**
     * Configuration request: Get device access.
     * <br><br>
     * This is the most important request! Before interacting with a device, the client must 
     * request appropriate access.
     * <br><br>
     * The access codes, which are used in both the request and response, are given above. Read 
     * access means that the server will start sending data from the specified device. For 
     * instance, if read access is obtained for the sonar device Player will start sending sonar 
     * data to the client. Write access means that the client has permission to control the 
     * actuators of the device. There is no locking mechanism so different clients can have 
     * concurrent write access to the same actuators. All access is both of the above and finally 
     * close means that there is no longer any access to the device. Device request messages can 
     * be sent at any time, providing on the fly reconfiguration for clients that need different 
     * devices depending on the task at hand.
     * <br><br>
     * Of course, not all of the access codes are applicable to all devices; for instance it does 
     * not make sense to write to the sonars. However, a request for such access will not generate 
     * an error; rather, it will be granted, but any commands actually sent to that device will be 
     * ignored. In response to such a device request, the server will send a reply indicating the 
     * actual access that was granted for the device. The granted access may be different from the 
     * requested access; in particular, if there was some error in initializing the device the 
     * granted access will be error, and the client should not try to read from or write to the 
     * device.
     * @param rDevice the interface for the device
     * @param index the index for the device
     * @param r the requested access
     * @return an object of PlayerDevice type
     */
    private PlayerDevice requestDeviceAccess (short rDevice, short index, char r) {
        if (isDebugging)
        	System.err.println ("[PlayerClient][Debug] : Subscribing to " + rDevice);
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_PLAYER_CODE, (short)0, 7);/* 7 bytes payload */ 
            /* see player.h / player_device_req for additional explanations */
            os.writeShort (PLAYER_PLAYER_DEV_REQ);  /* subtype - must be PLAYER_PLAYER_DEV_REQ */
            os.writeShort (rDevice);                /* the interface for the device */
            os.writeShort (index);                  /* the index for the device */
            os.writeByte ((byte)r);                 /* requested access rights */
            os.flush ();
            short result = 0;
            while ((result = requestSatisfy()) != PLAYER_MSGTYPE_RESP_ACK) {
                if (result == -2)
                    System.exit (1);
            };
        } catch (Exception e) {
            System.err.println ("[PlayerClient] : Request data error: " + e.toString ());
        }
        return newpd;		
    }
	
    /**
     * Handle several Player replies. If PLAYER_MSGTYPE_RESP_ACK after a requestDeviceAccess (), 
     * creates a newpd object of a PlayerDevice type.
     * @return the message type that Player replied with.
     */
    private short requestSatisfy () {
        short type = -1;
        try {
            while (is.readShort () != PLAYER_STXX)	/* wait for the STX */
                if (isDebugging)
                    System.err.println ("[PlayerClient][Debug] : Unrecognized header: ");
            
            type = is.readShort ();      /* DATA, CMD, REQ, RESP_ACK, SYNCH, RESP_NACK, RESP_ERR */
            short device = is.readShort ();	/* what kind of device */
            short index  = is.readShort ();	/* which device of what kind */
            if (isDebugging)
                System.err.println ("[PlayerClient][Debug] : Got a message: type = " + type + 
                        " device = " + device + " index = " + index);

            if (type == PLAYER_MSGTYPE_DATA) {
                try {
                    deviceList[(int)device][(int)index].readData ();
                } catch (NullPointerException npe) {
                    System.err.println ("[PlayerClient] : Got a message: type = " + type + 
                            " device = " + device + " index = " + index);
                }
                return -1;
            }

            /* ignore the time_sec, time_usec, timestamp_sec, timestamp_usec and reserved fields */
            is.readInt (); is.readInt (); is.readInt (); is.readInt (); is.readInt ();
            int size = is.readInt ();			/* read the packet size */

            if (type == PLAYER_MSGTYPE_SYNCH) {
                for (int i = 0; i < size; i++, is.readByte ());
                return -1;
            } else if (type == PLAYER_MSGTYPE_RESP_NACK)
                deviceList[(int)device][(int)index].handleNARMessage ();
            else if (type == PLAYER_MSGTYPE_RESP_ERR)
                deviceList[(int)device][(int)index].handleEARMessage ();

            /* PLAYER_MSGTYPE_RESP_ACK */
            else {
                if (isDebugging)
                    System.err.println ("[PlayerClient][Debug] : Got responce: " + device + 
                            " size of payload: " + size);

                /* see player.h / player_device_resp for additional explanations */
                is.readShort ();                    /* subtype - ignored */
                short device2 = is.readShort ();    /* the interface for the device */
                short index2  = is.readShort ();    /* the index for the device */
                byte r = is.readByte ();            /* requested access rights */
                System.err.print ("[PlayerClient] : Got response for device " + device2 + "(");
                for (int i = 0; i < PLAYER_MAX_DEVICE_STRING_LEN; i++) {
                    char c = (char)is.readByte ();
                    if (c != 0)
                    	System.err.print (c);
                }
                System.err.println (") with access: " + (char)r);

                if (r == PLAYER_ERROR_MODE) {
                    System.err.println ("[PlayerClient] : Error was replied from the server!");
                    return -2;
                }
                newpd = null;
            
                switch (device2) {
                    case PLAYER_NULL_CODE: {                /* /dev/null analogue */
                    	break;
                    }
                    case PLAYER_PLAYER_CODE: {              /* the server itself */
                        break;
                    }
                    case PLAYER_POWER_CODE: {               /* power subsystem */
                    	newpd = new PowerInterface (this, index2);
                    	break;
                    }
                    case PLAYER_GRIPPER_CODE: {             /* gripper */
                        newpd = new GripperInterface (this, index2);
                        break;
                    }
                    case PLAYER_POSITION_CODE: {            /* device that moves */
                    	newpd = new PositionInterface (this, index2);
                    	break;
                    }
                    case PLAYER_SONAR_CODE: {               /* fixed range-finder */
                    	newpd = new SonarInterface (this, index2);
                    	break;
                    }
                    case PLAYER_LASER_CODE: {               /* scanning range-finder */
                    	newpd = new LaserInterface (this, index2);
                    	break;
                    }
                    case PLAYER_BLOBFINDER_CODE: {          /* visual blobfinder */
                        newpd = new BlobfinderInterface (this, index2);
                        break;
                    }
                    case PLAYER_PTZ_CODE: {                 /* pan-tilt-zoom unit */
                        newpd = new PtzInterface (this, index2);
                        break;
                    }
                    case PLAYER_AUDIO_CODE: {               /* audio I/O */
                        newpd = new AudioInterface (this, index2);
                        break;
                    }
                    case PLAYER_FIDUCIAL_CODE: {            /* fiducial detector */
                        newpd = new FiducialInterface (this, index2);
                        break;
                    }
                    case PLAYER_SPEECH_CODE: {              /* speech I/O */
                        newpd = new SpeechInterface (this, index2);
                        break;
                    }
                    case PLAYER_GPS_CODE: {                 /* GPS unit */
                        newpd = new GPSInterface (this, index2);
                        break;
                    }
                    case PLAYER_BUMPER_CODE: {              /* bumper array */
                        newpd = new BumperInterface (this, index2);
                        break;
                    }
                    case PLAYER_TRUTH_CODE: {               /* ground-truth (Stage) */
                        newpd = new TruthInterface (this, index2);
                        break;
                    }
                    case PLAYER_IDARTURRET_CODE: {          /* ranging + comms */
                        /* Player support obsolete? */
                        break;
                    }
                    case PLAYER_IDAR_CODE: {                /* ranging + comms */
                        /* Player support obsolete? */
                        break;
                    }
                    case PLAYER_DESCARTES_CODE: {           /* descartes platform */
                        /* Player support obsolete? */
                        break;
                    }
                    case PLAYER_DIO_CODE: {                 /* digital I/O */
                        newpd = new DIOInterface (this, index2);
                        break;
                    }
                    case PLAYER_AIO_CODE: {                 /* analog I/O */
                        newpd = new AIOInterface (this, index2);
                        break;
                    }
                    case PLAYER_IR_CODE: {                  /* IR array */
                        newpd = new IRInterface (this, index2);
                        break;
                    }
                    case PLAYER_WIFI_CODE: {                /* wifi card status */
                        newpd = new WiFiInterface (this, index2);
                        break;
                    }
                    case PLAYER_WAVEFORM_CODE: {            /* fetch raw waveforms */
                        newpd = new WaveformInterface (this, index2);
                        break;
                    }
                    case PLAYER_LOCALIZE_CODE: {            /* localization */
                        newpd = new LocalizeInterface (this, index2);
                        break;
                    }
                    case PLAYER_MCOM_CODE: {                /* multicoms */
                        newpd = new MComInterface (this, index2);
                        break;
                    }
                    case PLAYER_SOUND_CODE: {               /* sound file playback */
                        newpd = new SoundInterface (this, index2);
                        break;
                    }
                    case PLAYER_AUDIODSP_CODE: {            /* audio DSP I/O */
                        newpd = new AudioDSPInterface (this, index2);
                        break;
                    }
                    case PLAYER_AUDIOMIXER_CODE: {          /* audio I/O */
                        newpd = new AudioMixerInterface (this, index2);
                        break;
                    }
                    case PLAYER_POSITION3D_CODE: {          /* 3-D position */
                        newpd = new Position3DInterface (this, index2);
                        break;
                    }
                    case PLAYER_SIMULATION_CODE: {          /* simulators */
                        newpd = new SimulationInterface (this, index2);
                        break;
                    }
                    case PLAYER_SERVICE_ADV_CODE: {         /* LAN advertisement */
                        /* Player support obsolete? */
                        break;
                    }
                    case PLAYER_BLINKENLIGHT_CODE: {        /* blinking lights */
                        newpd = new BlinkenlightInterface (this, index2);
                        break;
                    }
                    case PLAYER_NOMAD_CODE: {               /* nomad robot */
                        newpd = new NomadInterface (this, index2);
                        break;
                    }
                    case PLAYER_CAMERA_CODE: {              /* camera device (gazebo) */
                        newpd = new CameraInterface (this, index2);
                        break;
                    }
                    case PLAYER_MAP_CODE: {                 /* get a map */
                        newpd = new MapInterface (this, index2);
                        break;
                    }
                    case PLAYER_PLANNER_CODE: {             /* 2D motion planner */
                        newpd = new PlannerInterface (this, index2);
                        break;
                    }
                    case PLAYER_LOG_CODE: {                 /* log R/W control */
                        newpd = new LogInterface (this, index2);
                        break;
                    }
                    case PLAYER_ENERGY_CODE: {              /* energy charging */
                        newpd = new EnergyInterface (this, index2);
                        break;
                    }
                    case PLAYER_MOTOR_CODE: {               /* motor interface */
                        newpd = new MotorInterface (this, index2);
                        break;
                    }
                    case PLAYER_POSITION2D_CODE: {          /* 2-D position */
                        newpd = new Position2DInterface (this, index2);
                        break;
                    }
                    case PLAYER_JOYSTICK_CODE: {            /* joystick */
                        newpd = new JoystickInterface (this, index2);
                        break;
                    }
                    case PLAYER_SPEECH_RECOGNITION_CODE: {  /* speech recognition I/O */
                        newpd = new SpeechRecognitionInterface (this, index2);
                        break;
                    }
                    case PLAYER_OPAQUE_CODE: {              /* plugin interface */
                        break;
                    }
                    default: {
                    	System.err.println ("[PlayerClient] : Unsupported device error! - " +
                                device2);
                    	newpd = null;
                    	r     = PLAYER_ERROR_MODE;
                    	break;
                    }
                }
                
                if (newpd != null) {
                		/* add the device to the list */
                    	deviceList[device2][index2] = newpd;
                }
            }
        } catch (EOFException e) {
            System.err.println ("[PlayerClient] : java.io.EOFException : Is the Player server still running?");
            if (stopOnEOFException)
                System.exit (1);
        } catch (Exception e) {
            System.err.println ("[PlayerClient] : Read error: " + e.toString ());
            e.printStackTrace ();
        }
        return type;
    }
	
    /**
     * Configuration request: Get data.
     * <br><br>
     * When the server is in a PLAYER_DATAMODE_PULL_* data delivery mode, the client can request a 
     * single round of data by sending a zero-argument request with type code 0x0003. The response 
     * will be a zero-length acknowledgement. The client only needs to make this request when a 
     * PLAYER_DATAMODE_PULL_* mode is in use.
     */
    public void requestData () {
    	try {
            sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_PLAYER_CODE, (short)0, 2); /* 2 bytes payload */ 
            os.writeShort (PLAYER_PLAYER_DATA_REQ);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[PlayerClient] : Couldn't request one round data " + 
                    e.toString ());
        }
    }
    
    /**
     * Configuration request: Change data delivery mode.
     * <br><br>
     * The Player server supports four data modes, described above. By default, the server operates 
     * in PLAYER_DATAMODE_PUSH_NEW mode at a frequency of 10Hz. To switch to a different mode send a 
     * request with the format given below. The server's reply will be a zero-length 
     * acknowledgement.
     * @param mode the requested mode
     */
    public void requestDataDeliveryMode (byte mode) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_PLAYER_CODE, (short)0, 3); /* 3 bytes payload */ 
            os.writeShort (PLAYER_PLAYER_DATAMODE_REQ);
            os.writeByte (mode);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[PlayerClient] : Couldn't request change of data mode " + 
                    e.toString ());
        }
    }
    
    /**
     * Configuration request: Change data delivery frequency.
     * <br><br>
     * By default, the fixed frequency for the PUSH data delivery modes is 10Hz; thus a client 
     * which makes no configuration changes will receive sensor data approximately every 100ms. 
     * The server can send data faster or slower; to change the frequency, send a request with 
     * this format. The server's reply will be a zero-length acknowledgement.
     * @param frequency requested frequency in Hz 
     */
    public void requestDataDeliveryFrequency (short frequency) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_PLAYER_CODE, (short)0, 4); /* 4 bytes payload */ 
            os.writeShort (PLAYER_PLAYER_DATAFREQ_REQ);
            os.writeShort (frequency);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[PlayerClient] : Couldn't request change of data frequency " + 
                    e.toString ());
        }
    }
    
    /**
     * Configuration request: Authentication.
     * <br><br>
     * If server authentication has been enabled (by providing '-key <key>' on the command-line; 
     * see Command line options); then each client must authenticate itself before otherwise 
     * interacting with the server. To authenticate, send a request with this format.
     * <br><br>
     * If the key matches the server's key then the client is authenticated, the server will reply 
     * with a zero-length acknowledgement, and the client can continue with other operations. If 
     * the key does not match, or if the client attempts any other server interactions before 
     * authenticating, then the connection will be closed immediately. It is only necessary to 
     * authenticate each client once.
     * <br><br>
     * Note that this support for authentication is NOT a security mechanism. The keys are always 
     * in plain text, both in memory and when transmitted over the network; further, since the key 
     * is given on the command-line, there is a very good chance that you can find it in plain text 
     * in the process table (in Linux try 'ps -ax | grep player'). Thus you should not use an 
     * important password as your key, nor should you rely on Player authentication to prevent bad 
     * guys from driving your robots (use a firewall instead). Rather, authentication was introduced 
     * into Player to prevent accidentally connecting one's client program to someone else's robot. 
     * This kind of accident occurs primarily when Stage is running in a multi-user environment. In 
     * this case it is very likely that there is a Player server listening on port 6665, and clients 
     * will generally connect to that port by default, unless a specific option is given.
     * <br><br>
     * This mechanism was never really used, and may be removed. 
     * @param key the authentication key
     */
    public void requestAuthentication (byte[] key) {
        try {
            if (key.length > 32) 
                throw new Exception ("[PlayerClient] : Supplied authentication key is " + 
                        key.length + " but should be <= 32 bytes");
            sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_PLAYER_CODE, (short)0, 34);/* 34 bytes payload */ 
            os.writeShort (PLAYER_PLAYER_AUTH_REQ);
            for (int i = 0; i < key.length; i++)
                os.writeByte (key[i]);
            for (int i = 0; i < 32 - key.length; i++)
                os.writeByte ((byte)0);
            os.flush ();
        } catch (Exception e) {
        	System.err.println ("[PlayerClient] : Couldn't request authentication " + 
                    e.toString ());
        }
    }
    
    /**
     * Use nameservice to get the corresponding port for a robot name (only with Stage).
     * @param name the robot name
     */
    public void requestNameService (char[] name) {
        try {
            int totalSize = PLAYER_MAX_DEVICE_STRING_LEN + 2;
            /* 4 bytes payload */
            sendHeader (PLAYER_MSGTYPE_REQ, PLAYER_PLAYER_CODE, (short)0, totalSize); 
            os.writeShort (PLAYER_PLAYER_NAMESERVICE_REQ);
            for (int i = 0; i < name.length; i++) {
            	os.writeByte ((byte)name[i]);
            }
            for (int i = 0; i < PLAYER_MAX_DEVICE_STRING_LEN - name.length; i++) {
                os.writeByte (0);
            }
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[PlayerClient] : Couldn't request name service " + e.toString ());
        }
    }

    /**
     * Get the device list index.
     * @return device list index as an integer
     * @see #incI(int)
     */
    public synchronized int getI () { return index; }

    /**
     * Increments the device list index.
     * @param increment number of increments
     * @see #getI()
     */
    public synchronized void incI (int increment) {
    	if (Math.abs (index - Integer.MAX_VALUE) <= DIFFERENCE_SYNCH_FACTOR)
            index = 0;
        index += increment;
    }
	
    /**
     * Read the Player server replies (for a threaded Javaclient).
     * <br><br> 
     * Message header field and types:<br>
     * STX   type 	 device index 	t_sec 	t_usec  ts_sec  ts_usec reserved size<br>
     * short short 	 short 	short 	int 	int 	int 	int 	int 	 int<br>
     * @return the message type code
     */
    private short read () {
        short type = -1;
        try {
            while (is.readShort () != PLAYER_STXX);   /* wait for the STX */
            type = is.readShort ();                   /* read the type of message */
            //if (isDebugging) 
                //System.err.println ("[PlayerClient][Debug] : Entered = " + type);
            /* verify the message type code - see "Message Formats" from the Player manual */
            switch (type) {
                case PLAYER_MSGTYPE_DATA: {       /* Data message */
                    short device = is.readShort ();
                    short index  = is.readShort ();
                    //if (isDebugging)
                        //System.err.println ("[PlayerClient][Debug] : Data for " + device + 
                                //" " + index);
                    deviceList[(int)device][(int)index].readData ();
                    break;
                }
                case PLAYER_MSGTYPE_CMD: {        /* Command message */
                    System.err.println ("[PlayerClient] : Client shouldn't receive a cmd message!");
                    break;
                }
                case PLAYER_MSGTYPE_REQ: {        /* Request message */
                    System.err.println ("[PlayerClient] : Client shouldn't receive a req message!");
                    break;
                }
                case PLAYER_MSGTYPE_RESP_ACK: {   /* Acknowledgement response message */
                    short device = is.readShort ();
                    short index  = is.readShort ();
                    /* ignore the time_sec, time_usec, timestamp_sec, timestamp_usec and reserved */
                    is.readInt (); is.readInt (); is.readInt (); is.readInt (); is.readInt ();
                    int size = is.readInt ();        /* read the packet size */
                    if (device == 1) {               /* the player server device interface */
                        handleResponse (size);       /* handle the payload */
                        break;
                    }
                    deviceList[(int)device][(int)index].handleResponse (size);
                    break;
                }
                case PLAYER_MSGTYPE_SYNCH: {      /* Synchronization message */
                    /* ignore the device and index fields */
                    is.readShort (); is.readShort ();
                    /* ignore the time_sec, time_usec, timestamp_sec, timestamp_usec and reserved */
                    is.readInt (); is.readInt (); is.readInt (); is.readInt (); is.readInt ();
                    /* ignore the size field */
                    is.readInt ();
                    break;
                }
                case PLAYER_MSGTYPE_RESP_NACK: {  /* Negative acknowledgement response message */
                    short device = is.readShort ();
                    short index  = is.readShort ();
                    /* ignore the time_sec, time_usec, timestamp_sec, timestamp_usec and reserved */
                    is.readInt (); is.readInt (); is.readInt (); is.readInt (); is.readInt ();
                    deviceList[(int)device][(int)index].handleNARMessage ();
                    break;
                }
                case PLAYER_MSGTYPE_RESP_ERR: {   /* Error response message */
                    short device = is.readShort ();
                    short index  = is.readShort ();
                    /* ignore the time_sec, time_usec, timestamp_sec, timestamp_usec and reserved */
                    is.readInt (); is.readInt (); is.readInt (); is.readInt (); is.readInt ();
                    deviceList[(int)device][(int)index].handleEARMessage ();
                    break;
                }
                default: {
                    System.err.println ("[PlayerClient] : Unknown message type received in read()");
                    break;
                }
            }
        } catch (EOFException e) {
            System.err.println ("[PlayerClient] : java.io.EOFException : Is the Player server still running?");
            if (stopOnEOFException)
                System.exit (1);
        } catch (SocketException e) {
            System.err.println ("[PlayerClient] : java.io.SocketException : Is the Player server still running?");
            if (stopOnEOFException)
                System.exit (1);
        } catch (Exception e) {
            System.err.println ("[PlayerClient] : Read error: " + e.toString ());
            e.printStackTrace ();
        }
        return type;
    }
    
    /**
     * Read the Player server replies in non-threaded mode.
     */
    public void readAll () {
    	if (isThreaded) return;
        while (read () != PLAYER_MSGTYPE_SYNCH);
    }
    
    /**
     * Handle acknowledgement response messages (threaded mode).
     * @param size size of the payload
     */
    public void handleResponse (int size) {
        if (size == 0) {
            System.err.println ("[PlayerClient] : Unexpected response of size 0!");
            return;
        }
        try {
            /* each reply begins with a uint16_t subtype field */
            short subtype = is.readShort ();
            switch (subtype) {
                /* Configuration request: Get the list of available devices. */
                case PLAYER_PLAYER_DEVLIST_REQ: {		/* get device list */
                    pddt      = new PlayerDeviceDevlistT ();
	    	    	
                    pddt.setDeviceCount (is.readShort ()); /* the number of devices */ 
                    PlayerDeviceIdT[] playerDevList = new PlayerDeviceIdT[pddt.getDeviceCount ()];
                    for (int i = 0; i < pddt.getDeviceCount (); i++) {
                        playerDevList[i]       = new PlayerDeviceIdT ();
                        playerDevList[i].setCode  (is.readShort ());
                        playerDevList[i].setIndex (is.readShort ());
                        playerDevList[i].setPort  (is.readShort ());
                    }
                    pddt.setDevList (playerDevList);
                    /* ignore the rest of the payload - if any */
                    for (int j = 0; j < size - (4 + pddt.getDeviceCount () * 6); j++)
                        is.readByte ();
                    
                    readyPDDT = true;
                    break;
                }
                
                /* Configuration request: Get the driver name for a particular device. */
                case PLAYER_PLAYER_DRIVERINFO_REQ: {	/* get driver name */
                    pddi             = new PlayerDeviceDriverInfo();
                    PlayerDeviceIdT pddiDevID = new PlayerDeviceIdT ();
                    
                    pddiDevID.setCode  (is.readShort ());
                    pddiDevID.setIndex (is.readShort ());
                    pddiDevID.setPort  (is.readShort ());
                    
                    pddi.setDevID (pddiDevID);
                    
                    String pddiDriverName  = new String ();
	    	    	
                    for (int j = 0; j < size - 8; j++)
                        pddiDriverName += is.readChar ();
                    
                    pddi.setDriverName(pddiDriverName);
                    
                    readyPDDI        = true;
                    break;
                }
                case PLAYER_PLAYER_DEV_REQ: {             /* request device access */
                    break;
                }
                case PLAYER_PLAYER_DATA_REQ: {            /* request data */
                    break;
                }
                case PLAYER_PLAYER_DATAMODE_REQ: {        /* change data delivery mode */
                    break;
                }
                case PLAYER_PLAYER_DATAFREQ_REQ: {        /* change data delivery frequency */
                    break;
                }
                case PLAYER_PLAYER_AUTH_REQ: {            /* authentication */
                    receivedAuthentication = true;
                    break;
                }
                case PLAYER_PLAYER_NAMESERVICE_REQ: {
                    portNumber = is.readShort ();
                    readyPortNumber = true;
                    break;
                }
            }
        } catch (Exception e) {}
    }
    
    /**
     * Get the list of available devices after a PLAYER_PLAYER_DEVLIST_REQ request.
     * @return an object of PlayerDeviceDevlistT type
     * @see #isReadyPDDT()
     */
    public PlayerDeviceDevlistT   getPDDT () { return pddt; }
    /**
     * Get the driver name for a particular device after a PLAYER_PLAYER_DRIVERINFO_REQ request.
     * @return an object of PlayerDeviceDriverInfo type
     * @see #isReadyPDDI()
     */
    public PlayerDeviceDriverInfo getPDDI () { return pddi; }
    /**
     * Get the port number for the specified robot after a PLAYER_PLAYER_NAMESERVICE_REQ request.
     * @return the port number the specified robot runs on
     * @see #requestNameService(char[])
     * @see #isReadyPortNumber()
     */
    public int getPortNumber () { return portNumber; }

    /**
     * Check to see if the Player server replied with a PLAYER_PLAYER_DEVLIST_REQ successfully.
     * @return true if the PLAYER_PLAYER_DEVLIST_REQ occured, false otherwise
     * @see #getPDDT()
     */
    public boolean isReadyPDDT () {
        if (readyPDDT) {
            readyPDDT = false;
            return true;
        }
        return false;
    }
    /**
     * Check to see if the Player server replied with a PLAYER_PLAYER_DRIVERINFO_REQ successfully.
     * @return true if the PLAYER_PLAYER_DRIVERINFO_REQ occured, false otherwise
     * @see #getPDDI()
     */
    public boolean isReadyPDDI () {
        if (readyPDDI) {
            readyPDDI = false;
            return true;
        }
        return false;
    }
    /**
     * Check to see if the client has authenticated successfully.
     * @return true if client has authenticated, false otherwise
     */
    public boolean isAuthenticated () { 
        if (receivedAuthentication) {
            receivedAuthentication = false;
            return true;
        }
        return false;
    }
    /**
     * Check to see if the port number has been identified. 
     * @return true if the port is ready to be read, false otherwise
     * @see #getPortNumber()
     */
    public boolean isReadyPortNumber () {
        if (readyPortNumber) {
            readyPortNumber = false;
            return true;
        }
        return false; 
    }
}
