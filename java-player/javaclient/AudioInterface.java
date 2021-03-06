/*
 *  Player Java Client - AudioInterface.java
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
 * $Id: AudioInterface.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient;

/**
 * The audio interface is used to control sound hardware, if equipped.
 * @author Maxim A. Batalin, Esben H. Ostergaard & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class AudioInterface extends PlayerDevice {

    private final short PLAYER_AUDIO_CODE = PlayerClient.PLAYER_AUDIO_CODE; /* audio I/O */
    
    /* the player message types (see player.h) */
    private final short PLAYER_MSGTYPE_CMD = PlayerClient.PLAYER_MSGTYPE_CMD;
    
    private int frequency[] = new int[5];
    private int amplitude[] = new int[5];
    /**
     * Constructor for AudioInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public AudioInterface (PlayerClient pc, short indexOfDevice) {
        super (pc);
        device = PLAYER_AUDIO_CODE;
        index  = indexOfDevice;
        
        for (int i = 0; i < 5; i++) {
        	frequency[i] = 0;
            amplitude[i] = 0;
        }
    }
    
    /**
     * The audio interface reads the audio stream from /dev/audio (which is assumed to be 
     * associated with a sound card connected to a microphone) and performs some analysis on 
     * it. Five frequency/amplitude pairs are then returned as data.
     */
    public synchronized void readData () {
        readHeader ();
        try {
            for (int i = 0; i < 5; i++) {
            	frequency[i] = is.readUnsignedShort ();
                amplitude[i] = is.readUnsignedShort ();
            }
        } catch (Exception e) {
            System.err.println ("[Audio] : Error when reading payload: " + e.toString ());
        }
    }
    
    /**
     * Returns the five highest frequencies.
     * @return an array filled with the highest five frequencies
     */
    public synchronized int[] getFiveHighestFrequencies () { return frequency; }
    
    /**
     * Returns the five highest amplitudes.
     * @return an array filled with the highest five amplitudes
     */
    public synchronized int[] getFiveHighestAmplitudes  () { return amplitude; }
    
    /**
     * The audio interface accepts commands to produce fixed-frequency tones through 
     * /dev/dsp (which is assumed to be associated with a sound card to which a speaker is 
     * attached).
     * @param freq frequency to play (Hz?) 
     * @param amp amplitude to play (dB?)
     * @param duration duration to play (sec?)
     */
    public void produceSound (short freq, short amp, short duration) {
        try {
            sendHeader (PLAYER_MSGTYPE_CMD, 6);       /* 6 bytes payload */
            os.writeShort (freq);                     /* freq frequency to play (Hz?) */
            os.writeShort (amp);                      /* amp amplitude to play (dB?) */
            os.writeShort (duration);                 /* duration duration to play (sec?) */
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Audio] : Couldn't send produce sound command request: " + 
                    e.toString ());
        }
    }
}
