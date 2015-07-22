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
    private final String XMPPdomain;
    
    /**
     * The hostname used to access the XMPP server.
     */
    private final String XMPPhost;
    
    
    /**
     * The hostname used by the XMPP server (used to access to the MUC).
     */
    private final String MUCdomain;

    
    
    /**
     * The name of the MUC room that we'll use.
     */
    private final String roomName;
    
    /**
     * The port used by the XMPP server.
     */
    private final int port;

    /**
     * The jid (jitsi-id) to pass to the MUC, assumed to have permissions to create/destroy rooms.
     */
    private final String focusUserJid;

    /**
     * The name of the videobridge to send to the MUC server, when creating rooms
     */
    private final String MUCvideobridge;
    
    /**
     * Instantiates a new <tt>HostInfo</tt> instance with default attribut. 
     */
    public HostInfo() {
        this.XMPPdomain = null;
        this.XMPPhost = null;
        this.MUCdomain = null;
        this.roomName = null;
        this.port = 0;
        this.focusUserJid = null;
	this.MUCvideobridge = null;
    }
    
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
            String roomName,
            String focusUserJid,
	    String MUCvideobridge)
    {
        this.XMPPdomain = XMPPdomain;
        this.port = port;
        this.XMPPhost = XMPPhost;
        this.MUCdomain = MUCdomain;
        this.roomName = roomName;
        this.focusUserJid = focusUserJid;
	this.MUCvideobridge = MUCvideobridge;
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

    /**
     * The jid (jitsi-id) to pass to the MUC, assumed to have permissions to create/destroy rooms.
     * @return the jid of the focus user.
     */
    public String getFocusUserJid()
    {
        return this.focusUserJid;
    }

    /**
     * The name of the videobridge to send to the MUC server, when creating rooms
     * @return the name of the videobridge to pass to the MUC
     */
    public String getMUCvideobridge()
    {
        return this.MUCvideobridge;
    }
}
