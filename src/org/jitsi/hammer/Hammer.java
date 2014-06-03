package org.jitsi.hammer;

import java.util.HashMap;
import java.util.Map;



/*
//XXX remove these import when you remove the print below in init().
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.MediaType;
import org.jitsi.service.neomedia.MediaUseCase;
*/

import org.jivesoftware.smack.*;
import org.osgi.framework.*;
import org.osgi.framework.launch.*;
import org.osgi.framework.startlevel.*;
import net.java.sip.communicator.impl.osgi.framework.launch.*;


//import java.util.*;



public class Hammer {
    private String username;
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
     * The locations of the OSGi bundles (or rather of the class files of their
     * <tt>BundleActivator</tt> implementations) comprising Jitsi Hammer.
     * An element of the <tt>BUNDLES</tt> array is an array of <tt>String</tt>s
     * and represents an OSGi start level.
     */
    private static final String[][] BUNDLES =
        {
            {
                "net/java/sip/communicator/impl/libjitsi/LibJitsiActivator"
            },
            {
                "net/java/sip/communicator/util/UtilActivator",
                "net/java/sip/communicator/impl/fileaccess/FileAccessActivator"
            },
            {
                "net/java/sip/communicator/impl/configuration/ConfigurationActivator"
            },
            /*{
                "net/java/sip/communicator/impl/resources/ResourceManagementActivator"
            },
            {
                "net/java/sip/communicator/util/dns/DnsUtilActivator"
            },
            {
                "net/java/sip/communicator/impl/netaddr/NetaddrActivator"
            },
            /*{
                "net/java/sip/communicator/impl/packetlogging/PacketLoggingActivator"
            },*/
            /*{
                "net/java/sip/communicator/service/gui/internal/GuiServiceActivator"
            },
            {
                "net/java/sip/communicator/service/protocol/media/ProtocolMediaActivator"
            },
            /*{
                "org/jitsi/hammer/HammerActivator"
            }*/
        };

    
    JingleSession sessions[] = null;

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


    /*
     * This code is a slightly modified copy of the one found in
     * startOSGi of the class ComponentImpl of jitsi-videobridge.
     * 
     * This function run the activation of different bundle that are needed
     * These bundle are the one found in the BUNDLE array
     */
    public void init()
    {
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
        }
        catch (BundleException be)
        {
            throw new RuntimeException(be);
        }

        synchronized (frameworkSyncRoot)
        {
            this.framework = framework;
        }
        /*
        ConfigurationService config = LibJitsi.getConfigurationService();
        System.out.println(config.getConfigurationFilename());
        System.out.println(config.getScHomeDirLocation());
        System.out.println(config.getScHomeDirName());
        
        for(MediaDevice device : LibJitsi.getMediaService().getDevices(MediaType.AUDIO, MediaUseCase.ANY))
        {
            System.out.println(device.getDirection());
            for(MediaFormat type : device.getSupportedFormats())
            {
                System.out.println(type.toString());
            }
        }
        System.out.println("\n\n");
        for(MediaDevice device : LibJitsi.getMediaService().getDevices(MediaType.VIDEO, MediaUseCase.ANY))
        {
            System.out.println(device.getDirection());
            for(MediaFormat type : device.getSupportedFormats())
            {
                System.out.println(type.toString());
            }
        }*/
    }
    
    public void start() {
        try
        {
            for(JingleSession session : sessions)
            {
                session.start();
            }
        }
        catch (XMPPException e)
        {
            e.printStackTrace();
        }
    }
}

