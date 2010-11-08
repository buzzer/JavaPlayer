/*
 *  Player Java Client - PlayerWiFiLinkT.java
 *  Copyright (C) 2003-2005 Maxim A. Batalin & Radu Bogdan Rusu
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
 * $Id: PlayerWiFiLinkT.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient.structures;

/**
 * The wifi interface returns data regarding the signal characteristics of remote hosts as 
 * perceived through a wireless network interface; this is the format of the data for each host. <br />
 * (see the player_wifi_link structure from player.h)
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class PlayerWiFiLinkT {
    private char[] mac   = new char[32];            /* MAC address */
    private char[] ip    = new char[32];            /* IP address */
    private char[] essid = new char[32];            /* ESSID */
    private byte mode;                              /* mode (master, adhoc, etc) */
    private short frequency;                        /* frequency (MHz) */
    private byte encrypt;                           /* encrypted */
    private short qual;                             /* link quality, level and noise information */
    private short level;
    private short noise;
    
    /**
     * 
     * @return the MAC address 
     */
    public synchronized char[] getMAC () {
    	return this.mac;
    }
    
    /**
     * 
     * @param newmac MAC address 
     */
    public synchronized void setMAC (char[] newmac) {
    	this.mac = newmac;
    }

    /**
     * 
     * @return the IP address 
     */
    public synchronized char[] getIP () {
        return this.ip;
    }
    
    /**
     * 
     * @param newip IP address 
     */
    public synchronized void setIP (char[] newip) {
        this.ip = newip;
    }
    
    /**
     * 
     * @return the ESSID
     */
    public synchronized char[] getESSID () {
        return this.essid;
    }
    
    /**
     * 
     * @param newessid ESSID
     */
    public synchronized void setESSID (char[] newessid) {
        this.essid = newessid;
    }

    /**
     * 
     * @return the mode (master, adhoc, etc)
     */
    public synchronized byte getMode () {
        return this.mode;
    }
    
    /**
     * 
     * @param newmode mode (master, adhoc, etc)
     */
    public synchronized void setMode (byte newmode) {
        this.mode = newmode;
    }
    
    /**
     * 
     * @return the frequency (MHz)
     */
    public synchronized short getFrequency () {
        return this.frequency;
    }
    
    /**
     * 
     * @param newfrequency frequency (MHz)
     */
    public synchronized void setFrequency (short newfrequency) {
        this.frequency = newfrequency;
    }
    
    /**
     * 
     * @return the encryption status
     */
    public synchronized byte getEncrypt () {
        return this.encrypt;
    }
    
    /**
     * 
     * @param newencrypt encription value 
     */
    public synchronized void setEncrypt (byte newencrypt) {
        this.encrypt = newencrypt;
    }

    /**
     * 
     * @return the link quality
     */
    public synchronized short getQual () {
        return this.qual;
    }
    
    /**
     * 
     * @param newqual link quality
     */
    public synchronized void setQual (short newqual) {
        this.qual = newqual;
    }
    
    /**
     * 
     * @return the level
     */
    public synchronized short getLevel () {
        return this.level;
    }

    /**
     * 
     * @param newlevel level
     */
    public synchronized void setLevel (short newlevel) {
        this.level = newlevel;
    }

    /**
     * 
     * @return the noise information
     */
    public synchronized short getNoise () {
        return this.noise;
    }
    
    /**
     * 
     * @param newnoise noise information
     */
    public synchronized void setNoise (short newnoise) {
        this.noise = newnoise;
    }
}