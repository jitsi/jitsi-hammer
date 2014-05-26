package org.jitsi.hammer;

import org.jivesoftware.smack.*;

import org.jitsi.service.libjitsi.*;

//import java.util.*;



public class Hammer {
    protected String username;
    
    protected HostInfo serverInfo;
    
    /**
     * The locations of the OSGi bundles (or rather of the class files of their
     * <tt>BundleActivator</tt> implementations) comprising Jitsi Hammer.
     * An element of the <tt>BUNDLES</tt> array is an array of <tt>String</tt>s
     * and represents an OSGi start level.
     */
    /*
    private static final String[][] BUNDLES
        = {
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
                "org/jitsi/hammer/HammerBundleActivator"
            }
        };
*/
    
    JingleSession sessions[] = null;

    public Hammer(HostInfo host, String username, int numberOfUser)
    {
        this.username = username;
        this.serverInfo = host;
        sessions = new JingleSession[numberOfUser];
        
        for(int i = 0; i<numberOfUser; i++)
        {
            sessions[i] = new JingleSession(host,username+"_"+i);    
        }
    }

    public void start() {
        // TODO Setting up OSGI
        LibJitsi.start();
    }
    
    public void startStream() {
        // TODO
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

