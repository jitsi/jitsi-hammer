/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jitsi.hammer.device;

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
public class IVFCaptureDevice
    extends AbstractVideoPullBufferCaptureDevice
{
    private Format[] SUPPORTED_FORMATS = new Format[1];
    private String file;
    private IVFHeader ivfHeader;
    
    
    /**
     * Initializes a new <tt>DataSource</tt> instance.
     */
    public IVFCaptureDevice(String filePath)
    {
        this.file = filePath;
        ivfHeader = new IVFHeader(filePath);
        
        this.SUPPORTED_FORMATS[0] = new VideoFormat(
                Constants.VP8,
                ivfHeader.getDimension(),
                Format.NOT_SPECIFIED,
                Format.byteArray,
                ivfHeader.getFramerate());
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
     * <tt>CaptureDeviceInfo</tt> and this instance does not have one.
     */
    @Override
    protected Format[] getSupportedFormats(int streamIndex)
    {
        return SUPPORTED_FORMATS;
    }
    
    
    /**
     * return the header of the IVF file this <tt>CaptureDevice</tt> read into.
     * @return the <tt>IVFHeader<tt> containing the header of the IVF file of
     * this <tt>CaptureDevice</tt>
     */
    protected IVFHeader getIVFHeader()
    {
        return ivfHeader;
    }
    
    /**
     * Get the path of the IVF file this CaptureDevice have to read
     * @return the path of the IVF file
     */
    protected String getFilePath()
    {
        return file;
    }
}
