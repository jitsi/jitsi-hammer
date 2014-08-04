/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jitsi.hammer.stats;


import java.io.*;
import java.text.*;
import java.util.*;

import org.apache.commons.math3.stat.descriptive.*;
import org.jitsi.hammer.*;
import org.jitsi.service.neomedia.MediaStreamStats;
import org.jitsi.service.neomedia.MediaType;

/**
 * @author Thomas Kuntz
 *
 * This class is used to keep track of stats of all the streams of all the
 * fake users (<tt>FakeUser</tt>), generate new new stats, writes stats
 * to files, print them etc...
 *
 */
public class HammerStats implements Runnable
{
    /**
     * A boolean used to stop the run method of this <tt>HammerStats</tt>.
     */
    private boolean threadStop = false;

    /**
     * The name (not the path or location) of the directory where
     * the stats files will be written.
     */
    private static final String STATS_DIR_NAME = "stats";

    /**
     * The path to the stats directory. All stats will be written in files
     * located in this directory.
     */
    private final String statsDirectoryPath;

    /**
     * The file that will contain the overall stats
     */
    private final File overallStatsFile;

    /**
     * The file that will contain all the stats recorded by run()
     */
    private final File allStatsFile;

    /**
     * An <tt>List</tt> of <tt>FakeUserStats</tt> that contains the
     * <tt>MediaStreamStats</tt>s of the <tt>FakeUser</tt>.
     * It is used to keep track of the streams' stats.
     */
    private final ArrayList<FakeUserStats> fakeUserStatsList =
        new ArrayList<FakeUserStats>();

    /**
     * The time (in seconds) the HammerStats wait between two updates.
     */
    private int timeBetweenUpdate = 5;

    /**
     * The boolean used to know if the logging of all the stats in the
     * run method is enable.
     */
    private boolean allStatsLogging = false;

    /**
     * The boolean used to know if the logging of the summary stats
     * (like average, standard deviation, min, max...) computed from
     * all the streams' stats is enable of not.
     */
    private boolean summaryStatsLogging = false;

    /**
     * The HammerSummaryStats used to compute summary stats from the
     * audio streams' stats.
     */
    HammerSummaryStats audioSummaryStats = new HammerSummaryStats();

    /**
     * The HammerSummaryStats used to compute summary stats from the
     * video streams' stats.
     */
    HammerSummaryStats videoSummaryStats = new HammerSummaryStats();

    /**
     * Initialize an instance of a <tt>HammerStats</tt> with the default
     * stats directory path.
     */
    public HammerStats()
    {
        this( System.getProperty(Main.PNAME_SC_HOME_DIR_LOCATION)
            + File.separator
            + System.getProperty(Main.PNAME_SC_HOME_DIR_NAME)
            + File.separator
            + HammerStats.STATS_DIR_NAME);
    }

    /**
     * Initialize an instance of a <tt>HammerStats</tt> with a custom stats
     * directory path.
     * @param statsDirectoryPath the path to the stats directory, where the
     * stats files will be saved.
     */
    public HammerStats(String statsDirectoryPath)
    {
        this.statsDirectoryPath =
            statsDirectoryPath
            + File.separator
            + new SimpleDateFormat("yyyy-MM-dd'  'HH.mm.ss").format(new Date());

        this.overallStatsFile = new File(
            this.statsDirectoryPath
            + File.separator
            + "overallStats.json");
        this.allStatsFile = new File(
            this.statsDirectoryPath
            + File.separator
            + "allStats.json");
    }


    public synchronized void addFakeUsersStats(
        FakeUserStats fakeUserStats)
    {
        if(fakeUserStats == null)
        {
            throw new NullPointerException("FakeUserStats can't be null");
        }
        fakeUserStatsList.add(fakeUserStats);
    }

    /**
     * Keep track, collect and update the stats of all the
     * <tt>MediaStreamStats</tt> this <tt>HammerStats</tt> handles.
     *
     * Also write the results in the stats files.
     */
    public void run()
    {
        PrintWriter writer = null;
        StringBuilder allBldr = new StringBuilder();
        String delim;
        String delim_ = "";
        synchronized(this)
        {
            threadStop = false;
        }

        while(threadStop == false)
        {
            synchronized(this)
            {
                if(allStatsLogging || summaryStatsLogging)
                {
                    if(writer == null)
                    {
                        try
                        {
                            writer = new PrintWriter(allStatsFile, "UTF-8");
                            writer.print("[\n");
                        }
                        catch (FileNotFoundException e)
                        {
                            e.printStackTrace();
                        }
                        catch (UnsupportedEncodingException e)
                        {
                            e.printStackTrace();
                        }
                    }

                    //Clear the StringBuilder
                    allBldr.setLength(0);

                    writer.print(delim_ + '\n');
                    delim_ = ",";
                    writer.print("{\n");
                    writer.print("  \"timestamp\":" + System.currentTimeMillis()+",\n");

                    if(allStatsLogging)
                    {

                    }
                    if(summaryStatsLogging)
                    {
                        audioSummaryStats.clear();
                        videoSummaryStats.clear();
                    }

                    delim = "";
                    for(FakeUserStats stats : fakeUserStatsList)
                    {
                        //We update the stats before using/reading them.
                        stats.updateStats();

                        if(allStatsLogging)
                        {
                            allBldr.append(delim + stats.getStatsJSON(2) + '\n');
                            delim = ",";
                        }

                        if(summaryStatsLogging)
                        {
                            audioSummaryStats.add(
                                stats.getMediaStreamStats(MediaType.AUDIO));
                            videoSummaryStats.add(
                                stats.getMediaStreamStats(MediaType.VIDEO));
                        }
                    }

                    if(allStatsLogging)
                    {
                        writer.print("  \"users\":\n");
                        writer.print("  [\n");
                        writer.print(allBldr.toString());
                        writer.print("  ]");
                        if(summaryStatsLogging) writer.print(',');
                        writer.print('\n');
                    }
                    if(summaryStatsLogging)
                    {
                        writer.print("  \"summary\":\n");
                        writer.print("  {\n");


                        writer.print("    \"max\":\n");
                        writer.print("    {\n");
                        writer.print("        \"audio\":");
                        writer.print(audioSummaryStats.getMaxJSON() + ",\n");
                        writer.print("        \"video\":");
                        writer.print(videoSummaryStats.getMaxJSON() + '\n');
                        writer.print("    },\n");

                        writer.print("    \"mean\":\n");
                        writer.print("    {\n");
                        writer.print("       \"audio\":");
                        writer.print(audioSummaryStats.getMeanJSON() + ",\n");
                        writer.print("        \"video\":");
                        writer.print(videoSummaryStats.getMeanJSON() + '\n');
                        writer.print("    },\n");

                        writer.print("    \"min\":\n");
                        writer.print("    {\n");
                        writer.print("        \"audio\":");
                        writer.print(audioSummaryStats.getMinJSON() + ",\n");
                        writer.print("        \"video\":");
                        writer.print(videoSummaryStats.getMinJSON() + '\n');
                        writer.print("    },\n");

                        writer.print("    \"standard_deviation\":\n");
                        writer.print("    {\n");
                        writer.print("        \"audio\":");
                        writer.print(audioSummaryStats.getStandardDeviationJSON() + ",\n");
                        writer.print("        \"video\":");
                        writer.print(videoSummaryStats.getStandardDeviationJSON() + '\n');
                        writer.print("    }\n");


                        writer.print("  }\n");
                    }

                    writer.append("}");
                    writer.flush();
                }
            }

            try
            {
                Thread.sleep(timeBetweenUpdate * 1000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
                threadStop = true;
            }
        }

        if(writer != null)
        {
            writer.print("]\n");
            writer.close();
        }
    }

    /**
     * Provoke the stop of the method run(). If the method run() is not running,
     * calling this method won't do anything
     */
    public synchronized void stop()
    {
        threadStop = true;
    }

    /**
     * Write the overall stats of the <tt>MediaStream</tt> this
     * <tt>MediaStreamStats</tt> keep track in its file.
     */
    public void writeOverallStats()
    {
        //Make sur that the directory was created
        File saveDir = new File(this.statsDirectoryPath);
        if(saveDir.exists() == false)
        {
            saveDir.mkdirs();
        }

        try
        {
            PrintWriter writer = new PrintWriter(overallStatsFile, "UTF-8");
            writer.print(getOverallStats() + '\n');
            writer.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * print the overall stats of the <tt>MediaStream</tt> this
     * <tt>MediaStreamStats</tt> keep track to stdout.
     */
    public void printOverallStats()
    {
        System.out.println(getOverallStats());
    }

    /**
     * Create and return the String that contains the overall stats.
     * @return the String that contains the overall stats.
     */
    protected String getOverallStats()
    {
        //TODO
        return "";
    }

    /**
     * Set the time this <tt>HammerStats</tt> will wait between 2 updates of
     * stats.
     * @param timeval the time of the wait, in seconds
     */
    public void setTimeBetweenUpdate(int timeval)
    {
        if(timeval <= 0) timeval = 1;
        this.timeBetweenUpdate = timeval;
    }

    /**
     * Get the time (in seconds) this <tt>HammerStats</tt> will wait
     * between 2 updates of stats.
     */
    public int getTimeBetweenUpdate()
    {
        return this.timeBetweenUpdate;
    }

    /**
     * Enable or disable the logging of all the stats collected by this
     * <tt>HammerStats</tt>.
     * @param allStats the boolean that enable of disable the logging.
     */
    public void setAllStatsLogging(boolean allStats)
    {
        this.allStatsLogging = allStats;
        if(allStats)
        {
            File saveDir = new File(this.statsDirectoryPath);
            if(saveDir.exists() == false)
            {
                saveDir.mkdirs();
            }
        }
    }

    /**
     * Enable or disable the logging of all the stats collected by this
     * <tt>HammerStats</tt>.
     * @param allStats the boolean that enable of disable the logging.
     */
    public void setSummaryStatsLogging(boolean summaryStats)
    {
        this.summaryStatsLogging = summaryStats;
        if(summaryStats)
        {
            File saveDir = new File(this.statsDirectoryPath);
            if(saveDir.exists() == false)
            {
                saveDir.mkdirs();
            }
        }
    }


    private class HammerSummaryStats
    {
        SummaryStatistics downloadRateKiloBitPerSec = new SummaryStatistics();
        SummaryStatistics uploadRateKiloBitPerSec = new SummaryStatistics();
        SummaryStatistics downloadPercentLoss = new SummaryStatistics();
        SummaryStatistics uploadPercentLoss = new SummaryStatistics();
        SummaryStatistics nbFec = new SummaryStatistics();
        SummaryStatistics percentDiscarded = new SummaryStatistics();
        SummaryStatistics nbDiscarded = new SummaryStatistics();
        SummaryStatistics nbDiscardedFull = new SummaryStatistics();
        SummaryStatistics nbDiscardedLate = new SummaryStatistics();
        SummaryStatistics nbDiscardedReset = new SummaryStatistics();
        SummaryStatistics nbDiscardedShrink = new SummaryStatistics();
        SummaryStatistics jitterBufferDelayMs = new SummaryStatistics();
        SummaryStatistics packetQueueCountPackets = new SummaryStatistics();
        SummaryStatistics packetQueueSize = new SummaryStatistics();
        SummaryStatistics rttMs = new SummaryStatistics();
        SummaryStatistics downloadJitterMs = new SummaryStatistics();
        SummaryStatistics uploadJitterMs = new SummaryStatistics();

        public void add(MediaStreamStats stats)
        {
            downloadRateKiloBitPerSec.addValue(stats.getDownloadRateKiloBitPerSec());
            uploadRateKiloBitPerSec.addValue(stats.getUploadRateKiloBitPerSec());
            downloadPercentLoss.addValue(stats.getDownloadPercentLoss());
            uploadPercentLoss.addValue(stats.getUploadPercentLoss());
            nbFec.addValue(stats.getNbFec());
            percentDiscarded.addValue(stats.getPercentDiscarded());
            nbDiscarded.addValue(stats.getNbDiscarded());
            nbDiscardedFull.addValue(stats.getNbDiscardedFull());
            nbDiscardedLate.addValue(stats.getNbDiscardedLate());
            nbDiscardedReset.addValue(stats.getNbDiscardedReset());
            nbDiscardedShrink.addValue(stats.getNbDiscardedShrink());
            jitterBufferDelayMs.addValue(stats.getJitterBufferDelayMs());
            packetQueueCountPackets.addValue(stats.getPacketQueueCountPackets());
            packetQueueSize.addValue(stats.getPacketQueueSize());
            rttMs.addValue(stats.getRttMs());
            downloadJitterMs.addValue(stats.getDownloadJitterMs());
            uploadJitterMs.addValue(stats.getUploadJitterMs());
        }

        public void clear()
        {
            downloadRateKiloBitPerSec.clear();
            uploadRateKiloBitPerSec.clear();
            downloadPercentLoss.clear();
            uploadPercentLoss.clear();
            nbFec.clear();
            percentDiscarded.clear();
            nbDiscarded.clear();
            nbDiscardedFull.clear();
            nbDiscardedLate.clear();
            nbDiscardedReset.clear();
            nbDiscardedShrink.clear();
            jitterBufferDelayMs.clear();
            packetQueueCountPackets.clear();
            packetQueueSize.clear();
            rttMs.clear();
            downloadJitterMs.clear();
            uploadJitterMs.clear();
        }

        public String getMaxJSON()
        {
            String str = String.format(FakeUserStats.jsonMediaStreamStatsTemplate,
                -1, //ssrc not needed here
                downloadRateKiloBitPerSec.getMax(),
                uploadRateKiloBitPerSec.getMax(),
                downloadPercentLoss.getMax(),
                uploadPercentLoss.getMax(),
                nbFec.getMax(),
                percentDiscarded.getMax(),
                nbDiscarded.getMax(),
                nbDiscardedFull.getMax(),
                nbDiscardedLate.getMax(),
                nbDiscardedReset.getMax(),
                nbDiscardedShrink.getMax(),
                jitterBufferDelayMs.getMax(),
                packetQueueCountPackets.getMax(),
                packetQueueSize.getMax(),
                rttMs.getMax(),
                downloadJitterMs.getMax(),
                uploadJitterMs.getMax());
            return str;
        }

        public String getMeanJSON()
        {
            String str = String.format(FakeUserStats.jsonMediaStreamStatsTemplate,
                -1, //ssrc not needed here
                downloadRateKiloBitPerSec.getMean(),
                uploadRateKiloBitPerSec.getMean(),
                downloadPercentLoss.getMean(),
                uploadPercentLoss.getMean(),
                nbFec.getMean(),
                percentDiscarded.getMean(),
                nbDiscarded.getMean(),
                nbDiscardedFull.getMean(),
                nbDiscardedLate.getMean(),
                nbDiscardedReset.getMean(),
                nbDiscardedShrink.getMean(),
                jitterBufferDelayMs.getMean(),
                packetQueueCountPackets.getMean(),
                packetQueueSize.getMean(),
                rttMs.getMean(),
                downloadJitterMs.getMean(),
                uploadJitterMs.getMean());
            return str;
        }

        public String getMinJSON()
        {
            String str = String.format(FakeUserStats.jsonMediaStreamStatsTemplate,
                -1, //ssrc not needed here
                downloadRateKiloBitPerSec.getMin(),
                uploadRateKiloBitPerSec.getMin(),
                downloadPercentLoss.getMin(),
                uploadPercentLoss.getMin(),
                nbFec.getMin(),
                percentDiscarded.getMin(),
                nbDiscarded.getMin(),
                nbDiscardedFull.getMin(),
                nbDiscardedLate.getMin(),
                nbDiscardedReset.getMin(),
                nbDiscardedShrink.getMin(),
                jitterBufferDelayMs.getMin(),
                packetQueueCountPackets.getMin(),
                packetQueueSize.getMin(),
                rttMs.getMin(),
                downloadJitterMs.getMin(),
                uploadJitterMs.getMin());
            return str;
        }

        public String getStandardDeviationJSON()
        {
            String str = String.format(FakeUserStats.jsonMediaStreamStatsTemplate,
                -1, //ssrc not needed here
                downloadRateKiloBitPerSec.getStandardDeviation(),
                uploadRateKiloBitPerSec.getStandardDeviation(),
                downloadPercentLoss.getStandardDeviation(),
                uploadPercentLoss.getStandardDeviation(),
                nbFec.getStandardDeviation(),
                percentDiscarded.getStandardDeviation(),
                nbDiscarded.getStandardDeviation(),
                nbDiscardedFull.getStandardDeviation(),
                nbDiscardedLate.getStandardDeviation(),
                nbDiscardedReset.getStandardDeviation(),
                nbDiscardedShrink.getStandardDeviation(),
                jitterBufferDelayMs.getStandardDeviation(),
                packetQueueCountPackets.getStandardDeviation(),
                packetQueueSize.getStandardDeviation(),
                rttMs.getStandardDeviation(),
                downloadJitterMs.getStandardDeviation(),
                uploadJitterMs.getStandardDeviation());
            return str;
        }

        public String getSumJSON()
        {
            String str = String.format(FakeUserStats.jsonMediaStreamStatsTemplate,
                -1, //ssrc not needed here
                downloadRateKiloBitPerSec.getSum(),
                uploadRateKiloBitPerSec.getSum(),
                downloadPercentLoss.getSum(),
                uploadPercentLoss.getSum(),
                nbFec.getSum(),
                percentDiscarded.getSum(),
                nbDiscarded.getSum(),
                nbDiscardedFull.getSum(),
                nbDiscardedLate.getSum(),
                nbDiscardedReset.getSum(),
                nbDiscardedShrink.getSum(),
                jitterBufferDelayMs.getSum(),
                packetQueueCountPackets.getSum(),
                packetQueueSize.getSum(),
                rttMs.getSum(),
                downloadJitterMs.getSum(),
                uploadJitterMs.getSum());
            return str;
        }

        public String getVarianceJSON()
        {
            String str = String.format(FakeUserStats.jsonMediaStreamStatsTemplate,
                -1, //ssrc not needed here
                downloadRateKiloBitPerSec.getVariance(),
                uploadRateKiloBitPerSec.getVariance(),
                downloadPercentLoss.getVariance(),
                uploadPercentLoss.getVariance(),
                nbFec.getVariance(),
                percentDiscarded.getVariance(),
                nbDiscarded.getVariance(),
                nbDiscardedFull.getVariance(),
                nbDiscardedLate.getVariance(),
                nbDiscardedReset.getVariance(),
                nbDiscardedShrink.getVariance(),
                jitterBufferDelayMs.getVariance(),
                packetQueueCountPackets.getVariance(),
                packetQueueSize.getVariance(),
                rttMs.getVariance(),
                downloadJitterMs.getVariance(),
                uploadJitterMs.getVariance());
            return str;
        }
    }
}
