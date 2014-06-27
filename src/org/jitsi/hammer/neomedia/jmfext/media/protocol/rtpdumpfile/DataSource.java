/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jitsi.hammer.neomedia.jmfext.media.protocol.rtpdumpfile;


import javax.media.control.*;

import org.jitsi.impl.neomedia.jmfext.media.protocol.*;

/**
 * Implements <tt>CaptureDevice</tt> and <tt>DataSource</tt> for the purposes of
 * rtpdump file streaming.
 * 
 * 
 * @author Thomas Kuntz
 */
public class DataSource
    extends AbstractVideoPullBufferCaptureDevice
{
    //new VideoFormat(Constants.VP8_RTP)
    
    /**
     * {@inheritDoc}
     *
     * Implements
     * {@link AbstractPushBufferCaptureDevice#createStream(int, FormatControl)}.
     */
    protected RtpdumpStream createStream(
            int streamIndex,
            FormatControl formatControl)
    {
        return new RtpdumpStream(this, formatControl);
    }
}
