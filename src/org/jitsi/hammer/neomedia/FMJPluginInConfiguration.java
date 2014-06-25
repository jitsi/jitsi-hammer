
/*
 * Jitsi-Hammer, A traffic generator for Jitsi Videobridge.
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jitsi.hammer.neomedia;

import java.net.*;
import java.util.*;

import javax.media.PackageManager;

import org.jitsi.impl.neomedia.codec.FMJPlugInConfiguration;
import org.jitsi.util.Logger;

/**
 * Utility class that handles registration of JFM packages and plugins.
 *
 * @author Thomas Kuntz
 */
public class FMJPluginInConfiguration
{
    /**
     * The <tt>Logger</tt> used by the <tt>FMJPlugInConfiguration</tt> class
     * for logging output.
     */
    private static final Logger logger
        = Logger.getLogger(FMJPlugInConfiguration.class);
    
    /**
     * Whether custom packages have been registered with JFM
     */
    private static boolean packagesRegistered = false;
    
    /**
     * The package prefixes of the additional JMF <tt>DataSource</tt>s (e.g. low
     * latency PortAudio and ALSA <tt>CaptureDevice</tt>s).
     */
    private static final String[] CUSTOM_PACKAGES
        = {
            "org.jitsi.hammer.neomedia.jmfext",
        };
    
    /**
     * Register in JMF the custom packages we provide
     */
    public static void registerCustomPackages()
    {
        if(packagesRegistered)
            return;

        @SuppressWarnings("unchecked")
        Vector<String> packages = PackageManager.getProtocolPrefixList();
        boolean loggerIsDebugEnabled = logger.isDebugEnabled();

        // We prefer our custom packages/protocol prefixes over FMJ's.
        for (int i = CUSTOM_PACKAGES.length - 1; i >= 0; i--)
        {
            String customPackage = CUSTOM_PACKAGES[i];

            /*
             * Linear search in a loop but it doesn't have to scale since the
             * list is always short.
             */
            if (!packages.contains(customPackage))
            {
                packages.add(0, customPackage);
                if (loggerIsDebugEnabled)
                    logger.debug("Adding package  : " + customPackage);
            }
        }

        PackageManager.setProtocolPrefixList(packages);
        PackageManager.commitProtocolPrefixList();
        if (loggerIsDebugEnabled)
            logger.debug("Registering new protocol prefix list: " + packages);

        packagesRegistered = true;
    }
}