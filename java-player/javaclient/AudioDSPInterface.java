/*
 *  Player Java Client - AudioDSPInterface.java
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
 * $Id: AudioDSPInterface.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient;

/**
 * The audiodsp interface is used to control sound hardware, if equipped.
 * @author Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 * </ul>
 */
public class AudioDSPInterface extends PlayerDevice {

    private static final boolean isDebugging = PlayerClient.isDebugging;
    
    /* audio dsp I/O */
    private final short PLAYER_AUDIODSP_CODE = PlayerClient.PLAYER_AUDIODSP_CODE;

    /* the player message types (see player.h) */
    private static final short PLAYER_MSGTYPE_CMD = PlayerClient.PLAYER_MSGTYPE_CMD;
    private static final short PLAYER_MSGTYPE_REQ = PlayerClient.PLAYER_MSGTYPE_REQ;

    /* string to match the currently assigned devices */
    private static final short PLAYER_MAX_DEVICE_STRING_LEN = 
        PlayerClient.PLAYER_MAX_DEVICE_STRING_LEN;
    
    /* configuration subtypes */
    public static final short PLAYER_AUDIODSP_SET_CONFIG = 1;
    public static final short PLAYER_AUDIODSP_GET_CONFIG = 2;
    public static final short PLAYER_AUDIODSP_PLAY_TONE  = 3;
    public static final short PLAYER_AUDIODSP_PLAY_CHIRP = 4;
    public static final short PLAYER_AUDIODSP_REPLAY     = 5;

    private int frequency[] = new int[5];
    private int amplitude[] = new int[5];

    private int  sampleFormat;  /* format with which to sample */ 
    private int  sampleRate;    /* sample rate in Hertz */
    private byte channels;      /* number of channels to use. 1=mono, 2=stereo */
    
    /**
     * Constructor for AudioDSPInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public AudioDSPInterface (PlayerClient pc, short indexOfDevice) {
        super(pc);
        device    = PLAYER_AUDIODSP_CODE;
        index     = indexOfDevice;
    }
    
    /**
     * The audiodsp interface reads the audio stream from /dev/dsp (which is assumed 
     * to be associated with a sound card connected to a microphone) and performs some 
     * analysis on it. Five frequency/amplitude pairs are then returned as data.
     */
    public synchronized void readData () {
        readHeader ();
        try {
            for (int i = 0; i < 5; i++)
                frequency[i] = is.readUnsignedShort ();     /* Hz */
            for (int i = 0; i < 5; i++)
                amplitude[i] = is.readUnsignedShort ();     /* dB */
        } catch (Exception e) {
            System.err.println ("[AudioDSP] : Error when reading payload: " + e.toString ());
        }
    }

    /**
     * Returns the five highest frequencies.
     * @return an array filled with the highest five frequencies
     */
    public synchronized int[] getFrequencies () { return frequency; }
    
    /**
     * Returns the five highest amplitudes.
     * @return an array filled with the highest five amplitudes
     */
    public synchronized int[] getAmplitudes  () { return amplitude; }

    /**
     * The audiodsp interface accepts commands to produce fixed-frequency tones 
     * or binary phase shift keyed(BPSK) chirps through /dev/dsp (which is assumed 
     * to be associated with a sound card to which a speaker is attached).
     * @param subtype The packet subtype. Set to PLAYER_AUDIODSP_PLAY_TONE to play 
     * a single frequency; bitString and bitStringLen do not need to be set. Set to 
     * PLAYER_AUDIODSP_PLAY_CHIRP to play a BPSKeyed chirp; bitString should contain 
     * the binary string to encode, and bitStringLen set to the length of the 
     * bitString. Set to PLAYER_AUDIODSP_REPLAY to replay the last sound.
     * @param freq Frequency to play (Hz)
     * @param amp Amplitude to play (dB?)
     * @param duration Duration to play (msec)
     * @param bitString BitString to encode in sine wave
     * @param bitStringLen Length of the bit string 
     */
    public void command (byte subtype, int freq, int amp, 
                         int duration, char[] bitString, int bitStringLen) {
        try {
            int size = 20 + PLAYER_MAX_DEVICE_STRING_LEN;
            sendHeader (PLAYER_MSGTYPE_CMD, size);  /* payload */
            os.writeInt (subtype);                  /* the packet subtype */
            os.writeInt (freq);                     /* frequency to play */ 
            os.writeInt (amp);                      /* amplitude to play */ 
            os.writeInt (duration);                 /* duration to play */
            for (int i = 0; i < PLAYER_MAX_DEVICE_STRING_LEN; i++)
                os.writeChar (bitString[i]);        /* sine wave to play */
            os.writeInt (bitStringLen);             /* length of the bit string */ 
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[AudioDSP] : Couldn't send new command: " +
                    e.toString ());
        }
    }
    
    /**
     * Configuration request : Get audio properties.
     * <br><br>
     * The audiodsp configuration can be queried using the PLAYER_AUDIODSP_GET_CONFIG 
     * request and modified using the PLAYER_AUDIODSP_SET_CONFIG request.<br /><br />
     * The sample format is defined in sys/soundcard.h, and defines the byte size and 
     * endian format for each sample.<br /><br />
     * The sample rate defines the Hertz at which to sample.<br /><br />
     * Mono or stereo sampling is defined in the channels parameter where 1==mono and 
     * 2==stereo.<br /><br />
     * See the player_audiodsp_config structure from player.h
     */
    public void getAudioProperties () {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 1);     /* 1 byte payload */
            os.writeByte (PLAYER_AUDIODSP_GET_CONFIG);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[AudioDSP] : Couldn't send PLAYER_AUDIODSP_GET_CONFIG " +
                    "command: " + e.toString ());
        }
    }

    /**
     * Configuration request : Set audio properties.
     * <br><br>
     * The audiodsp configuration can be queried using the PLAYER_AUDIODSP_GET_CONFIG 
     * request and modified using the PLAYER_AUDIODSP_SET_CONFIG request.<br /><br />
     * The sample format is defined in sys/soundcard.h, and defines the byte size and 
     * endian format for each sample.<br /><br />
     * The sample rate defines the Hertz at which to sample.<br /><br />
     * Mono or stereo sampling is defined in the channels parameter where 1==mono and 
     * 2==stereo.<br /><br />
     * See the player_audiodsp_config structure from player.h
     * @param sampleFormat Format with which to sample
     * @param sampleRate Sample rate in Hertz
     * @param channels Number of channels to use. 1=mono, 2=stereo
     */
    public void setAudioProperties (int sampleFormat, int sampleRate, byte channels) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 8);     /* 8 byte payload */
            os.writeByte  (PLAYER_AUDIODSP_SET_CONFIG);
            os.writeInt   (sampleFormat);           /* format with which to sample */ 
            os.writeShort (sampleRate);             /* Sample rate in Hertz */
            os.writeByte  (channels);               /* nr of channels to use, 1=mono, 2=stereo */
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[AudioDSP] : Couldn't send PLAYER_AUDIODSP_SET_CONFIG " +
                    "command: " + e.toString ());
        }
    }
    
    /**
     * Handle acknowledgement response messages (threaded mode).
     * @param size size of the payload
     */
    public void handleResponse (int size) {
        if (size == 0) {
            if (isDebugging)
            	System.err.println ("[AudioDSP][Debug] : Unexpected response of size 0!");
            return;
        }
        try {
            /* each reply begins with a uint8_t subtype field */
            byte subtype = is.readByte ();
            switch (subtype) {
                case PLAYER_AUDIODSP_GET_CONFIG: {
                    sampleFormat = is.readInt ();                 /* format with which to sample */
                    sampleRate   = is.readUnsignedShort ();       /* sample rate in Hertz */
                    channels     = (byte)is.readUnsignedByte ();  /* number of channels to use */
                    break;
                }
                case PLAYER_AUDIODSP_SET_CONFIG: {
                    break;
                }
                default:{
                    System.err.println ("[AudioDSP] : Unexpected response " + subtype + 
                            " of size = " + size);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println ("[AudioDSP] : Error when reading payload " + e.toString ());
        }
    }

    /**
     * Returns the format with which to sample.
     * @return the format with which to sample as an integer
     */
    public synchronized int getSampleFormat () { return sampleFormat; }

    /**
     * Returns the sample rate in Hertz.
     * @return the sample rate in Hertz as an integer
     */
    public synchronized int getSampleRate () { return sampleRate; }

    /**
     * Returns the number of channels to use (1=mono, 2=stereo).
     * @return the number of channels to use as a byte
     */
    public synchronized byte getChannels () { return channels; }
}
