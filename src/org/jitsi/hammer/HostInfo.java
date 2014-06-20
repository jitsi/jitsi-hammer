/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jitsi.hammer;

import org.kohsuke.args4j.*;

/**
 * The class contains an number of information about the host server.
 * 
 * All these informations have default value.
 * 
 * This class also use args4j so that these informations can be set as
 * arguments for this program
 *
 * @author Thomas Kuntz
 */
public class HostInfo
{
    /**
     * @Option is used by args4j to know what options can be set as arguments
     * for this program.
     */
    
    /**
     * The domain name of the XMPP server.
     */
    @Option(name="-domain",usage="XMPP domain name")
    private String XMPPdomain = "guest.jit.si";
    
    /**
     * The hostname used by the XMPP server (used to access to the MUC).
     */
    @Option(name="-XMPPhost",usage="XMPP server hostname")
    private String XMPPhost = "meet.jit.si";
    
    /**
     * The jitsi-videobridge hostname related to the XMPP server.
     */
    @Option(name="-bridge",usage="jitsi-videobridge hostname")
    private String bridge = "jitsi-videobridge.lambada.jitsi.net";
    
    
    /**
     * The hostname used by the XMPP server (used to access to the MUC).
     */
    @Option(name="-MUCDomain",usage="MUC domain name")
    private String MUCdomain = "meet.jit.si";
    
    /**
     * The name of the MUC room that we'll use.
     */
    @Option(name="-room",usage="MUC room name")
    private String roomName = "TestHammer";
    
    /**
     * The port used by the XMPP server.
     */
    @Option(name="-port",usage="XMPP port")
    private int port = 5222;

    
    /**
     * Instantiates a new <tt>HostInfo</tt> instance with default attribut. 
     */
    public HostInfo() {}
    
    /**
     * @arg domain the domain name of the XMPP server.
     * @arg host the hostname of the XMPP server
     * @arg bridge the hostname of the jitsi-videobridge of the XMPP server
     * @arg port the port number of the XMPP server
     * @arg roomName the room name used for the MUC
     * Instantiates a new <tt>HostInfo</tt> instance
     * with all the informations needed.
     */
    public HostInfo(String XMPPdomain,String XMPPhost,int port,String bridge,String MUCdomain,String roomName)
    {
        this.XMPPdomain = XMPPdomain;
        this.port = port;
        this.XMPPhost = XMPPhost;
        this.bridge = bridge;
        this.MUCdomain = MUCdomain;
        this.roomName = roomName;
    }
    
    /**
     * Get the domain of the XMPP server of this <tt>HostInfo</tt>
     * (in lower case).
     * @return the domain of the XMPP server (in lower case).
     */
    public String getXMPPDomain()
    {
        return this.XMPPdomain.toLowerCase();
    }
    
    /**
     * Get the domain of the MUC server of this <tt>HostInfo</tt>
     * (in lower case).
     * @return the domain of the MUC server (in lower case).
     */
    public String getMUCDomain()
    {
        return this.MUCdomain.toLowerCase();
    }

    /**
     * Get the hostname of the XMPP server of this <tt>HostInfo</tt>
     * (in lower case).
     * @return the hostname of the XMPP server (in lower case).
     */
    public String getXMPPHostname()
    {
        return this.XMPPhost.toLowerCase();
    }
    
    /**
     * Get the hostname of the jitsi-videobridge of
     * XMPP server of this <tt>HostInfo</tt> (in lower case).
     * @return the hostname of the jitsi-videobridge (in lower case).
     */
    public String getBridge()
    {
        return this.bridge.toLowerCase();
    }
    
    /**
     * Get the room name (to access a MUC) of this <tt>HostInfo</tt>
     * (in lower case).
     * @return the room name of a MUC (in lower case).
     */
    public String getRoomName()
    {
        return this.roomName.toLowerCase();
    }

    /**
     * Get the port number of the XMPP server of this <tt>HostInfo</tt>.
     * @return the port number of the XMPP server.
     */
    public int getPort()
    {
        return this.port;
    }
}
