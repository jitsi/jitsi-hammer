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
     * The hostname used to access the XMPP server via BOSH
     */
    private String BOSHhost;

    /**
     * The hostname used by the XMPP server (used to access to the MUC).
     */
    private String MUCdomain;
    
    /**
     * The name of the MUC room that we'll use.
     */
    private String roomName;
    
    /**
     * The port used by the BOSH server.
     */
    private int port;

    /**
     * The URI where BOSH server listening on
     */
    private String boshURI;

    /**
     * The boolean flag identifying whether to use HTTPS or not
     */
    private boolean useHTTPS;

    /**
     * The XMPP address (JID) for the Focus component
     */
    private String focusJID;

    
    /**
     * Instantiates a new <tt>HostInfo</tt> instance with default attribut. 
     */
    public HostInfo() {}
    
    /**
     * @arg XMPPdomain the domain name of the XMPP server.
     * @arg BOSHhost the hostname of the XMPP server
     * @arg port the port number of the XMPP server
     * @arg MUCdomain the domain of the MUC server
     * @arg roomName the room name used for the MUC
     * Instantiates a new <tt>HostInfo</tt> instance
     * with all the informations needed.
     */
    public HostInfo(
            String XMPPdomain,
            String BOSHhost,
            int port,
            String MUCdomain,
            String roomName,
            String boshURI,
            boolean useHTTPS,
            String focusJID
            )
    {
        this.XMPPdomain = XMPPdomain;
        this.port = port;
        this.BOSHhost = BOSHhost;
        this.MUCdomain = MUCdomain;
        this.roomName = roomName;
        this.boshURI = boshURI;
        this.useHTTPS = useHTTPS;
        this.focusJID = focusJID;
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
    public String getBOSHhostname()
    {
        return this.BOSHhost.toLowerCase();
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

    /**
     * Get the URI where BOSH server is listening
     * @return the URI where BOSH server is listening
     */
    public String getBOSHuri()
    {
        return this.boshURI;
    }

    /**
     * Get the boolean flag identifying whether to use HTTPS or just plain HTTP
     * @return the boolean flag identifying whether to use HTTPS
     *          or just plain HTTP
     */
    public boolean getUseHTTPS()
    {
        return this.useHTTPS;
    }

    /**
     * Get the focus JID
     */
    public String getFocusJID() 
    {
        return this.focusJID;    
    }

    /**
     * Construct the conference URL (JID) for the targeted conference 
     * using the <tt>HostInfo</tt> associated with 
     * the object
     *
     * @return URL for the corresponding conference
     */
    public String getRoomURL()
    {
        return this.roomName + "@" + this.MUCdomain;
    }
    
}
