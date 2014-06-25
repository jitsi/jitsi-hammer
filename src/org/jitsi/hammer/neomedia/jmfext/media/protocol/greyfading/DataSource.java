/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jitsi.hammer.neomedia.jmfext.media.protocol.greyfading;

import java.awt.Dimension;
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
public class DataSource
    extends AbstractVideoPullBufferCaptureDevice
{
    protected final static float FRAMERATE = 25;
    
    
    /**
     * The list of <tt>Format</tt>s supported by the
     * <tt>DataSource</tt> instances of <tt>VideoGreyFadingStream</tt>.
     */
    protected static final Format[] SUPPORTED_FORMATS
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
     * <tt>DataSource</tt> because the super looks them up by
     * <tt>CaptureDeviceInfo</tt> and this instance does not have one. FIXME
     * 
     * For now it doesn't have a CaptureDeviceInfo, but it will have one soon
     */
    @Override
    protected Format[] getSupportedFormats(int streamIndex)
    {
        return SUPPORTED_FORMATS.clone();
    }
}
