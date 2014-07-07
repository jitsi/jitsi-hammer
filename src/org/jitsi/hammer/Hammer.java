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
import org.jitsi.service.libjitsi.LibJitsi;

import org.jitsi.hammer.extension.*;
import org.jitsi.hammer.neomedia.*;

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
     * The base of the username use by all the virtual users this Hammer
     * will create.
     */
    private String username;
    
    /**
     * The information about the XMPP server to which all virtual users will
     * try to connect.
     */
    private HostInfo serverInfo;
    
    
    /**
     * The <tt>org.osgi.framework.launch.Framework</tt> instance which
     * represents the OSGi instance launched by this <tt>ComponentImpl</tt>.
     */
    private Framework framework;
    
    /**
     * The <tt>Object</tt> which synchronizes the access to {@link #framework}.
     */
    private final Object frameworkSyncRoot = new Object();
    
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
            }/*,
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
     * The array containing all the <tt>JingleSession</tt> that this Hammer
     * handle, representing all the virtual user that will connect to the XMPP
     * server and start MediaStream with its jitsi-videobridge
     */
    private JingleSession sessions[] = null;
    
    

    /**
     * Instantiate a <tt>Hammer</tt> object with <tt>numberOfUser</tt> virtual
     * users that will try to connect to the XMPP server and its videobridge
     * contained in <tt>host</tt>.
     * 
     * @param host The information about the XMPP server to which all
     * virtual users will try to connect.
     * @param username The base of the username used by all the virtual users.
     * @param numberOfUser The number of virtual users this <tt>Hammer</tt>
     * will create and handle.
     */
    public Hammer(HostInfo host, String username, int numberOfUser)
    {
        this.username = username;
        this.serverInfo = host;
        sessions = new JingleSession[numberOfUser];
        
        for(int i = 0; i<numberOfUser; i++)
        {
            sessions[i] = new JingleSession(this.serverInfo,this.username+"_"+i);    
        }
    }



    /**
     * Initialize the Hammer by launching the OSGi Framework and
     * installing/registering the needed bundle (LibJitis and more..).
     */
    public void init()
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
            if (this.framework != null)
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
            
            /*
             * Call to getMediaService to initialize the MediaService
             * implementation so that the configuration for the
             * fmj registry are correctly set 
             * before I write my capture devices in it.
             */
            LibJitsi.getMediaService();
            FMJPluginInConfiguration.registerCustomPackages();
        }
        catch (BundleException be)
        {
            throw new RuntimeException(be);
        }

        synchronized (frameworkSyncRoot)
        {
            this.framework = framework;
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
     */
    public void start(int wait) {
        //if(wait <= 0) wait = 1;
        
        try
        {
            for(JingleSession session : sessions)
            {
                session.start();
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
    }
}

