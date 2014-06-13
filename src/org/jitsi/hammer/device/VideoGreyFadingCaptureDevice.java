/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jitsi.hammer.device;

import java.io.*;
import java.util.*;

import javax.media.*;
import javax.media.control.*;
import javax.media.format.*;
import javax.media.protocol.*;

import org.jitsi.impl.neomedia.codec.*;
import org.jitsi.impl.neomedia.jmfext.media.protocol.*;

/**
 * Implements a <tt>CaptureDevice</tt> which provides a fading animation from
 * white to black to white... in form of video.
 *
 * @author Thomas Kuntz
 */
public class VideoGreyFadingCaptureDevice
    extends AbstractVideoPullBufferCaptureDevice
{
    /**
     * {@inheritDoc}
     *
     * Implements
     * {@link AbstractPushBufferCaptureDevice#createStream(int, FormatControl)}.
     */
    protected VideoGreyFadingStream createStream(
            int streamIndex,
            FormatControl formatControl)
    {
        return new VideoGreyFadingStream(this, formatControl);
    }

    /**
     * {@inheritDoc}
     *
     * Overrides the super implementation in order to return the list of
     * <tt>Format</tt>s hardcoded as supported in
     * <tt>VideoGreyFadingCaptureDevice</tt> because the super looks them up by
     * <tt>CaptureDeviceInfo</tt> and this instance does not have one.
     */
    @Override
    protected Format[] getSupportedFormats(int streamIndex)
    {
        return VideoGreyFadingMediaDevice.SUPPORTED_FORMATS.clone();
    }
}
