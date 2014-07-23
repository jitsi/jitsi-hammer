/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jitsi.hammer.stats;


import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import org.jitsi.hammer.Main;

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
            + new SimpleDateFormat("yyyy-MM-dd'_'HH.mm.ss").format(new Date());
        new File(this.statsDirectoryPath).mkdirs();

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
        String delim;
        String delim_ = "";
        synchronized(this)
        {
            threadStop = false;
        }


        try
        {
            writer = new PrintWriter(allStatsFile, "UTF-8");
            writer.println("[");
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }

        while(threadStop == false)
        {
            writer.println(delim_);
            delim_ = ",";
            synchronized(this)
            {
                writer.println("{");
                writer.println("  \"timestamp\":" + System.currentTimeMillis()+",");
                writer.println("  \"users\":");
                writer.println("  [");
                delim = "";
                for(FakeUserStats stats : fakeUserStatsList)
                {
                    //We update the stats before using/reading them.
                    stats.updateStats();

                    writer.println(delim + stats.getStatsJSON(2));
                    delim = ",";

                    //TODO read/collect the stats we want to keep track.
                    //Write them to the stats file
                    //Maybe compute some things, like average or whatenot
                }
                writer.println("  ]");
                writer.append("}");
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

        writer.println("]");
        writer.close();
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
        try
        {
            PrintWriter writer = new PrintWriter(overallStatsFile, "UTF-8");
            writer.println(getOverallStats());
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
}
