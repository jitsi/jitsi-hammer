/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jitsi.hammer;



import org.osgi.framework.*;
import org.osgi.framework.launch.*;
import org.osgi.framework.startlevel.*;
import org.jivesoftware.smack.*;
import org.jivesoftware.smack.provider.*;

import net.java.sip.communicator.impl.osgi.framework.launch.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.*;

import org.jitsi.hammer.extension.*;
import org.jitsi.hammer.stats.HammerStats;
import org.jitsi.hammer.utils.MediaDeviceChooser;

import java.util.*;

/**
 *
 * @author Thomas Kuntz
 *
 * The <tt>Hammer</tt> class is the core class of the jitsi-hammer project.
 * This class will try to create N virtual users to a XMPP server then to
 * a MUC chatroom created by JitMeet (https://jitsi.org/Projects/JitMeet).
 *
 * Each virtual user after succeeding in the connection to the JitMeet MUC
 * receive an invitation to a audio/video conference. After receiving the
 * invitation, each virtual user will positively reply to the invitation and
 * start sending audio and video data to the jitsi-videobridge handling the
 * conference.
 */
public class Hammer {
    /**
     * The base of the nickname use by all the virtual users this Hammer
     * will create.
     */
    private final String nickname;

    /**
     * The information about the XMPP server to which all virtual users will
     * try to connect.
     */
    private final HostInfo serverInfo;

    /**
     * The <tt>MediaDeviceChooser</tt> that will be used by all the
     * <tt>FakeUser</tt> to choose their <tt>MediaDevice</tt>
     */
    private final MediaDeviceChooser mediaDeviceChooser;


    /**
     * The <tt>org.osgi.framework.launch.Framework</tt> instance which
     * represents the OSGi instance launched by this <tt>ComponentImpl</tt>.
     */
    private static Framework framework;

    /**
     * The <tt>Object</tt> which synchronizes the access to {@link #framework}.
     */
    private static final Object frameworkSyncRoot = new Object();

    /**
     * The locations of the OSGi bundles (or rather of the path of the class
     * files of their <tt>BundleActivator</tt> implementations).
     * An element of the <tt>BUNDLES</tt> array is an array of <tt>String</tt>s
     * and represents an OSGi start level.
     */
    private static final String[][] BUNDLES =
    {

        {
            "net/java/sip/communicator/impl/libjitsi/LibJitsiActivator"
        }
        //These bundles are used in Jitsi-Videobridge from which I copied
        //some code, but these bundle doesn't seems necessary for the hammer
        /*,
            {
                "net/java/sip/communicator/util/UtilActivator",
                "net/java/sip/communicator/impl/fileaccess/FileAccessActivator"
            },
            {
                "net/java/sip/communicator/impl/configuration/ConfigurationActivator"
            },
            {
                "net/java/sip/communicator/impl/resources/ResourceManagementActivator"
            },
            {
                "net/java/sip/communicator/util/dns/DnsUtilActivator"
            },
            {
                "net/java/sip/communicator/impl/netaddr/NetaddrActivator"
            },
            {
                "net/java/sip/communicator/impl/packetlogging/PacketLoggingActivator"
            },
            {
                "net/java/sip/communicator/service/gui/internal/GuiServiceActivator"
            },
            {
                "net/java/sip/communicator/service/protocol/media/ProtocolMediaActivator"
            },
            {
                "org/jitsi/hammer/HammerActivator"
            }*/
    };

    /**
     * The array containing all the <tt>FakeUser</tt> that this Hammer
     * handle, representing all the virtual user that will connect to the XMPP
     * server and start MediaStream with its jitsi-videobridge
     */
    private FakeUser fakeUsers[] = null;

    /**
     * The <tt>HammerStats/tt> that will be used by this <tt>Hammer</tt>
     * to keep track of the streams' stats of all the <tt>FakeUser</tt>
     * etc..
     */
    private final HammerStats hammerStats = new HammerStats();

    /**
     * The thread that run the <tt>HammerStats</tt> of this <tt>Hammer</tt>
     */
    private Thread hammerStatsThread;


    /**
     * Instantiate a <tt>Hammer</tt> object with <tt>numberOfUser</tt> virtual
     * users that will try to connect to the XMPP server and its videobridge
     * contained in <tt>host</tt>.
     *
     * @param host The information about the XMPP server to which all
     * virtual users will try to connect.
     * @param mdc
     * @param nickname The base of the nickname used by all the virtual users.
     * @param numberOfUser The number of virtual users this <tt>Hammer</tt>
     * will create and handle.
     */
    public Hammer(HostInfo host, MediaDeviceChooser mdc, String nickname, int numberOfUser)
    {
        this.nickname = nickname;
        this.serverInfo = host;
        this.mediaDeviceChooser = mdc;
        fakeUsers = new FakeUser[numberOfUser];

        for(int i = 0; i<fakeUsers.length; i++)
        {
            fakeUsers[i] = new FakeUser(
                this.serverInfo,
                this.mediaDeviceChooser,
                this.nickname+"_"+i);
        }
    }



    /**
     * Initialize the Hammer by launching the OSGi Framework and
     * installing/registering the needed bundle (LibJitis and more..).
     */
    public static void init()
    {
        /**
         * This code is a slightly modified copy of the one found in
         * startOSGi of the class ComponentImpl of jitsi-videobridge.
         *
         * This function run the activation of different bundle that are needed
         * These bundle are the one found in the <tt>BUNDLE</tt> array
         */
        synchronized (frameworkSyncRoot)
        {
            if (Hammer.framework != null)
                return;
        }

        FrameworkFactory frameworkFactory = new FrameworkFactoryImpl();
        Map<String, String> configuration = new HashMap<String, String>();
        BundleContext bundleContext = null;

        configuration.put(
            Constants.FRAMEWORK_BEGINNING_STARTLEVEL,
            Integer.toString(BUNDLES.length));

        Framework framework = frameworkFactory.newFramework(configuration);

        try
        {
            framework.init();

            bundleContext = framework.getBundleContext();

            for (int startLevelMinus1 = 0;
                startLevelMinus1 < BUNDLES.length;
                startLevelMinus1++)
            {
                int startLevel = startLevelMinus1 + 1;

                for (String location : BUNDLES[startLevelMinus1])
                {
                    Bundle bundle = bundleContext.installBundle(location);

                    if (bundle != null)
                    {
                        BundleStartLevel bundleStartLevel
                        = bundle.adapt(BundleStartLevel.class);

                        if (bundleStartLevel != null)
                            bundleStartLevel.setStartLevel(startLevel);
                    }
                }
            }

            framework.start();
        }
        catch (BundleException be)
        {
            throw new RuntimeException(be);
        }

        synchronized (frameworkSyncRoot)
        {
            Hammer.framework = framework;
        }


        ProviderManager manager = ProviderManager.getInstance();
        manager.addExtensionProvider(
            MediaProvider.ELEMENT_NAME,
            MediaProvider.NAMESPACE,
            new MediaProvider());
        manager.addExtensionProvider(
            SsrcProvider.ELEMENT_NAME,
            SsrcProvider.NAMESPACE,
            new SsrcProvider());

        manager.addIQProvider(
            JingleIQ.ELEMENT_NAME,
            JingleIQ.NAMESPACE,
            new JingleIQProvider());
    }

    /**
     * Start the connection of all the virtual user that this <tt>Hammer</tt>
     * handles to the XMPP server(and then a MUC).
     *
     * @param wait the number of milliseconds the Hammer will wait during the
     * start of two consecutive fake users.
     * @param overallStats enable or not the logging of the overall stats
     * computed at the end of the run.
     * @param allStats enable or not the logging of the all the stats collected
     * by the <tt>HammerStats</tt> during the run.
     * @param summaryStats enable or not the logging of the dummary stats
     * computed from all the streams' stats collected by the
     * <tt>HammerStats</tt> during the run.
     * @param statsPollingTime the number of seconds between two polling of stats
     * by the <tt>HammerStats</tt> run method.
     */
    public void start(
        int wait,
        boolean overallStats,
        boolean allStats,
        boolean summaryStats,
        int statsPollingTime)
    {
        if(wait <= 0) wait = 1;

        try
        {
            for(FakeUser user : fakeUsers)
            {
                user.start();
                hammerStats.addFakeUsersStats(user.getFakeUserStats());
                Thread.sleep(wait);
            }
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        hammerStats.setOverallStatsLogging(overallStats);
        hammerStats.setAllStatsLogging(allStats);
        hammerStats.setSummaryStatsLogging(summaryStats);
        hammerStats.setTimeBetweenUpdate(statsPollingTime);
        hammerStatsThread = new Thread(hammerStats);
        hammerStatsThread.start();
    }


    /**
     * Start the connection of all the virtual user that this <tt>Hammer</tt>
     * handles to the XMPP server(and then a MUC), using the <tt>Credential</tt>
     * given as arguments for the login.
     *
     * @param wait the number of milliseconds the Hammer will wait during the
     * start of two consecutive fake users.
     * @param credentials a list of <tt>Credentials</tt> used for the login
     * of the fake users.
     * @param overallStats enable or not the logging of the overall stats
     * computed at the end of the run.
     * @param allStats enable or not the logging of the all the stats collected
     * by the <tt>HammerStats</tt> during the run.
     * @param summaryStats enable or not the logging of the dummary stats
     * computed from all the streams' stats collected by the
     * <tt>HammerStats</tt> during the run.
     * @param b
     * @param statsPollingTime the number of seconds between two polling of stats
     * by the <tt>HammerStats</tt> run method.
     */
    public void start(
        int wait,
        List<Credential> credentials,
        boolean overallStats,
        boolean allStats,
        boolean summaryStats,
        int statsPollingTime)
    {
        if(wait <= 0) wait = 1;

        try
        {
            Iterator<FakeUser> userIt = Arrays.asList(fakeUsers).iterator();
            Iterator<Credential> credIt = credentials.iterator();
            FakeUser user = null;
            Credential credential = null;

            while(credIt.hasNext() && userIt.hasNext())
            {
                user = userIt.next();
                credential = credIt.next();

                user.start(credential.getUsername(),credential.getPassword());
                hammerStats.addFakeUsersStats(user.getFakeUserStats());
                Thread.sleep(wait);
            }
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        hammerStats.setOverallStatsLogging(overallStats);
        hammerStats.setAllStatsLogging(allStats);
        hammerStats.setSummaryStatsLogging(summaryStats);
        hammerStats.setTimeBetweenUpdate(statsPollingTime);
        hammerStatsThread = new Thread(hammerStats);
        hammerStatsThread.start();
    }


    /**
     * Stop the streams of all the fake users created, and disconnect them
     * from the MUC and the XMPP server.
     * Also stop the <tt>HammerStats</tt> thread.
     */
    public void stop()
    {
        for(FakeUser user : fakeUsers)
        {
            user.stop();
        }

        /*
         * Stop the thread of the HammerStats, without using the Thread
         * instance hammerStatsThread, to allow it to cleanly stop.
         */
        hammerStats.stop();
        try
        {
            hammerStatsThread.join();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }


    /**
     * Get the <tt>HammerStats</tt> used by this <tt>Hammer</tt>.
     * @return the <tt>HammerStats</tt> used by this <tt>Hammer</tt>.
     */
    public HammerStats getHammerStats()
    {
        return hammerStats;
    }
}

