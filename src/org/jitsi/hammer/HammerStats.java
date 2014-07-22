/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jitsi.hammer;


import java.io.*;
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
     * An <tt>List</tt> that contains the <tt>MediaStreamStats</tt> of all the
     * <tt>AudioMediaStream<tt> this class keeps track of.
     */
    private final ArrayList<MediaStreamStats> audioStreamsStats =
        new ArrayList<MediaStreamStats>();

    /**
     * An <tt>List</tt> that contains the <tt>MediaStreamStats</tt> of all the
     * <tt>VideoMediaStream<tt> this class keeps track of.
     */
    private final ArrayList<MediaStreamStats> videoStreamsStats =
        new ArrayList<MediaStreamStats>();

    /**
     * The time (in seconds) the HammerStats wait between two updates.
     */
    private int timeBetweenUpdate = 2;

    /**
     * Initialize an instance of a <tt>HammerStats</tt> with the default
     * stats directory path.
     */
    public HammerStats()
    {
        this( System.getProperty(Main.PNAME_SC_HOME_DIR_LOCATION)
            + File.separator
            + System.getProperty(Main.PNAME_SC_HOME_DIR_NAME)
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
        this.statsDirectoryPath = statsDirectoryPath;
        new File(this.statsDirectoryPath).mkdirs();
    }


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

    /**
     * Keep track, collect and update the stats of all the
     * <tt>MediaStreamStats</tt> this <tt>HammerStats</tt> handles.
     * 
     * Also write the results in the stats files.
     */
    public void run()
    {
        Iterator<MediaStreamStats> audioStatsIterator = null;
        Iterator<MediaStreamStats> videoStatsIterator = null;
        MediaStreamStats audioStats = null;
        MediaStreamStats videoStats = null;

        synchronized(this)
        {
            threadStop = false;
        }


        while(threadStop == false)
        {
            synchronized(this)
            {
                //We get the iterator for both ListArray
                audioStatsIterator = audioStreamsStats.iterator();
                videoStatsIterator = videoStreamsStats.iterator();

                //While there is a MediaStreamStats not updated and collected
                while(audioStatsIterator.hasNext() && videoStatsIterator.hasNext())
                {
                    audioStats = audioStatsIterator.next();
                    videoStats = videoStatsIterator.next();

                    //We update the stats before using/reading them.
                    audioStats.updateStats();
                    videoStats.updateStats();

                    //TODO read/collect the stats we want to keep track.
                    //Write them to the stats file
                    //Maybe compute some things, like average or whatenot
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
     * Print the overall stats of the <tt>MediaStream</tt> this
     * <tt>MediaStreamStats</tt> keep track.
     */
    public void printOverallStats()
    {
        //TODO print
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
}
