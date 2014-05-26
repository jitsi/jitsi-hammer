package org.jitsi.hammer;

import org.kohsuke.args4j.*;

public class HostInfo
{
    /**
     * @Option is used by args4j to know what options can be set as arguments for this program
     */
    @Option(name="-domain",usage="XMPP domain name")
    private String domain = "guest.jit.si";
    @Option(name="-muc",usage="XMPP server hostname")
    private String muc = "meet.jit.si";
    @Option(name="-bridge",usage="jitsi-videobridge domain name")
    private String bridge = "jitsi-videobridge.lambada.jitsi.net";
    @Option(name="-room",usage="MUC room name")
    private String roomName = "TestHammer";
    @Option(name="-port",usage="XMPP port")
    private int port = 5222;

    public HostInfo() {}

    public HostInfo(String domain,String muc,int port,String bridge,String roomName)
    {
        this.domain = domain;
        this.port = port;
        this.muc = muc;
        this.bridge = bridge;
        this.roomName = roomName;
    }
    
    public String getDomain()
    {
        return this.domain.toLowerCase();
    }

    public String getMUC()
    {
        return this.muc.toLowerCase();
    }
    
    public String getBridge()
    {
        return this.bridge.toLowerCase();
    }
    
    public String getRoomName()
    {
        return this.roomName.toLowerCase();
    }

    public int getPort()
    {
        return this.port;
    }
}
