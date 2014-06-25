/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jitsi.hammer.neomedia.jmfext.media.protocol.greyfading;

import java.awt.Dimension;

import javax.media.CaptureDeviceInfo;
import javax.media.Format;
import javax.media.format.RGBFormat;
import javax.media.format.VideoFormat;
import javax.media.protocol.*;

import org.jitsi.impl.neomedia.codec.FFmpeg;
import org.jitsi.impl.neomedia.codec.video.AVFrameFormat;
import org.jitsi.impl.neomedia.device.*;
import org.jitsi.service.neomedia.*;


/**
 * Implements a <tt>MediaDevice</tt> which provides a fading animation from
 * white to black to white... in form of video.
 *
 * @author Thomas Kuntz
 */
public class VideoGreyFadingMediaDevice
    extends MediaDeviceImpl
{

    public final static float FRAMERATE = 24;
    
    
    /**
     * The list of <tt>Format</tt>s supported by the
     * <tt>VideoGreyFadingCaptureDevice</tt> instances.
     */
    public static final Format[] SUPPORTED_FORMATS
        = new Format[]
        		{
                new RGBFormat(
                	 new Dimension(640,480), // size
                     Format.NOT_SPECIFIED, // maxDataLength
                     Format.byteArray, // dataType
                     FRAMERATE, // frameRate
                     32, // bitsPerPixel
                     2 /* red */,
                     3 /* green */,
                     4 /* blue */)
        		};
    
 
    public VideoGreyFadingMediaDevice()
    {
        super(new CaptureDeviceInfo(
                    "GreyFadingVideo",
                    null,
                    VideoGreyFadingMediaDevice.SUPPORTED_FORMATS),
                MediaType.VIDEO);
    }

    
    /**
     * {@inheritDoc}
     *
     * Overrides the super implementation to initialize a <tt>CaptureDevice</tt>
     * without asking FMJ to initialize one for a <tt>CaptureDeviceInfo</tt>.
     */
    @Override
    protected CaptureDevice createCaptureDevice()
    {
        return new DataSource();
    }

    
    /**
     * {@inheritDoc}
     *
     * Overrides the super implementation to always return
     * {@link MediaDirection#SENDRECV} because this instance stands for a relay
     * and because the super bases the <tt>MediaDirection</tt> on the
     * <tt>CaptureDeviceInfo</tt> which this instance does not have.
     */
    @Override
    public MediaDirection getDirection()
    {
        return MediaDirection.SENDRECV;
    }
}
