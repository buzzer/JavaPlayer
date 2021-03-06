/*
 *  Player Java Client - GPSInterface.java
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
 * $Id: GPSInterface.java 10 2005-05-10 12:10:24Z veedee $
 *
 */
package javaclient;

/**
 * The gps interface provides access to an absolute position system, such as GPS.
 * This interface accepts no commands. 
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class GPSInterface extends PlayerDevice {

    private short PLAYER_GPS_CODE = PlayerClient.PLAYER_GPS_CODE; /* GPS unit */
    
    private int   timeSec   = 0; /* GPS (UTC) time in seconds since the epoch */
    private int   timeuSec  = 0; /* GPS (UTC) time in microseconds since the epoch */
    
    /* Latitude in degrees / 1e7 (units are scaled such that the effective resolution is 
     * roughly 1cm). Positive is north of equator, negative is south of equator.
     */
    private int   latitude  = 0;
    
    /* Longitude in degrees / 1e7 (units are scaled such that the effective resolution is 
     * roughly 1cm). Positive is east of prime meridian, negative is west of prime meridian.
     */
    private int   longitude = 0;
    
    /* Altitude, in millimeters. Positive is above reference (e.g. sea-level), and negative 
     * is below. 
     */
    private int   altitude  = 0;
    
    private int   utmE      = 0;    /* UTM WGS84 coordinates, easting (cm) */
    private int   utmN      = 0;    /* UTM WGS84 coordinates, northing (cm) */
    private byte  quality   = 0;    /* quality of fix 0 = invalid, 1 = GPS fix, 2 = DGPS fix */
    private byte  numSats   = 0;    /* number of satellites in view */
    private short hDop      = 0;    /* horizontal dilution of position (HDOP), times 10 */
    private short vDop      = 0;    /* vertical dilution of position (VDOP), times 10 */
    private int   errHorz   = 0;    /* horizontal error (mm) */
    private int   errVert   = 0;    /* vertical error (mm) */
    
    /**
     * Constructor for GPSInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public GPSInterface (PlayerClient pc, short indexOfDevice) {
        super (pc);
        device    = PLAYER_GPS_CODE;
        index     = indexOfDevice;
    }
    
    /**
     * Read the current global position and heading information.
     */
    public synchronized void readData () {
        try {
            readHeader ();
            timeSec   = is.readInt ();                  /* GPS (UTC) time, seconds */
            timeuSec  = is.readInt ();                  /* GPS (UTC) time, microseconds */
            latitude  = is.readInt ();                  /* latitude */
            longitude = is.readInt ();                  /* longitude */
            altitude  = is.readInt ();                  /* altitude */
            utmE      = is.readInt ();                  /* UTM WGS84 coordinates, East */
            utmN      = is.readInt ();                  /* UTM WGS84 coordinates, North */
            quality   = (byte)is.readUnsignedByte ();   /* quality */
            numSats   = (byte)is.readUnsignedByte ();   /* number of satellites in view */
            hDop      = (short)is.readUnsignedShort (); /* horizontal dilution of position */
            vDop      = (short)is.readUnsignedShort (); /* vertical dilution of position */
            errHorz   = is.readInt ();                  /* horizontal error */
            errVert   = is.readInt ();                  /* vertical error */
        } catch (Exception e) {
            System.err.println ("[GPS] : Error when reading payload: " + e.toString());
        }
    }
    
    /**
     * Get the GPS (UTC) time in seconds since the epoch.
     * @return the GPS (UTC) time in seconds since the epoch as an integer
     */
    public synchronized int  getTimeSec   () { return timeSec;   }
    
    /**
     * Get the GPS (UTC) time in microseconds since the epoch.
     * @return the GPS (UTC) time in microseconds since the epoch as an integer
     */
    public synchronized int  getTimeuSec  () { return timeuSec;  }
    
    /**
     * Get the latitude in degrees / 1e7.
     * @return the latitude in degrees / 1e7 as an integer
     */
    public synchronized int   getLatitude  () { return latitude;  }
    
    /**
     * Get the longitude in degrees / 1e7.
     * @return the longitude in degrees / 1e7 as an integer
     */
    public synchronized int   getLongitude () { return longitude; }
    
    /**
     * Get the altitude, in millimeters.
     * @return the altitude, in millimeters as an integer
     */
    public synchronized int   getAltitude  () { return altitude;  }
    
    /**
     * Get the UTM WGS84 coordinates, easting in cm.
     * @return the UTM WGS84 coordinates, easting in cm as an integer.
     */
    public synchronized int   getUtmE      () { return utmE;      }

    /**
     * Get the UTM WGS84 coordinates, northing in cm.
     * @return the UTM WGS84 coordinates, northing in cm as an integer.
     */
    public synchronized int   getUtmN      () { return utmN;      }
    
    /**
     * Get the quality of fix.
     * @return 0 = invalid, 1 = GPS fix, 2 = DGPS fix
     */
    public synchronized byte  getQuality   () { return quality;   }
    
    /**
     * Get the number of satellites in view.
     * @return the number of satellites in view as a byte.
     */
    public synchronized byte  getNumSats   () { return numSats;   }
    
    /**
     * Get the horizontal dilution of position (HDOP), times 10.
     * @return the horizontal dilution of position (HDOP), times 10 as a short
     */
    public synchronized short getHDop      () { return hDop;      }
    
    /**
     * Get the vertical dilution of position (VDOP), times 10.
     * @return the vertical dilution of position (VDOP), times 10 as a short
     */
    public synchronized short getVDop      () { return vDop;      }
    
    /**
     * Get the horizontal error in mm.
     * @return the horizontal error in mm as an integer
     */
    public synchronized int   getErrHorz   () { return errHorz;   }

    /**
     * Get the vertical error in mm.
     * @return the vertical error in mm as an integer
     */
    public synchronized int   getErrVert   () { return errVert;   }
}
