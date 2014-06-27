/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jitsi.hammer.neomedia.jmfext.media.protocol.rtpdumpfile;

import java.awt.*;
import java.io.*;
import java.util.Arrays;

import com.google.common.io.LittleEndianDataInputStream;

/**
 * This class represent a RTP packet (header+payload) of a RTP stream recorded
 * in a rtpdump file
 * 
 * 
 * @author Thomas Kuntz
 */
public class RTPPacket
{
    private int rtpdump_timestamp;
    private short version;
    private boolean padding;
    private boolean extension;
    private short CSRCCount;
    private boolean marker;
    private int sequenceNumber;
    private short payloadType;
    private long timestamp;
    private long ssrc;
    private long[] CSRC;
    private byte[] payload;
    
    
    public RTPPacket(byte[] rtpPacket,int timestamp)
    {
        this(rtpPacket);
        rtpdump_timestamp = timestamp;
    }
    
    public RTPPacket(byte[] rtpPacket)
    {
        version = (short) ((rtpPacket[0] & 0xC0) >> 6);
        padding = ((rtpPacket[0] & 0x20) != 0);
        extension = ((rtpPacket[0] & 0x10) != 0);
        CSRCCount = (short) ((rtpPacket[0] & 0x0F));
        
        marker = ((rtpPacket[1] & 0x80) != 0);
        payloadType = (short) ((rtpPacket[1] & 0x7F));
        
        
        sequenceNumber = ((rtpPacket[2] & 0xFF) << 8) | (rtpPacket[3] & 0xFF);
        
        
        
        timestamp = 
                ((rtpPacket[4] & 0xFF) << 24) |
                ((rtpPacket[5] & 0xFF) << 16)  |
                ((rtpPacket[6] & 0xFF) << 8)  |
                ((rtpPacket[7] & 0xFF) << 0);
        
        ssrc =  
                ((rtpPacket[8] & 0xFF) << 24) |
                ((rtpPacket[9] & 0xFF) << 16) |
                ((rtpPacket[10] & 0xFF) << 8) |
                ((rtpPacket[11] & 0xFF) << 0);
        
        
        
        CSRC = new long[CSRCCount];
        for(int i = 0; i< CSRCCount ; i++)
        {
            CSRC[i] =   
                    ((rtpPacket[12 + i*4 + 0] & 0xFF) << 24) |
                    ((rtpPacket[12 + i*4 + 1] & 0xFF) << 16)  |
                    ((rtpPacket[12 + i*4 + 2] & 0xFF) << 8)  |
                    ((rtpPacket[12 + i*4 + 3] & 0xFF) << 0);
        }
        
        payload = Arrays.copyOfRange(
                rtpPacket,
                12 + CSRCCount*4,
                rtpPacket.length);
    }
    
    public int getRtpdumpTimestamp()
    {
        return rtpdump_timestamp;
    }
    
    public short getVersion()
    {
        return version;
    }
    
    public boolean hasPadding()
    {
        return padding;
    }
    
    public boolean hasExtension()
    {
        return extension;
    }
    
    public short getCSRCCount()
    {
        return CSRCCount;
    }
    
    public boolean hasMarker()
    {
        return marker;
    }
    
    public short getPayloadType()
    {
        return payloadType;
    }
    
    public int getSequenceNumber()
    {
        return sequenceNumber;
    }
    
    public long getTimestamp()
    {
        return timestamp;
    }
    
    public long getSSRC()
    {
        return ssrc;
    }
    
    public long[] getCSRC()
    {
        return CSRC;
    }
    
    public byte[] getPayload()
    {
        return payload;
    }
}