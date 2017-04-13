
/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
 * Copyright @ 2017 Atlassian Pty Ltd
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
package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;


import java.util.*;
import net.java.sip.communicator.util.*;

/**
 * Implements <tt>NewAbstractExtensionElement</tt> for the <tt>source</tt> element
 * defined by <a href="http://hancke.name/jabber/jingle-sources">
 * Source-Specific Media Attributes in Jingle</a>.
 *
 * @author Lyubomir Marinov
 * @author Pawel Domas
 * @author Brian Baldino
 */
public class NewSourcePacketExtension
        extends NewAbstractExtensionElement
{
    private final static Logger logger
            = Logger.getLogger(NewSourcePacketExtension.class);

    /**
     * The XML name of the <tt>setup</tt> element defined by Source-Specific
     * Media Attributes in Jingle.
     */
    public static final String ELEMENT_NAME = "source";

    /**
     * The XML namespace of the <tt>setup</tt> element defined by
     * Source-Specific Media Attributes in Jingle.
     */
    public static final String NAMESPACE = "urn:xmpp:jingle:apps:rtp:ssma:0";

    /**
     * The XML name of the <tt>setup</tt> element's attribute which corresponds
     * to the <tt>ssrc</tt> media attribute in SDP.
     */
    public static final String SSRC_ATTR_NAME = "ssrc";

    /** Initializes a new <tt>SourcePacketExtension</tt> instance. */
    public NewSourcePacketExtension()
    {
        super(ELEMENT_NAME, NAMESPACE);
    }

    public NewSourcePacketExtension(String type, String ssrc, String direction)
    {
        super(ELEMENT_NAME, NAMESPACE);
        setAttribute("type", type);
        setSSRC(ssrc);
        setAttribute("direction", direction);
    }

    /**
     * Adds a specific parameter (as defined by Source-Specific Media Attributes
     * in Jingle) to this source.
     *
     * @param parameter the <tt>ParameterPacketExtension</tt> to add to this
     * source
     */
    public void addParameter(ParameterPacketExtension parameter)
    {
        addChildExtension(parameter);
    }

    /**
     * Gets the parameters (as defined by Source-Specific Media Attributes in
     * Jingle) of this source.
     *
     * @return the <tt>ParameterPacketExtension</tt>s of this source
     */
    public List<NewParameterPacketExtension> getParameters()
    {
        return getChildExtensionsOfType(NewParameterPacketExtension.class);
    }

    /**
     * Finds the value of SSRC parameter identified by given name.
     * @param name the name of SSRC parameter to find.
     * @return value of SSRC parameter
     */
    public String getParameter(String name)
    {
        for (NewParameterPacketExtension param : getParameters())
        {
            if (name.equals(param.getName()))
                return param.getValue();
        }
        return null;
    }

    /**
     * Gets the synchronization source (SSRC) ID of this source.
     *
     * @return the synchronization source (SSRC) ID of this source
     */
    public long getSSRC()
    {
        String s = getAttributeAsString(SSRC_ATTR_NAME);

        return (s == null) ? -1 : Long.parseLong(s);
    }

    /**
     * Sets the synchronization source (SSRC) ID of this source.
     *
     * @param ssrc the synchronization source (SSRC) ID to be set on this source
     */
    public void setSSRC(long ssrc)
    {
        setSSRC(ssrc == -1 ? null : Long.toString(0xffffffffL & ssrc));
    }

    public void setSSRC(String ssrc)
    {
        if (ssrc == null)
            removeAttribute(SSRC_ATTR_NAME);
        else
            setAttribute(SSRC_ATTR_NAME, ssrc);

    }
}