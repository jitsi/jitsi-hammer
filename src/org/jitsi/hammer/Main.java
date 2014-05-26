package org.jitsi.hammer;

import org.kohsuke.args4j.*;


public class Main
{
    public static void main(String[] args)
        throws InterruptedException
    {
        
        java.util.logging.Logger l = java.util.logging.Logger.getLogger("");
        l.setLevel(java.util.logging.Level.WARNING);
        //LibJitsi.start();
        
        HostInfo infoCLI = new HostInfo();
        CmdLineParser parser = new CmdLineParser(infoCLI);
        try {
            parser.parseArgument(args);
        }
        catch(CmdLineException e)
        {
            System.err.println(e.getMessage());
            parser.printUsage(System.err);
            return;
        }
        
        //This HostInfo is used for tests in my localhost XMPP server
        //HostInfo infoCLI = new HostInfo("jitmeet.example.com","conference.jitmeet.example.com","jitsi-videobridge.lambada.jitsi.net","HammerTest");

        Hammer hammer = new Hammer(infoCLI,"JitMeet-Hammer",1);
        hammer.start();
        hammer.startStream();
        while(true) Thread.sleep(3600);
    }
}
