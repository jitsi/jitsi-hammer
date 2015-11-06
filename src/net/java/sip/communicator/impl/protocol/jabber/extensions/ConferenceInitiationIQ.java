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
package net.java.sip.communicator.impl.protocol.jabber.extensions;

import org.jitsi.hammer.utils.*;
import org.jivesoftware.smack.packet.*;

import java.util.*;

/**
 * An <tt>IQ</tt> that represents a conference initiation info-query 
 * that ought to be sent in order to get Jitsi Conference Focus 
 * managing the conference
 *
 * @author Maksym Kulish
 */
public class ConferenceInitiationIQ extends IQ 
{
    
    /**
     * The name of the "conference" XML element
     */
    public static final String ELEMENT_NAME = "conference";

    /**
     * The namespace for the XML element
     */
    public static final String NAMESPACE = "http://jitsi.org/protocol/focus";

    /**
     * The name of the attribute that contains the room URL
     */
    public static final String ROOM_ATTR_NAME = "room";

    /**
     * The name of the attribute that contains machine UID
     */
    public static final String MACHINE_UID_ATTR_NAME = "machine-uid";
    

    /**
     * The <tt>HostInfo</tt> object associated with a particular 
     * Hammer instance, containing room name,  XMPP vhost and (optionally) Focus
     * address associated with the targeted Jitsi deployment
     * and room name
     */
    private HostInfo serverInfo;

    /**
     * The random <tt>UUID</tt> to use in conference initiation as a machine UID
     */
    private UUID machineUID = UUID.randomUUID();

    /**
     * The list of conference property packet extension for the properties 
     * associated with this conference
     */
    private List<ConferencePropertyPacketExtension> conferenceProperties 
            = new ArrayList<ConferencePropertyPacketExtension>();


    /**
     * Add the <tt>ConferencePropertyPacketExtension</tt> to the list 
     * of the properties that will be sent with the conference
     * 
     * @param conferenceProperty the <tt>ConferencePropertyPacketExtension</tt> 
     *                           that will be sent with the conference
     */
    public void addConferenceProperty(
            ConferencePropertyPacketExtension conferenceProperty) 
    {
        this.conferenceProperties.add(conferenceProperty);
    }
    
    /**
     * Get the string builder for the child element XML
     * 
     * @return the child element section of the IQ XML
     */
    public StringBuilder getChildElementXML() 
    {

        StringBuilder stringBuilder = new StringBuilder("<" + ELEMENT_NAME);

        stringBuilder.append(" xmlns='" + NAMESPACE + "'");
        stringBuilder.append(" " + ROOM_ATTR_NAME + 
                "='" + serverInfo.getRoomURL() + "'");
        stringBuilder.append(" " + MACHINE_UID_ATTR_NAME + 
                "='" + machineUID + "'");
        
        if (this.conferenceProperties.size() == 0)
        {
            stringBuilder.append(" />");
        }
        else
        {
            stringBuilder.append(" >");
            for (ConferencePropertyPacketExtension cppe: 
                    conferenceProperties) {
                
                stringBuilder.append(cppe.toXML());
                
            }
            stringBuilder.append("</" + ELEMENT_NAME + ">");
        }
        
        return stringBuilder;
        
    }
    
    /**
     * Set the <tt>HostInfo</tt> associated with this Hammer instance
     *
     * @param serverInfo a <tt>HostInfo</tt> instance associated with 
     *                   this Hammer instance
     */
    public void setServerInfo(HostInfo serverInfo) {
        this.serverInfo = serverInfo;
    }
    
}
