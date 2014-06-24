/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jitsi.hammer.device;

import javax.media.CaptureDeviceInfo;
import javax.media.Format;
import javax.media.format.VideoFormat;
import javax.media.protocol.*;

import org.jitsi.impl.neomedia.device.*;
import org.jitsi.service.neomedia.*;
import org.jitsi.service.neomedia.codec.Constants;


/**
 * Implements a <tt>MediaDevice</tt> which provides a fading animation from
 * white to black to white... in form of video.
 *
 * @author Thomas Kuntz
 */
public class IVFMediaDevice
    extends MediaDeviceImpl
{
    /**
     * The list of <tt>Format</tt>s supported by the
     * <tt>IVFCaptureDevice</tt> instances.
     */
    public static final Format[] SUPPORTED_FORMATS
        = new Format[]
                {
                    new VideoFormat(Constants.VP8)
                };
    
    private String filename;
 
    public IVFMediaDevice(String filename)
    {
        super(new CaptureDeviceInfo(
                    "IVF_file",
                    null,
                    IVFMediaDevice.SUPPORTED_FORMATS),
                MediaType.VIDEO);
        this.filename = filename;
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
        return new IVFCaptureDevice(filename);
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
