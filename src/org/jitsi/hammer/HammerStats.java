/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jitsi.hammer;


import java.util.*;

import org.jitsi.service.neomedia.*;

/**
 * @author Thomas Kuntz
 * 
 * This class is used to keep track of stats of all the streams of all the
 * fake users (<tt>JingleSession</tt>), generate new new stats, writes stats
 * to files, print them etc...
 *
 */
public class HammerStats
{
    private ArrayList<MediaStreamStats> audioStreamsStats = 
            new ArrayList<MediaStreamStats>();
    
    
    private ArrayList<MediaStreamStats> videoStreamsStats = 
            new ArrayList<MediaStreamStats>();
    
    public HammerStats() {}
    
    
    public synchronized void addStreams(
            AudioMediaStream audioMediaStream,
            VideoMediaStream videoMediaStream)
    {
        if((audioMediaStream == null) || (videoMediaStream == null))
        {
            throw new NullPointerException("MediaStream can't be null");
        }
        
        audioStreamsStats.add(audioMediaStream.getMediaStreamStats());
        videoStreamsStats.add(videoMediaStream.getMediaStreamStats());
    }
}
