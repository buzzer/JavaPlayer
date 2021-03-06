/*
 *  Player Java Client - MCommInterface.java
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
 * $Id: MComInterface.java 14 2005-05-17 08:59:35Z veedee $
 *
 */
package javaclient;

/**
 * The mcom interface is designed for exchanging information between clients. A client 
 * sends a message of a given "type" and "channel". This device stores adds the message 
 * to that channel's stack. A second client can then request data of a given "type" and 
 * "channel". Push, Pop, Read, and Clear operations are defined, but their semantics can 
 * vary, based on the stack discipline of the underlying driver. For example, the
 * lifomcom driver enforces a last-in-first-out stack.<br /><br />  
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 * </ul>
 */
public class MComInterface extends PlayerDevice {

    private static final boolean isDebugging = PlayerClient.isDebugging;

    private final short PLAYER_MCOM_CODE = PlayerClient.PLAYER_MCOM_CODE; /* multicoms */

    /* the player message types (see player.h) */
    private static final short PLAYER_MSGTYPE_REQ = PlayerClient.PLAYER_MSGTYPE_REQ;

    /** size of the data field in messages */
    public static final short MCOM_DATA_LEN      = 128;

    /** number of buffers to keep per channel */
    public static final short MCOM_N_BUFS        = 10;
    
    /** size of channel name */
    public static final short MCOM_CHANNEL_LEN   = 8;
    
    /** returns this if empty */
    public static final String MCOM_EMPTY_STRING = "(EMPTY)";
    
    protected static final short MCOM_COMMAND_BUFFER_SIZE = 111;
    protected static final short MCOM_DATA_BUFFER_SIZE    = 0;
    
    /* request ids */
    public static final short PLAYER_MCOM_PUSH_REQ         = 0;
    public static final short PLAYER_MCOM_POP_REQ          = 1;
    public static final short PLAYER_MCOM_READ_REQ         = 2;
    public static final short PLAYER_MCOM_CLEAR_REQ        = 3;
    public static final short PLAYER_MCOM_SET_CAPACITY_REQ = 4;

    private char   full;                            /* a flag */
    private char[] data = new char[MCOM_DATA_LEN];  /* the data */
    
    private short   dataType;                       /* the "type" of the data */
    private char[]  channelName = new char[MCOM_CHANNEL_LEN];    /* the name of the channel */
    private char    fullF;
    private char[]  dataF = new char[MCOM_DATA_LEN];
    private boolean readyData = false;

    /**
     * Constructor for MComInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public MComInterface (PlayerClient pc, short indexOfDevice) {
        super(pc);
        device    = PLAYER_MCOM_CODE;
        index     = indexOfDevice;
    }
    
    /**
     * Read a piece of data.
     */
    public synchronized void readData () {
        readHeader ();
        try {
        	full = is.readChar ();                     /* a flag */
            for (int i = 0; i < MCOM_DATA_LEN; i++) {
            	data[i] = is.readChar ();              /* the data */
            }
        } catch (Exception e) {
            System.err.println ("[MCom] : Error when reading payload: " + e.toString ());
        }
    }
    
    /**
     * Returns the flag. 
     * @return the flag as a char
     */
    public synchronized char getFlag   () { return this.full; }

    /**
     * Returns the data. 
     * @return the data as a array of chars
     */
    public synchronized char[] getData () { return this.data; }
    
    /**
     * Configuration request: Config requests sent to server.
     * <br><br>
     * See the player_mcom_config structure from player.h
     * @param whichReq which request (should be one of the defined request ids)
     * @param type the "type" of the data 
     */
    public void sendConfigReq (int whichReq, int type, String channel, 
                               boolean fullT, char[] dataT) {
        try {
            int total = 4 + MCOM_CHANNEL_LEN + MCOM_DATA_LEN;
            sendHeader (PLAYER_MSGTYPE_REQ, total);     /* payload */
            os.writeByte  (whichReq);
            os.writeShort ((short)type);
            
            int size = channel.length ();
            if (size > MCOM_CHANNEL_LEN)
                size = MCOM_CHANNEL_LEN;
            for (int i = 0; i < size; i++)
                os.writeChar (channel.toCharArray ()[i]);
            if (size < MCOM_CHANNEL_LEN)
            	for (int i = 0; i < (MCOM_CHANNEL_LEN - size); i++)
                    os.writeChar (0);
            
            os.writeBoolean (fullT);
            for (int i = 0; i < MCOM_DATA_LEN; i++)
                os.writeChar (dataT[i]);
            
            os.flush ();
        } catch (Exception e) {
            String subtype = "";
            switch (type) {
                case PLAYER_MCOM_PUSH_REQ: {
                    subtype = "PLAYER_MCOM_PUSH_REQ";
                    break;
                }
                case PLAYER_MCOM_POP_REQ: {
                    subtype = "PLAYER_MCOM_POP_REQ";
                    break;
                }
                case PLAYER_MCOM_READ_REQ: {
                    subtype = "PLAYER_MCOM_READ_REQ";
                    break;
                }
                case PLAYER_MCOM_CLEAR_REQ: {
                    subtype = "PLAYER_MCOM_CLEAR_REQ";
                    break;
                }
                case PLAYER_MCOM_SET_CAPACITY_REQ: {
                    subtype = "PLAYER_MCOM_SET_CAPACITY_REQ";
                    break;
                }
                default: {
                    System.err.println ("[MCom] : Couldn't send " + subtype +
                            " command: " + e.toString ());
                }
            }
        }
    }

    /**
     * Configuration request: Push (PLAYER_MCOM_PUSH_REQ)
     */
    public void Push (int type, String channel, char[] dataT) {
        sendConfigReq (PLAYER_MCOM_PUSH_REQ, type, channel, true, dataT);
    }

    /**
     * Configuration request: Pop (PLAYER_MCOM_POP_REQ)
     */
    public void Pop (int type, String channel) {
        char[] dataT = new char[MCOM_DATA_LEN];
        sendConfigReq (PLAYER_MCOM_POP_REQ, type, channel, false, dataT);
    }
    
    /**
     * Configuration request: Read (PLAYER_MCOM_READ_REQ)
     */
    public void Read (int type, String channel) {
        char[] dataT = new char[MCOM_DATA_LEN];
        sendConfigReq (PLAYER_MCOM_READ_REQ, type, channel, false, dataT);
    }

    /**
     * Configuration request: Clear (PLAYER_MCOM_CLEAR_REQ)
     */
    public void Clear (int type, String channel) {
        char[] dataT = new char[MCOM_DATA_LEN];
        sendConfigReq (PLAYER_MCOM_CLEAR_REQ, type, channel, false, dataT);
    }

    /**
     * Configuration request: Set capacity (PLAYER_MCOM_SET_CAPACITY_REQ)
     */
    public void setCapacity (int type, String channel, char capacity) {
        char[] dataT = new char[MCOM_DATA_LEN];
        dataT[0] = capacity;
        sendConfigReq (PLAYER_MCOM_SET_CAPACITY_REQ, type, channel, false, dataT);
    }

    /**
     * Check if data is available.
     * @return true if ready, false if not ready 
     */
    public boolean isDataReady () {
        if (readyData) {
            readyData = false;
            return true;
        }
        return false;
    }

    /**
     * Handle acknowledgement response messages (threaded mode).
     * @param size size of the payload
     */
    public void handleResponse (int size) {
        if (size == 0) {
            if (isDebugging)
                System.err.println ("[MCom][Debug] : Unexpected response of size 0!");
            return;
        }
        try {
            /* each reply begins with a uint8_t subtype field */
            byte subtype = is.readByte ();
            switch (subtype) {
                case PLAYER_MCOM_PUSH_REQ: {
                	break;
                }
                case PLAYER_MCOM_POP_REQ: {
                    /* the "type" of data */
                    dataType = (short)is.readUnsignedShort ();
                    
                    for (int i = 0; i < MCOM_CHANNEL_LEN; i++)
                        channelName[i] = is.readChar ();    /* the name of the channel */
                    
                    fullF = is.readChar ();                 /* a flag (boolean 0/1) */
                    for (int i = 0; i < MCOM_DATA_LEN; i++)
                        dataF[i] = is.readChar ();          /* the data */
                    
                    readyData = true;
                    break;
                }
                case PLAYER_MCOM_READ_REQ: {
                    /* the "type" of data */
                    dataType = (short)is.readUnsignedShort ();
                    
                    for (int i = 0; i < MCOM_CHANNEL_LEN; i++)
                        channelName[i] = is.readChar ();    /* the name of the channel */
                    
                    fullF = is.readChar ();                 /* a flag (boolean 0/1) */
                    for (int i = 0; i < MCOM_DATA_LEN; i++)
                        dataF[i] = is.readChar ();          /* the data */
                    
                    readyData = true;
                    break;
                }
                case PLAYER_MCOM_CLEAR_REQ: {
                	break;
                }
                case PLAYER_MCOM_SET_CAPACITY_REQ: {
                	break;
                }
                default: {
                    System.err.println ("[MCom] : Unexpected response " + subtype + 
                            " of size = " + size);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println ("[MCom] : Error when reading payload " + e.toString ());
        }
    }
    
    /**
     * Returns the flag after a sendConfigReq () call. 
     * @return the flag as a char
     */
    public synchronized char getRFlag  () { return this.fullF; }

    /**
     * Returns the data after a sendConfigReq () call. 
     * @return the data as a array of chars
     */
    public synchronized char[] getRData () { return this.dataF; }

    /**
     * Returns the type of data after a sendConfigReq () call. 
     * @return the type of data as a short
     */
    public synchronized short getDataType     () { return this.dataType;    }
    
    /**
     * Returns the name of the channel after a sendConfigReq () call. 
     * @return the name of the channel as a array of chars
     */
    public synchronized char[] getChannelName () { return this.channelName; }
}
