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
package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

import java.util.*;

/**
 * <tt>JingleIQ</tt> which can wire up all the <tt>PacketExtension</tt>
 * from Jitsi which rely on Smack3 and Smack4 XMPP client library
 * 
 * @author Maksym Kulish
 */
public class Smack4AwareJingleIQ extends JingleIQ {

    /**
     * This exists since Smack4 do not have <tt>getExtensionsXML</tt> method
     * for <tt>IQ</tt> which return String; it specifies <tt>CharSequence</tt>
     * for its return type, causing <tt>JingleIQ</tt> to throw 
     * <tt>AbstractMethodError</tt> when serializing a packet
     * 
     * @return XML representation of <tt>Collection<PacketExtension></tt>
     *         belonging to this <tt>IQ</tt>
     */
    public String getExtensionsXML() {
        CharSequence xmlExtensionsRepresentation = super.getExtensionsXML();
        return xmlExtensionsRepresentation.toString();
    }

    /**
     * This exists since Smack4 do not have <tt>getExtensionsXML</tt> method
     * for <tt>IQ</tt> which return String; it specifies <tt>CharSequence</tt>
     * for its return type, causing <tt>JingleIQ</tt> to throw 
     * <tt>AbstractMethodError</tt> when serializing a packet
     */
    @Override
    public String getChildElementXML()
    {
        StringBuilder bldr = new StringBuilder("<" + ELEMENT_NAME);

        bldr.append(" xmlns='" + NAMESPACE + "'");

        bldr.append(" " + ACTION_ATTR_NAME + "='" + getAction() + "'");

        if( getInitiator() != null)
            bldr.append(" " + INITIATOR_ATTR_NAME
                    + "='" + getInitiator() + "'");

        if( getResponder() != null)
            bldr.append(" " + RESPONDER_ATTR_NAME
                    + "='" + getResponder() + "'");

        bldr.append(" " + SID_ATTR_NAME
                + "='" + getSID() + "'");

        CharSequence extensionsXMLSeq = getExtensionsXML();
        String extensionsXML = extensionsXMLSeq.toString();
        List<ContentPacketExtension> contentList = getContentList();
        ReasonPacketExtension reason = getReason();
        SessionInfoPacketExtension sessionInfoPacketExtension = 
                getSessionInfo();
        
        if ((contentList.size() == 0)
                && (reason == null)
                && (sessionInfoPacketExtension == null)
                && ((extensionsXML == null) || (extensionsXML.length() == 0)))
        {
            bldr.append("/>");
        }
        else
        {
            bldr.append(">");//it is possible to have empty jingle elements

            //content
            for(ContentPacketExtension cpe : contentList)
            {
                bldr.append(cpe.toXML());
            }

            //reason
            if (getReason() != null)
                bldr.append(reason.toXML());

            //session-info
            //XXX: this is RTP specific so we should probably handle it in a
            //subclass
            if (sessionInfoPacketExtension != null)
                bldr.append(sessionInfoPacketExtension.toXML());

            // extensions
            if ((extensionsXML != null) && (extensionsXML.length() != 0))
                bldr.append(extensionsXML);

            bldr.append("</" + ELEMENT_NAME + ">");
        }

        return bldr.toString();
    }
    
}
