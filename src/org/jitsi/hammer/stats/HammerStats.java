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
import org.jitsi.util.Logger;

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
     * The <tt>Logger</tt> used by the <tt>HammerStats</tt> class and its
     * instances for logging output.
     */
    private static final Logger logger
        = Logger.getLogger(HammerStats.class);

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
     * (like mean, standard deviation, min, max...) computed at each polling from
     * all the streams' stats is enable of not.
     */
    private boolean summaryStatsLogging = false;

    /**
     * The boolean used to know if the logging of the overall stats
     * (like mean, standard deviation, min, max...) computed from
     * all the streams' stats collected is enable of not.
     */
    private boolean overallStatsLogging;
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
            + new SimpleDateFormat("yyyy-MM-dd'  'HH'h'mm'm'ss's'").format(new Date());

        this.overallStatsFile = new File(
            this.statsDirectoryPath
            + File.separator
            + "overallStats.json");
        this.allStatsFile = new File(
            this.statsDirectoryPath
            + File.separator
            + "AllAndSummaryStats.json");

        logger.info("Stats directory : " + this.statsDirectoryPath);
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

        logger.info("Running the main loop");
        while(threadStop == false)
        {
            logger.info("New loop turn");
            synchronized(this)
            {
                if(overallStatsLogging || allStatsLogging || summaryStatsLogging)
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
                                logger.fatal(e.getStackTrace());
                                logger.fatal("HammerStats stopping due to FileNotFound");
                                stop();
                            }
                            catch (UnsupportedEncodingException e)
                            {
                                logger.fatal(e.getStackTrace());
                                logger.fatal("HammerStats stopping due to "
                                    + "UnsupportedEncoding");
                            }
                        }

                        //Clear the StringBuilder
                        allBldr.setLength(0);

                        writer.print(delim_ + '\n');
                        delim_ = ",";
                        writer.print("{\n");
                        writer.print("  \"timestamp\":" + System.currentTimeMillis()+",\n");
                    }

                    delim = "";
                    logger.info("Updating the MediaStreamStats");
                    for(FakeUserStats stats : fakeUserStatsList)
                    {
                        //We update the stats before using/reading them.
                        stats.updateStats();
                    }

                    for(FakeUserStats stats : fakeUserStatsList)
                    {
                        if(allStatsLogging)
                        {
                            allBldr.append(delim + stats.getStatsJSON(2) + '\n');
                            delim = ",";
                        }

                        if(summaryStatsLogging || overallStatsLogging)
                        {
                            logger.info("Adding stats values from the"
                                + " MediaStreamStats to their"
                                + " HammerSummaryStats objects");
                            audioSummaryStats.add(
                                stats.getMediaStreamStats(MediaType.AUDIO));
                            videoSummaryStats.add(
                                stats.getMediaStreamStats(MediaType.VIDEO));
                        }
                    }

                    if(allStatsLogging)
                    {
                        logger.info("Writing all stats to file");
                        writer.print("  \"users\":\n");
                        writer.print("  [\n");
                        writer.print(allBldr.toString());
                        writer.print("  ]");
                        if(summaryStatsLogging) writer.print(',');
                        writer.print('\n');
                    }
                    if(summaryStatsLogging)
                    {
                        logger.info("Writing summary stats to file");
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
                    if(allStatsLogging || summaryStatsLogging)
                    {
                        writer.append("}");
                        writer.flush();
                    }
                }

                if(summaryStatsLogging || overallStatsLogging)
                {
                    logger.info("Clearing the HammerSummaryStats by creating new"
                        + " SummaryStats objects for each watched stats");
                    audioSummaryStats.clear();
                    videoSummaryStats.clear();
                }
            }

            try
            {
                Thread.sleep(timeBetweenUpdate * 1000);
            }
            catch (InterruptedException e)
            {
                logger.fatal("Error during sleep in main loop : " + e);
                stop();
            }
        }
        logger.info("Exiting the main loop");

        if(writer != null)
        {
            writer.print("]\n");
            writer.close();
        }

        if(overallStatsLogging) writeOverallStats();
    }

    /**
     * Provoke the stop of the method run(). If the method run() is not running,
     * calling this method won't do anything
     */
    public synchronized void stop()
    {
        logger.error("Stopping the main loop");
        threadStop = true;
    }

    /**
     * Write the overall stats of the <tt>MediaStream</tt> this
     * <tt>MediaStreamStats</tt> keep track in its file.
     */
    public void writeOverallStats()
    {
        try
        {
            logger.info("Writting overall stats to file");
            PrintWriter writer = new PrintWriter(overallStatsFile, "UTF-8");
            writer.print(getOverallStats() + '\n');
            writer.close();
        }
        catch (FileNotFoundException e)
        {
            logger.error(e.getStackTrace());
        }
        catch (UnsupportedEncodingException e)
        {
            logger.error(e.getStackTrace());
        }
    }

    /**
     * print the overall stats of the <tt>MediaStream</tt> this
     * <tt>MediaStreamStats</tt> keep track to the PrintStream given as argument.
     * @param ps the <tt>PrintStream</tt> used to print the stats
     */
    public void printOverallStats(PrintStream ps)
    {
        ps.println(getOverallStats());
    }

    /**
     * Create and return the String that contains the overall stats.
     * @return the String that contains the overall stats.
     */
    protected String getOverallStats()
    {
        StringBuilder bldr = new StringBuilder();
        bldr.append("{\n");


        bldr.append("  \"max\":\n");
        bldr.append("  {\n");
        bldr.append("      \"audio\":");
        bldr.append(audioSummaryStats.getAggregateMaxJSON() + ",\n");
        bldr.append("      \"video\":");
        bldr.append(videoSummaryStats.getAggregateMaxJSON() + '\n');
        bldr.append("  },\n");

        bldr.append("  \"mean\":\n");
        bldr.append("  {\n");
        bldr.append("     \"audio\":");
        bldr.append(audioSummaryStats.getAggregateMeanJSON() + ",\n");
        bldr.append("      \"video\":");
        bldr.append(videoSummaryStats.getAggregateMeanJSON() + '\n');
        bldr.append("  },\n");

        bldr.append("  \"min\":\n");
        bldr.append("  {\n");
        bldr.append("      \"audio\":");
        bldr.append(audioSummaryStats.getAggregateMinJSON() + ",\n");
        bldr.append("      \"video\":");
        bldr.append(videoSummaryStats.getAggregateMinJSON() + '\n');
        bldr.append("  },\n");

        bldr.append("  \"standard_deviation\":\n");
        bldr.append("  {\n");
        bldr.append("      \"audio\":");
        bldr.append(audioSummaryStats.getAggregateStandardDeviationJSON() + ",\n");
        bldr.append("      \"video\":");
        bldr.append(videoSummaryStats.getAggregateStandardDeviationJSON() + '\n');
        bldr.append("  },\n");

        bldr.append("  \"sum\":\n");
        bldr.append("  {\n");
        bldr.append("      \"audio\":");
        bldr.append(audioSummaryStats.getAggregateSumJSON() + ",\n");
        bldr.append("      \"video\":");
        bldr.append(videoSummaryStats.getAggregateSumJSON() + '\n');
        bldr.append("  }\n");


        bldr.append("}\n");
        return bldr.toString();
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
                logger.info("Creating " + this.statsDirectoryPath + " directory");
                saveDir.mkdirs();
            }
        }
    }

    /**
     * Enable or disable the logging of the summary stats computed with all
     * the stats collected by this <tt>HammerStats</tt>.
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

    /**
     * Enable or disable the logging of all the stats collected by this
     * <tt>HammerStats</tt>.
     * @param allStats the boolean that enable of disable the logging.
     */
    public void setOverallStatsLogging(boolean overallStats)
    {
        this.overallStatsLogging = overallStats;
        if(overallStats)
        {
            File saveDir = new File(this.statsDirectoryPath);
            if(saveDir.exists() == false)
            {
                logger.info("Creating " + this.statsDirectoryPath + " directory");
                saveDir.mkdirs();
            }
        }
    }


    private class HammerSummaryStats
    {
        AggregateSummaryStatistics aggregateDownloadRateKiloBitPerSec = new AggregateSummaryStatistics();
        AggregateSummaryStatistics aggregateUploadRateKiloBitPerSec = new AggregateSummaryStatistics();
        AggregateSummaryStatistics aggregateDownloadPercentLoss = new AggregateSummaryStatistics();
        AggregateSummaryStatistics aggregateUploadPercentLoss = new AggregateSummaryStatistics();
        AggregateSummaryStatistics aggregateNbFec = new AggregateSummaryStatistics();
        AggregateSummaryStatistics aggregatePercentDiscarded = new AggregateSummaryStatistics();
        AggregateSummaryStatistics aggregateNbDiscarded = new AggregateSummaryStatistics();
        AggregateSummaryStatistics aggregateNbDiscardedFull = new AggregateSummaryStatistics();
        AggregateSummaryStatistics aggregateNbDiscardedLate = new AggregateSummaryStatistics();
        AggregateSummaryStatistics aggregateNbDiscardedReset = new AggregateSummaryStatistics();
        AggregateSummaryStatistics aggregateNbDiscardedShrink = new AggregateSummaryStatistics();
        AggregateSummaryStatistics aggregateJitterBufferDelayMs = new AggregateSummaryStatistics();
        AggregateSummaryStatistics aggregatePacketQueueCountPackets = new AggregateSummaryStatistics();
        AggregateSummaryStatistics aggregatePacketQueueSize = new AggregateSummaryStatistics();
        AggregateSummaryStatistics aggregateRttMs = new AggregateSummaryStatistics();
        AggregateSummaryStatistics aggregateDownloadJitterMs = new AggregateSummaryStatistics();
        AggregateSummaryStatistics aggregateUploadJitterMs = new AggregateSummaryStatistics();


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

        public HammerSummaryStats()
        {
            clear();
        }

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
            downloadRateKiloBitPerSec =
                aggregateDownloadRateKiloBitPerSec.createContributingStatistics();
            uploadRateKiloBitPerSec =
                aggregateUploadRateKiloBitPerSec.createContributingStatistics();
            downloadPercentLoss =
                aggregateDownloadPercentLoss.createContributingStatistics();
            uploadPercentLoss =
                aggregateUploadPercentLoss.createContributingStatistics();
            nbFec =
                aggregateNbFec.createContributingStatistics();
            percentDiscarded =
                aggregatePercentDiscarded.createContributingStatistics();
            nbDiscarded =
                aggregateNbDiscarded.createContributingStatistics();
            nbDiscardedFull =
                aggregateNbDiscardedFull.createContributingStatistics();
            nbDiscardedLate =
                aggregateNbDiscardedLate.createContributingStatistics();
            nbDiscardedReset =
                aggregateNbDiscardedReset.createContributingStatistics();
            nbDiscardedShrink =
                aggregateNbDiscardedShrink.createContributingStatistics();
            jitterBufferDelayMs =
                aggregateJitterBufferDelayMs.createContributingStatistics();
            packetQueueCountPackets =
                aggregatePacketQueueCountPackets.createContributingStatistics();
            packetQueueSize =
                aggregatePacketQueueSize.createContributingStatistics();
            rttMs =
                aggregateRttMs.createContributingStatistics();
            downloadJitterMs =
                aggregateDownloadJitterMs.createContributingStatistics();
            uploadJitterMs =
                aggregateUploadJitterMs.createContributingStatistics();
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

        public String getAggregateMaxJSON()
        {
            String str = String.format(FakeUserStats.jsonMediaStreamStatsTemplate,
                -1, //ssrc not needed here
                aggregateDownloadRateKiloBitPerSec.getMax(),
                aggregateUploadRateKiloBitPerSec.getMax(),
                aggregateDownloadPercentLoss.getMax(),
                aggregateUploadPercentLoss.getMax(),
                aggregateNbFec.getMax(),
                aggregatePercentDiscarded.getMax(),
                aggregateNbDiscarded.getMax(),
                aggregateNbDiscardedFull.getMax(),
                aggregateNbDiscardedLate.getMax(),
                aggregateNbDiscardedReset.getMax(),
                aggregateNbDiscardedShrink.getMax(),
                aggregateJitterBufferDelayMs.getMax(),
                aggregatePacketQueueCountPackets.getMax(),
                aggregatePacketQueueSize.getMax(),
                aggregateRttMs.getMax(),
                aggregateDownloadJitterMs.getMax(),
                aggregateUploadJitterMs.getMax());
            return str;
        }

        public String getAggregateMeanJSON()
        {
            String str = String.format(FakeUserStats.jsonMediaStreamStatsTemplate,
                -1, //ssrc not needed here
                aggregateDownloadRateKiloBitPerSec.getMean(),
                aggregateUploadRateKiloBitPerSec.getMean(),
                aggregateDownloadPercentLoss.getMean(),
                aggregateUploadPercentLoss.getMean(),
                aggregateNbFec.getMean(),
                aggregatePercentDiscarded.getMean(),
                aggregateNbDiscarded.getMean(),
                aggregateNbDiscardedFull.getMean(),
                aggregateNbDiscardedLate.getMean(),
                aggregateNbDiscardedReset.getMean(),
                aggregateNbDiscardedShrink.getMean(),
                aggregateJitterBufferDelayMs.getMean(),
                aggregatePacketQueueCountPackets.getMean(),
                aggregatePacketQueueSize.getMean(),
                aggregateRttMs.getMean(),
                aggregateDownloadJitterMs.getMean(),
                aggregateUploadJitterMs.getMean());
            return str;
        }

        public String getAggregateMinJSON()
        {
            String str = String.format(FakeUserStats.jsonMediaStreamStatsTemplate,
                -1, //ssrc not needed here
                aggregateDownloadRateKiloBitPerSec.getMin(),
                aggregateUploadRateKiloBitPerSec.getMin(),
                aggregateDownloadPercentLoss.getMin(),
                aggregateUploadPercentLoss.getMin(),
                aggregateNbFec.getMin(),
                aggregatePercentDiscarded.getMin(),
                aggregateNbDiscarded.getMin(),
                aggregateNbDiscardedFull.getMin(),
                aggregateNbDiscardedLate.getMin(),
                aggregateNbDiscardedReset.getMin(),
                aggregateNbDiscardedShrink.getMin(),
                aggregateJitterBufferDelayMs.getMin(),
                aggregatePacketQueueCountPackets.getMin(),
                aggregatePacketQueueSize.getMin(),
                aggregateRttMs.getMin(),
                aggregateDownloadJitterMs.getMin(),
                aggregateUploadJitterMs.getMin());
            return str;
        }

        public String getAggregateStandardDeviationJSON()
        {
            String str = String.format(FakeUserStats.jsonMediaStreamStatsTemplate,
                -1, //ssrc not needed here
                aggregateDownloadRateKiloBitPerSec.getStandardDeviation(),
                aggregateUploadRateKiloBitPerSec.getStandardDeviation(),
                aggregateDownloadPercentLoss.getStandardDeviation(),
                aggregateUploadPercentLoss.getStandardDeviation(),
                aggregateNbFec.getStandardDeviation(),
                aggregatePercentDiscarded.getStandardDeviation(),
                aggregateNbDiscarded.getStandardDeviation(),
                aggregateNbDiscardedFull.getStandardDeviation(),
                aggregateNbDiscardedLate.getStandardDeviation(),
                aggregateNbDiscardedReset.getStandardDeviation(),
                aggregateNbDiscardedShrink.getStandardDeviation(),
                aggregateJitterBufferDelayMs.getStandardDeviation(),
                aggregatePacketQueueCountPackets.getStandardDeviation(),
                aggregatePacketQueueSize.getStandardDeviation(),
                aggregateRttMs.getStandardDeviation(),
                aggregateDownloadJitterMs.getStandardDeviation(),
                aggregateUploadJitterMs.getStandardDeviation());
            return str;
        }

        public String getAggregateSumJSON()
        {
            String str = String.format(FakeUserStats.jsonMediaStreamStatsTemplate,
                -1, //ssrc not needed here
                aggregateDownloadRateKiloBitPerSec.getSum(),
                aggregateUploadRateKiloBitPerSec.getSum(),
                aggregateDownloadPercentLoss.getSum(),
                aggregateUploadPercentLoss.getSum(),
                aggregateNbFec.getSum(),
                aggregatePercentDiscarded.getSum(),
                aggregateNbDiscarded.getSum(),
                aggregateNbDiscardedFull.getSum(),
                aggregateNbDiscardedLate.getSum(),
                aggregateNbDiscardedReset.getSum(),
                aggregateNbDiscardedShrink.getSum(),
                aggregateJitterBufferDelayMs.getSum(),
                aggregatePacketQueueCountPackets.getSum(),
                aggregatePacketQueueSize.getSum(),
                aggregateRttMs.getSum(),
                aggregateDownloadJitterMs.getSum(),
                aggregateUploadJitterMs.getSum());
            return str;
        }

        public String getAggregateVarianceJSON()
        {
            String str = String.format(FakeUserStats.jsonMediaStreamStatsTemplate,
                -1, //ssrc not needed here
                aggregateDownloadRateKiloBitPerSec.getVariance(),
                aggregateUploadRateKiloBitPerSec.getVariance(),
                aggregateDownloadPercentLoss.getVariance(),
                aggregateUploadPercentLoss.getVariance(),
                aggregateNbFec.getVariance(),
                aggregatePercentDiscarded.getVariance(),
                aggregateNbDiscarded.getVariance(),
                aggregateNbDiscardedFull.getVariance(),
                aggregateNbDiscardedLate.getVariance(),
                aggregateNbDiscardedReset.getVariance(),
                aggregateNbDiscardedShrink.getVariance(),
                aggregateJitterBufferDelayMs.getVariance(),
                aggregatePacketQueueCountPackets.getVariance(),
                aggregatePacketQueueSize.getVariance(),
                aggregateRttMs.getVariance(),
                aggregateDownloadJitterMs.getVariance(),
                aggregateUploadJitterMs.getVariance());
            return str;
        }
    }
}
