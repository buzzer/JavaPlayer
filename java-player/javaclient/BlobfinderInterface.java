/*
 *  Player Java Client - BlobfinderInterface.java
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
 * $Id: BlobfinderInterface.java 22 2005-06-14 09:41:16Z veedee $
 *
 */
package javaclient;

import javaclient.structures.Blob;

/**
 * The blobfinder interface provides access to devices that detect blobs in images.
 * @author Maxim A. Batalin & Radu Bogdan Rusu
 * @version
 * <ul>
 *      <li>v1.6.3 - Player 1.6.3 (all interfaces) supported
 *      <li>v1.6.2 - Player 1.6.2 supported, Javadoc documentation, several bugfixes  
 *      <li>v1.5a &nbsp;- Player 1.5 supported (most popular devices)
 * </ul>
 */
public class BlobfinderInterface extends PlayerDevice {

    private static final boolean isDebugging = PlayerClient.isDebugging;
        
    /* visual blobfinder */
    private final short PLAYER_BLOBFINDER_CODE = PlayerClient.PLAYER_BLOBFINDER_CODE;
    
    /** the maximum number of blobs in total */
    public final short PLAYER_BLOBFINDER_MAX_BLOBS = 256; 
    
    /* the player message types (see player.h) */
    private static final short PLAYER_MSGTYPE_REQ = PlayerClient.PLAYER_MSGTYPE_REQ;
    
    /* config request codes */
    private final short PLAYER_BLOBFINDER_SET_COLOR_REQ         = 1;
    private final short PLAYER_BLOBFINDER_SET_IMAGER_PARAMS_REQ = 2;
    
    /* the image dimensions */
    private short imageWidth;
    private short imageHeight;
    
    private short blobCount = 0;                                            /* number of blobs */
    private Blob[] blobList = new Blob[PLAYER_BLOBFINDER_MAX_BLOBS];    /* list of blobs */
    
    /**
     * Constructor for BlobfinderInterface.
     * @param pc a reference to the PlayerClient object
     * @param indexOfDevice the index of the device
     */
    public BlobfinderInterface (PlayerClient pc, short indexOfDevice) {
        super(pc);
        device    = PLAYER_BLOBFINDER_CODE;
        index     = indexOfDevice;
    }
    
    /**
     * Read the list of detected blobs.
     */
    public synchronized void readData () {
        readHeader ();
        try {
            imageWidth  = is.readShort ();
            imageHeight = is.readShort ();
            blobCount   = is.readShort ();
            for (int i = 0; i < blobCount; i++) {
                blobList[i]        = new Blob     ();
                blobList[i].setID     (is.readShort ());       /* blob ID */
                blobList[i].setColor  (is.readInt   ());       /* 32-bit RGB color blob */
                blobList[i].setArea   (is.readInt   ());       /* the blob area (pixels) */
                blobList[i].setX      (is.readShort ());       /* the blob centroid */
                blobList[i].setY      (is.readShort ());
                blobList[i].setLeft   (is.readShort ());       /* bounding box for the blob */
                blobList[i].setRight  (is.readShort ());
                blobList[i].setTop    (is.readShort ());
                blobList[i].setBottom (is.readShort ());
                blobList[i].setRange  (is.readShort ());       /* range to the blob center */
            }
        } catch (Exception e) {
            System.err.println ("[Blobfinder] : Error when reading payload: " + e.toString ());
        }
    }
    
    /**
     * Configuration request: Set tracking color.
     * <br><br>
     * For some sensors (ie CMUcam), simple blob tracking tracks only one color. To set the 
     * tracking color, send a request with the format below, including the RGB color ranges 
     * (max and min). Values of -1 will cause the track color to be automatically set to the 
     * current window color. This is useful for setting the track color by holding the tracking 
     * object in front of the lens.
     * @param rmin Red minimum value (0-255)
     * @param rmax Red maximum value (0-255)
     * @param gmin Green minimum value (0-255)
     * @param gmax Green maximum value (0-255)
     * @param bmin Blue minimum value (0-255)
     * @param bmax Blue maximum value (0-255)
     */
    public void setTrackingColor (int rmin, int rmax,
                                  int gmin, int  gmax, 
                                  int bmin, int bmax) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 13);      /* 13 bytes payload */
            os.writeByte (PLAYER_BLOBFINDER_SET_COLOR_REQ);
            os.writeShort ((short)rmin);              /* RGB minimum and max values (0-255) */
            os.writeShort ((short)rmax);
            os.writeShort ((short)gmin);
            os.writeShort ((short)gmax);
            os.writeShort ((short)bmin);
            os.writeShort ((short)bmax);
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Blobfinder] : Couldn't send PLAYER_BLOBFINDER_SET_COLOR_REQ " +
                    "command: " + e.toString ());
        }
    }
    
    /**
     * Configuration request: Set imager params.
     * <br><br>
     * Imaging sensors that do blob tracking generally have some sorts of image quality 
     * parameters that you can tweak. The following ones are implemented here:
     * <ul>
     *          <li>brightness (0-255)
     *          <li>contrast (0-255)
     *          <li>auto gain (0=off, 1=on)
     *          <li>color mode (0=RGB/AutoWhiteBalance Off, 1=RGB/AutoWhiteBalance On, 
     *              2=YCrCB/AWB Off, 3=YCrCb/AWB On) To set the params, send a request with the 
     *              format below. Any values set to -1 will be left unchanged.
     * </ul>
     * @param brightness brightness value (0-255)
     * @param contrast contrast value (0-255)
     * @param colormode color mode (0=RGB/AWB off, 1=RGB/AWB on, 2=YCrCB/AWB off, 3=YCrCb/AWB on)
     * @param autogain auto gain (0=off, 1=on)
     */
    public void setImagerParams (int brightness, int contrast,
                                 int colormode, int autogain) {
        try {
            sendHeader (PLAYER_MSGTYPE_REQ, 7);       /* 7 bytes payload */
            os.writeByte  (PLAYER_BLOBFINDER_SET_IMAGER_PARAMS_REQ);
            os.writeShort ((short)brightness);        /* contrast: (0-255) -1=no change */
            os.writeShort ((short)contrast);          /* brightness: (0-255) -1=no change */
            os.writeByte  ((byte)colormode);          /* color mode */
            os.writeByte  ((byte)autogain);           /* autoGain: 0=off, 1=on. -1=no change */
            os.flush ();
        } catch (Exception e) {
            System.err.println ("[Blobfinder] : Couldn't " +
                    " send PLAYER_BLOBFINDER_SET_IMAGER_PARAMS_REQ command: " + e.toString ());
        }
    }
    
    /**
     * Get image width in pixels.
     * @return image width in pixels as a short
     */
    public synchronized short  getImageWidth  ()      { return imageWidth;  }
    
    /**
     * Get image height in pixels.
     * @return image height in pixels as a short
     */
    public synchronized short  getImageHeight ()      { return imageHeight; }

    /**
     * Get the number of valid blobs.
     * @return number of valid blobs as a short
     */
    public synchronized short  getBlobCount   ()      { return blobCount;   }

    /**
     * Get all the blobs.
     * @return an array of Blob objects filled with data
     */
    public synchronized Blob[] getBlobs       ()      { return blobList;    }

    /**
     * Get a specified blob.
     * @param i the number of blob from the blob array
     * @return the specified Blob object
     */
    public synchronized Blob   getBlob        (int i) { return blobList[i]; }
    
    /**
     * Handle acknowledgement response messages (threaded mode).
     * @param size size of the payload
     */
    public void handleResponse (int size) {
        if (size == 0) {
            if (isDebugging)
                System.err.println ("[Blobfinder][Debug] : Unexpected response of size 0!");
            return;
        }
        try {
            /* each reply begins with a uint8_t subtype field */
            byte subtype = is.readByte ();
            switch (subtype) {
                case PLAYER_BLOBFINDER_SET_COLOR_REQ: {
                    break;
                }
                case PLAYER_BLOBFINDER_SET_IMAGER_PARAMS_REQ: {
                    break;
                }
                default:{
                    System.err.println ("[Blobfinder] : Unexpected response " + subtype + 
                            " of size = " + size);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println ("[Blobfinder] : Error when reading payload " + e.toString ());
        }
    }

}

