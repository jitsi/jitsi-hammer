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
package org.jitsi.hammer.utils;

import java.net.*;

/**
 * This class provides the functionality of parsing of the BOSH URL
 * 
 *  @author Maksym Kulish 
 */
public class BOSHUrl {

    /**
     * The BOSH URI object
     */
    private URI uri;

    /**
     * Construct the BOSH URI from a given string
     * 
     * @param boshURI the BOSH URI string
     * @throws URISyntaxException when an improper BOSH URL given to constructor
     */
    public BOSHUrl(String boshURI) throws URISyntaxException
    {
        this.uri = new URI(boshURI);
        if (!this.uri.isAbsolute()) 
        {
            throw new URISyntaxException(boshURI, "Hammer accepts " +
                    "only absolute URIs as input");
        }
        if (!uri.getScheme().startsWith("http"))
        {
            throw new URISyntaxException(boshURI, "Hammer accepts only " +
                    "HTTP(S) URIs as input");
        }
    }

    /**
     * Construct the <tt>HostInfo</tt> from a given BOSH URI
     * 
     * @param roomName The room name to include in <tt>HostInfo</tt>
     * @return <tt>HostInfo</tt> describing the server and room where to target
     *        the load
     */
    public HostInfo getHostInfo(String roomName) {
        String XMPPdomain = uri.getHost();
        String BOSHhost = uri.getHost();
        String MUCdomain = "conference." + uri.getHost();
        String rawPath = uri.getRawPath();
        String boshURI = rawPath != null ? rawPath: "/";
        
        if (uri.getRawQuery() != null) {
            boshURI = boshURI + "?" + uri.getRawQuery();
        }
        
        boolean useHTTPS = (uri.getScheme().equals("https"));
        int port = uri.getPort();
        if (port == -1) {
            port = useHTTPS? 443:80;
        }
        return new HostInfo(
                XMPPdomain, 
                BOSHhost, 
                port, 
                MUCdomain, 
                roomName, 
                boshURI, 
                useHTTPS
        );
    }
    
}
