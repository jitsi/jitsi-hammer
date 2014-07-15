/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jitsi.hammer;

/**
 * The class contains an number of information about the host server.
 *
 * @author Thomas Kuntz
 */
public class HostInfo
{
    
    /**
     * The domain name of the XMPP server.
     */
    private String XMPPdomain;
    
    /**
     * The hostname used to access the XMPP server.
     */
    private String XMPPhost;
    
    
    /**
     * The hostname used by the XMPP server (used to access to the MUC).
     */
    private String MUCdomain;
    
    /**
     * The name of the MUC room that we'll use.
     */
    private String roomName;
    
    /**
     * The port used by the XMPP server.
     */
    private int port;

    
    /**
     * Instantiates a new <tt>HostInfo</tt> instance with default attribut. 
     */
    public HostInfo() {}
    
    /**
     * @arg XMPPdomain the domain name of the XMPP server.
     * @arg XMPPhost the hostname of the XMPP server
     * @arg port the port number of the XMPP server
     * @arg MUCdomain the domain of the MUC server
     * @arg roomName the room name used for the MUC
     * Instantiates a new <tt>HostInfo</tt> instance
     * with all the informations needed.
     */
    public HostInfo(
            String XMPPdomain,
            String XMPPhost,
            int port,
            String MUCdomain,
            String roomName)
    {
        this.XMPPdomain = XMPPdomain;
        this.port = port;
        this.XMPPhost = XMPPhost;
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
