/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 */

package org.jitsi.hammer;

import org.kohsuke.args4j.*;

/**
 * @author Thomas Kuntz
 * 
 * This class is used with args4j to easily deal with jitsi-hammer arguments
 * and options 
 *
 */
public class CmdLineArguments
{
    /**
     * @Option is used by args4j to know what options can be set as arguments
     * for this program.
     */
    
    /**
     * The domain name of the XMPP server.
     */
    @Option(name="-XMPPdomain",usage="The XMPP domain name",required=true)
    private String XMPPdomain;
    
    /**
     * The hostname used to access the XMPP server.
     */
    @Option(name="-XMPPhost",usage="The XMPP server hostname",required=true)
    private String XMPPhost;
    
    
    /**
     * The hostname used by the XMPP server (used to access to the MUC).
     */
    @Option(name="-MUCDomain",usage="The MUC domain name",required=true)
    private String MUCdomain;
    
    /**
     * The name of the MUC room that we'll use.
     */
    @Option(name="-room",usage="The MUC room name")
    private String roomName = "TestHammer";
    
    /**
     * The port used by the XMPP server.
     */
    @Option(name="-port",usage="The port of the XMPP server")
    private int port = 5222;
    
    /**
     * The number of fake users jitsi-hammer will create.
     */
    @Option(name="-users",usage="The number of fake users the hammer will create")
    private int numberOfFakeUsers = 1;
    
    /**
     * The length of the run (in seconds).
     */
    @Option(name="-length",usage="The length of the run in second."
            + "If zero or negative, the run will never stop itself")
    private int runLength = 0;
    
    /**
     * Create a HostInfo from the CLI options
     * @return a HostInfo created from the CLI options
     */
    public HostInfo getHostInfoFromArguments()
    {
        return new HostInfo(XMPPdomain, XMPPhost, port,MUCdomain,roomName);
    }
    
    /**
     * Get the number of fake users jitsi-hammer will create.
     * @return the number of fake users jitsi-hammer will create.
     */
    public int getNumberOfFakeUsers()
    {
        return numberOfFakeUsers;
    }
    
    /**
     * Get the length of the run (in seconds).
     * @return the length of the run (in seconds).
     */
    public int getRunLength()
    {
        return runLength;
    }
}
