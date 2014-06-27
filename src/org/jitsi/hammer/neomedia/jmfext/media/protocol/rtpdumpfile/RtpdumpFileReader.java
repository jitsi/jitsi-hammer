/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jitsi.hammer.neomedia.jmfext.media.protocol.rtpdumpfile;

import java.io.*;

/**
 * This class represent a rtpdump file and provide an API to get the
 * payload of the rtp packet it contains.
 * 
 * rtpdump format : 
 *  - http://www.cs.columbia.edu/irt/software/rtptools/
 *  - https://gist.github.com/Haerezis/18e3ffc2d69c86f8463f#file-rtpdump_file_format
 *      (backup gist)
 * 
 * 
 * rtpdump file can be generated with wireshark from RTP stream, just go to :
 * -> Telephony -> RTP -> Show All Streams
 * then select the RTP stream you want to record, and click on "Save As".
 * 
 * If the RTP menu isn't found in the Telephony menu, maybe you can find it in
 * the "Statistics" menu.
 *
 * 
 * @author Thomas Kuntz
 */
public class RtpdumpFileReader
{
    /*
     * The file wireshark/ui/tap-rtp-common.c , more specificaly the function
     * rtp_write_header that write the file header, show that this header
     * is 4+4+4+2+2=16 bytes.
     */
    public final static int FILE_HEADER_LENGTH = 4 + 4 + 4 + 2 + 2;
    //public final static int FILE_HEADER_LENGTH = 8 + 8 + 4 + 2 + 2;
    
    private RandomAccessFile stream;
    
    public RtpdumpFileReader(String filePath)
    {
        
        try
        {
            stream = new RandomAccessFile(filePath,"r");
            resetFile();
        }
        catch (FileNotFoundException e) 
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    
    public RTPPacket getNextPacket(boolean loopFile) throws IOException
    {
        if((loopFile == true) && (stream.getFilePointer() >= stream.length()))
        {
            resetFile();
        }
        
        byte[] rtpPacket;
        int sizeInBytes;
        int timestamp;
                
        stream.readShort();//read away an useless short (2 bytes)
        sizeInBytes = stream.readUnsignedShort();
        rtpPacket = new byte[sizeInBytes];
        timestamp = stream.readInt();
        
        stream.read(rtpPacket);
        
        return new RTPPacket(rtpPacket,timestamp);
    }
    
    private void resetFile() throws IOException
    {
        /*
         * Go to the beginning of the rtpdum file and
         * skip the first line of ascii, giving the file version
         * skip the file header (useless)
         */
        stream.seek( 0 );
        stream.readLine();//read the first line that is ascii
        stream.seek( stream.getFilePointer() + RtpdumpFileReader.FILE_HEADER_LENGTH );
    }
}