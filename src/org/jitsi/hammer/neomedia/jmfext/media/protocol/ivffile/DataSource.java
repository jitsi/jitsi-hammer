/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jitsi.hammer.neomedia.jmfext.media.protocol.ivffile;

import java.io.IOException;

import javax.media.*;
import javax.media.control.*;
import javax.media.format.VideoFormat;

import org.jitsi.impl.neomedia.jmfext.media.protocol.*;
import org.jitsi.service.neomedia.codec.Constants;

/**
 * Implements <tt>CaptureDevice</tt> and <tt>DataSource</tt> for the purposes of
 * ivf (vp8 raw file, extracted from webm) file streaming
 *
 * @author Thomas Kuntz
 */
public class DataSource
    extends AbstractVideoPullBufferCaptureDevice
{
    private Format[] SUPPORTED_FORMATS = new Format[1];
    private String file;
    private IVFHeader ivfHeader;
    
    /**
     * doConnect allow us to initialize the DataSource with informations that
     * we couldn't have in the constructor, like the MediaLocator that give us
     * the path of the ivf file which give us information on the format 
     */
    public void doConnect() throws IOException
    {
        super.doConnect();
        this.file = getLocator().getRemainder();
        ivfHeader = new IVFHeader(this.file);
        
        this.SUPPORTED_FORMATS[0] = new VideoFormat(
                Constants.VP8,
                ivfHeader.getDimension(),
                Format.NOT_SPECIFIED,
                Format.byteArray,
                ivfHeader.getFramerate() / ivfHeader.getTimeScale());
    }
    
    /**
     * {@inheritDoc}
     *
     * Implements
     * {@link AbstractPushBufferCaptureDevice#createStream(int, FormatControl)}.
     */
    protected IVFStream createStream(
            int streamIndex,
            FormatControl formatControl)
    {
        return new IVFStream(this, formatControl);
    }

    
    
    /**
     * {@inheritDoc}
     *
     * Overrides the super implementation in order to return the list of
     * <tt>Format</tt>s hardcoded as supported in
     * <tt>IVFCaptureDevice</tt> because the super looks them up by
     * <tt>CaptureDeviceInfo</tt> and it doesn't have some information
     * (like the framerate etc.).
     */
    @Override
    protected Format[] getSupportedFormats(int streamIndex)
    {
        return SUPPORTED_FORMATS.clone();
    }
}
