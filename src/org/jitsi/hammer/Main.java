/*
 * Copyright @ 2015 Atlassian Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jitsi.hammer;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.util.List;

import net.java.sip.communicator.launcher.ChangeJVMFrame;
//import net.java.sip.communicator.util.ScStdOut;




import org.jitsi.hammer.utils.*;
import org.kohsuke.args4j.*;

/**
 *
 * @author Thomas Kuntz
 *
 * This class contains the Main method used to launch jitsi-hammer.
 * A lot of code is copied from the SIPCommunicator.java Main
 * method, because jitsi-hammer use a lot of the same configuration that Jitsi.
 */
public class Main
{
    /**
     * The name of the property that stores the home dir for cache data, such
     * as avatars and spelling dictionaries.
     */
    public static final String PNAME_SC_CACHE_DIR_LOCATION =
        "net.java.sip.communicator.SC_CACHE_DIR_LOCATION";

    /**
     * The name of the property that stores the home dir for application log
     * files (not history).
     */
    public static final String PNAME_SC_LOG_DIR_LOCATION =
        "net.java.sip.communicator.SC_LOG_DIR_LOCATION";

    /**
     * The name of the property that stores our home dir location.
     */
    public static final String PNAME_SC_HOME_DIR_LOCATION
    = "net.java.sip.communicator.SC_HOME_DIR_LOCATION";

    /**
     * The name of the property that stores our home dir name.
     */
    public static final String PNAME_SC_HOME_DIR_NAME
    = "net.java.sip.communicator.SC_HOME_DIR_NAME";


    /**
     * Sets the system properties net.java.sip.communicator.SC_HOME_DIR_LOCATION
     * and net.java.sip.communicator.SC_HOME_DIR_NAME (if they aren't already
     * set) in accord with the OS conventions specified by the name of the OS.
     *
     * Please leave the access modifier as package (default) to allow launch-
     * wrappers to call it.
     *
     * @param osName the name of the OS according to which the SC_HOME_DIR_*
     * properties are to be set
     */
    static void setScHomeDir(String osName)
    {
        /*
         * Though we'll be setting the SC_HOME_DIR_* property values depending
         * on the OS running the application, we have to make sure we are
         * compatible with earlier releases i.e. use
         * ${user.home}/.sip-communicator if it exists (and the new path isn't
         * already in use).
         */
        String profileLocation = System.getProperty(PNAME_SC_HOME_DIR_LOCATION);
        String name = System.getProperty(PNAME_SC_HOME_DIR_NAME);

        if (profileLocation == null
            || name == null)
        {
            String defaultLocation = System.getProperty("user.home");
            String defaultName = ".Jitsi-Hammer";

            if (osName.startsWith("Mac"))
            {
                if (profileLocation == null)
                    profileLocation =
                    System.getProperty("user.home") + File.separator
                    + "Library" + File.separator
                    + "Application Support";

                if (name == null)
                    name = "Jitsi-Hammer";
            }
            else if (osName.startsWith("Windows"))
            {
                /*
                 * Primarily important on Vista because Windows Explorer opens
                 * in %USERPROFILE% so .sip-communicator is always visible. But
                 * it may be a good idea to follow the OS recommendations and
                 * use APPDATA on pre-Vista systems as well.
                 */
                if (profileLocation == null)
                    profileLocation = System.getenv("APPDATA");
                if (name == null)
                    name = "Jitsi-Hammer";
            }

            /* If there're no OS specifics, use the defaults. */
            if (profileLocation == null)
                profileLocation = defaultLocation;
            if (name == null)
                name = defaultName;

            System.setProperty(PNAME_SC_HOME_DIR_LOCATION, profileLocation);
            System.setProperty(PNAME_SC_HOME_DIR_NAME, name);
        }

        // when we end up with the home dirs, make sure we have log dir
        new File(new File(profileLocation, name), "log").mkdirs();

        // Make libjitsi treat the configuration file as read-only
        System.setProperty(
                "net.java.sip.communicator.CONFIGURATION_FILE_IS_READ_ONLY",
                "true");
    }


    /**
     * Sets some system properties specific to the OS that needs to be set at
     * the very beginning of a program (typically for UI related properties,
     * before AWT is launched).
     *
     * @param osName OS name
     */
    private static void setSystemProperties(String osName)
    {
        // setup here all system properties that need to be initialized at
        // the very beginning of an application
        if(osName.startsWith("Windows"))
        {
            // disable Direct 3D pipeline (used for fullscreen) before
            // displaying anything (frame, ...)
            System.setProperty("sun.java2d.d3d", "false");
        }
        else if(osName.startsWith("Mac"))
        {
            // On Mac OS X when switch in fullscreen, all the monitors goes
            // fullscreen (turns black) and only one monitors has images
            // displayed. So disable this behavior because somebody may want
            // to use one monitor to do other stuff while having other ones with
            // fullscreen stuff.
            System.setProperty("apple.awt.fullscreencapturealldisplays",
                "false");
        }
    }


    public static void main(String[] args)
        throws InterruptedException
    {
        String version = System.getProperty("java.version");
        String vmVendor = System.getProperty("java.vendor");
        String osName = System.getProperty("os.name");

        setSystemProperties(osName);
        setScHomeDir(osName);

        // this needs to be set before any DNS lookup is run
        File f
        = new File(
            System.getProperty(PNAME_SC_HOME_DIR_LOCATION),
            System.getProperty(PNAME_SC_HOME_DIR_NAME)
            + File.separator
            + ".usednsjava");
        if(f.exists())
        {
            System.setProperty(
                "sun.net.spi.nameservice.provider.1",
                "dns,dnsjava");
        }

        if (version.startsWith("1.4") || vmVendor.startsWith("Gnu") ||
            vmVendor.startsWith("Free"))
        {
            String os = "";

            if (osName.startsWith("Mac"))
                os = ChangeJVMFrame.MAC_OSX;
            else if (osName.startsWith("Linux"))
                os = ChangeJVMFrame.LINUX;
            else if (osName.startsWith("Windows"))
                os = ChangeJVMFrame.WINDOWS;

            ChangeJVMFrame changeJVMFrame = new ChangeJVMFrame(os);
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

            changeJVMFrame.setLocation(
                screenSize.width/2 - changeJVMFrame.getWidth()/2,
                screenSize.height/2 - changeJVMFrame.getHeight()/2);
            changeJVMFrame.setVisible(true);

            return;
        }


        /*
         ************************************************************
         * This is the beginning of jitsi-hammer, before was
         * libjitsi configuration
         ************************************************************
         */

        //java.util.logging.Logger l = java.util.logging.Logger.getLogger("");
        //l.setLevel(java.util.logging.Level.WARNING);

        CmdLineArguments infoCLI = new CmdLineArguments();
        CmdLineParser parser = new CmdLineParser(infoCLI);
        try
        {
            parser.parseArgument(args);
        }
        catch(CmdLineException e)
        {
            if(infoCLI.getHelpOption())
            {
                System.out.println("Required options of the program :");

                System.out.println("-XMPPdomain , -XMPPhost , -MUCdomain\n");
            }
            else
            {
                System.out.println(e.getMessage() + '\n');
            }
            System.out.println("Jitsi-Hammer options usage :");
            parser.printUsage(System.out);
            System.exit(1);
        }
        if(infoCLI.getHelpOption())
        {
            System.out.println("Required options of the program :");
            System.out.println("-XMPPdomain , -XMPPhost , -MUCDomain\n");
            System.out.println("Jitsi-Hammer options usage :");
            parser.printUsage(System.out);
            System.exit(1);
        }

        //We call initialize the Hammer (registering OSGi bundle for example)
        Hammer.init();

        HostInfo hostInfo = infoCLI.getHostInfoFromArguments();
        MediaDeviceChooser mdc = infoCLI.getMediaDeviceChooser();

        int numberOfFakeUsers = infoCLI.getNumberOfFakeUsers();
        List<Credential> credentials = infoCLI.getCredentialsList();
        if(credentials.size() > 0) numberOfFakeUsers = credentials.size();


        final Hammer hammer = new Hammer(
            hostInfo,
            mdc,
            "Jitsi-Hammer",
            numberOfFakeUsers);


        //Cleanly stop the hammer when the program shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable()
        {
            public void run()
            {
                System.out.println("Stopping Jitsi-Hammer...");

                hammer.stop();

                System.out.println("Exiting the program...");
            }
        }));


        //After the initialization we start the Hammer (all its users will
        //connect to the XMPP server and try to setup media stream with it bridge
        hammer.start(
                infoCLI.getInterval(),
                infoCLI.getDisableStats(),
                (credentials.size() > 0) ? credentials : null,
                infoCLI.getOverallStats(),
                infoCLI.getAllStats(),
                infoCLI.getSummaryStats(),
                infoCLI.getStatsPolling());

        if(infoCLI.getRunLength() > 0)
        {
            Thread.sleep(infoCLI.getRunLength() * 1000);
        }
        else
        {
            while(true) Thread.sleep(3600000);
        }

        //It's necessary to finish Main() with an exit, to trigger the shutdown
        //hook that will stop the hammer
        System.exit(0);
    }
}
