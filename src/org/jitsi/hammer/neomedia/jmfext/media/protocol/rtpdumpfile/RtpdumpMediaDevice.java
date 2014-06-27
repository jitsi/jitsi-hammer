/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jitsi.hammer.neomedia.jmfext.media.protocol.rtpdumpfile;

import java.io.IOException;

import javax.media.*;
import javax.media.format.*;
import javax.media.protocol.*;

import org.jitsi.impl.neomedia.device.*;
import org.jitsi.impl.neomedia.jmfext.media.protocol.AbstractPullBufferCaptureDevice;
import org.jitsi.service.neomedia.*;


/**
 * Implements a <tt>MediaDevice</tt> which provides a fading animation from
 * white to black to white... in form of video.
 *
 * @author Thomas Kuntz
 */
public class RtpdumpMediaDevice
    extends MediaDeviceImpl
{
    
    public RtpdumpMediaDevice(String filename, String payloadTypeConstant)
    {
        super(new CaptureDeviceInfo(
                    filename,
                    new MediaLocator("rtpdumpfile:" + filename),
                    new Format[] { new VideoFormat(payloadTypeConstant) } ),
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
        DataSource captureDevice = new DataSource();
        captureDevice.setLocator(getCaptureDeviceInfo().getLocator());
        try
        {
            captureDevice.connect();
        }
        catch (IOException e)
        {
            e.printStackTrace(); //TODO what can I do if an exception is raised?
        }
        
        if (captureDevice instanceof AbstractPullBufferCaptureDevice)
        {
            ((AbstractPullBufferCaptureDevice) captureDevice)
                .setCaptureDeviceInfo(getCaptureDeviceInfo());
        }
        return captureDevice;
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
