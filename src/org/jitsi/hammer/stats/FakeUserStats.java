/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jitsi.hammer.stats;

import org.jitsi.service.neomedia.*;

/**
 * @author Thomas Kuntz
 *
 */
public class FakeUserStats
{
    private String username = "Hammer";
    private long audioSSRC = -1;
    private MediaStreamStats audioStats;
    private long videoSSRC = -1;
    private MediaStreamStats videoStats;

    public void setMediaStreamStats(MediaStream mediaStream)
    {
        if(mediaStream == null)
        {
            throw new NullPointerException("MediaStream can't be null");
        }

        if(mediaStream instanceof AudioMediaStream)
        {
            audioSSRC = mediaStream.getLocalSourceID();
            audioStats = mediaStream.getMediaStreamStats();
        }
        else if(mediaStream instanceof VideoMediaStream)
        {
            videoSSRC = mediaStream.getLocalSourceID();
            videoStats = mediaStream.getMediaStreamStats();
        }
        //other MediaType are not tracked.
    }

    public void setUsername(String username)
    {
        this.username = username;
    }


    public void updateStats()
    {
        audioStats.updateStats();
        videoStats.updateStats();
    }


    public MediaStreamStats getMediaStreamStats(MediaType type)
    {
        MediaStreamStats stats = null;
        switch(type)
        {
        case AUDIO:
            stats = audioStats;
            break;
        case VIDEO:
            stats = videoStats;
            break;
        default:
            break;
        }

        return stats;
    }


    public String getStatsJSON(int spaceIdent)
    {
        String ident = new String(new char[spaceIdent]).replace("\0" ,  " ");
        StringBuilder builder = new StringBuilder();
        builder.append(ident + "{\n");
        builder.append(ident + "  \"username\" : \""+ this.username +"\" , \n");
        builder.append(ident + "  \"streams\" :\n");
        builder.append(ident + "  {\n");


        builder.append(ident + "      \"audio\" : {");
        builder.append(" \"ssrc\":"+audioSSRC);
        builder.append(" , \"DownloadRateKiloBitPerSec\":"+audioStats.getDownloadRateKiloBitPerSec());
        builder.append(" , \"UploadKbps\":"+audioStats.getUploadRateKiloBitPerSec());
        builder.append(" , \"UploadRateKiloBitPerSec\":"+audioStats.getDownloadPercentLoss());
        builder.append(" , \"UploadPercentLoss\":"+audioStats.getUploadPercentLoss());
        builder.append(" , \"NbFec\":"+audioStats.getNbFec());
        builder.append(" , \"PercentDiscarded\":"+audioStats.getPercentDiscarded());
        builder.append(" , \"NbDiscarded\":"+audioStats.getNbDiscarded());
        builder.append(" , \"NbDiscardedFull\":"+audioStats.getNbDiscardedFull());
        builder.append(" , \"NbDiscardedLate\":"+audioStats.getNbDiscardedLate());
        builder.append(" , \"NbDiscardedReset\":"+audioStats.getNbDiscardedReset());
        builder.append(" , \"NbDiscardedShrink\":"+audioStats.getNbDiscardedShrink());
        builder.append(" , \"JitterBufferDelayMs\":"+audioStats.getJitterBufferDelayMs());
        builder.append(" , \"PacketQueueCountPackets\":"+audioStats.getPacketQueueCountPackets());
        builder.append(" , \"PacketQueueSize\":"+audioStats.getPacketQueueSize());
        builder.append(" , \"RttMs\":"+audioStats.getRttMs());
        builder.append(" , \"DownloadJitterMs\":"+audioStats.getDownloadJitterMs());
        builder.append(" , \"UploadJitterMs\":"+audioStats.getUploadJitterMs());
        builder.append(" },\n");

        builder.append(ident + "      \"video\" : {");
        builder.append(" \"ssrc\":"+videoSSRC);
        builder.append(" , \"DownloadRateKiloBitPerSec\":"+videoStats.getDownloadRateKiloBitPerSec());
        builder.append(" , \"UploadKbps\":"+videoStats.getUploadRateKiloBitPerSec());
        builder.append(" , \"UploadRateKiloBitPerSec\":"+videoStats.getDownloadPercentLoss());
        builder.append(" , \"UploadPercentLoss\":"+videoStats.getUploadPercentLoss());
        builder.append(" , \"NbFec\":"+videoStats.getNbFec());
        builder.append(" , \"PercentDiscarded\":"+videoStats.getPercentDiscarded());
        builder.append(" , \"NbDiscarded\":"+videoStats.getNbDiscarded());
        builder.append(" , \"NbDiscardedFull\":"+videoStats.getNbDiscardedFull());
        builder.append(" , \"NbDiscardedLate\":"+videoStats.getNbDiscardedLate());
        builder.append(" , \"NbDiscardedReset\":"+videoStats.getNbDiscardedReset());
        builder.append(" , \"NbDiscardedShrink\":"+videoStats.getNbDiscardedShrink());
        builder.append(" , \"JitterBufferDelayMs\":"+videoStats.getJitterBufferDelayMs());
        builder.append(" , \"PacketQueueCountPackets\":"+videoStats.getPacketQueueCountPackets());
        builder.append(" , \"PacketQueueSize\":"+videoStats.getPacketQueueSize());
        builder.append(" , \"RttMs\":"+videoStats.getRttMs());
        builder.append(" , \"DownloadJitterMs\":"+videoStats.getDownloadJitterMs());
        builder.append(" , \"UploadJitterMs\":"+videoStats.getUploadJitterMs());
        builder.append(" }\n");

        builder.append(ident + "  }\n");
        builder.append(ident + "}");

        return builder.toString();
    }
}
