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

import org.jivesoftware.smack.packet.IQ;

import java.util.ArrayList;
import java.util.List;

/**
 * <tt>IQ</tt> instance implementing the Jingle IQ message
 * @author Brian Baldino
 */
public class NewJingleIQ extends IQ
{
    /**
     * The name space that jingle belongs to.
     */
    public static final String NAMESPACE = "urn:xmpp:jingle:1";

    /**
     * The name of the element that contains the jingle data.
     */
    public static final String ELEMENT_NAME = "jingle";

    /**
     * The name of the argument that contains the jingle action value.
     */
    public static final String ACTION_ATTR_NAME = "action";

    /**
     * The name of the argument that contains the "initiator" jid.
     */
    public static final String INITIATOR_ATTR_NAME = "initiator";

    /**
     * The name of the argument that contains the "responder" jid.
     */
    public static final String RESPONDER_ATTR_NAME = "responder";

    /**
     * The name of the argument that contains the session id.
     */
    public static final String SID_ATTR_NAME = "sid";

    private NewJingleAction action;
    private String initiator;
    private String responder;
    private String sid;
    /**
     * The <tt>reason</tt> extension in a <tt>jingle</tt> IQ providers machine
     * and possibly human-readable information about the reason for the action.
     */
    private ReasonPacketExtension reason;

    /**
     * The list of "content" elements included in this IQ.
     */
    private final List<NewContentPacketExtension> contentList
            = new ArrayList<>();

    public NewJingleIQ()
    {
        super(ELEMENT_NAME, NAMESPACE);
    }

    @Override
    public IQ.IQChildElementXmlStringBuilder getIQChildElementBuilder(IQ.IQChildElementXmlStringBuilder xml) {
        xml.attribute(ACTION_ATTR_NAME, getAction().toString());
        if (initiator != null)
        {
            xml.attribute(INITIATOR_ATTR_NAME, getInitiator());
        }

        if (responder != null)
        {
            xml.attribute(RESPONDER_ATTR_NAME, getResponder());
        }

        xml.attribute(SID_ATTR_NAME, getSID());

        xml.rightAngleBracket();
        for (NewContentPacketExtension cpe : contentList)
        {
            xml.element(cpe);
        }
        if (reason != null)
        {
            xml.element(reason);
        }
        //xml.closeElement(ELEMENT_NAME);

        return xml;
    }

    public void setAction(NewJingleAction action) {
        this.action = action;
    }

    public NewJingleAction getAction() {
        return this.action;
    }

    public void setInitiator(String initiator) {
        this.initiator = initiator;
    }

    public String getInitiator() {
        return this.initiator;
    }

    public void setSID(String sid) {
        this.sid = sid;
    }

    public String getSID() {
        return this.sid;
    }

    public void setResponder(String responder) {
        this.responder = responder;
    }

    public String getResponder() {
        return this.responder;
    }

    /**
     * Specifies this IQ's <tt>reason</tt> extension. The <tt>reason</tt>
     * extension in a <tt>jingle</tt> IQ provides machine and possibly human
     * -readable information about the reason for the action.
     *
     * @param reason this IQ's <tt>reason</tt> extension.
     */
    public void setReason(ReasonPacketExtension reason)
    {
        this.reason = reason;
    }

    /**
     * Returns this IQ's <tt>reason</tt> extension. The <tt>reason</tt>
     * extension in a <tt>jingle</tt> IQ provides machine and possibly human
     * -readable information about the reason for the action.
     *
     * @return this IQ's <tt>reason</tt> extension.
     */
    public ReasonPacketExtension getReason()
    {
        return reason;
    }

    /**
     * Adds <tt>contentPacket</tt> to this IQ's content list.
     *
     * @param contentPacket the content packet extension we'd like to add to
     * this element's content list.
     */
    public void addContent(NewContentPacketExtension contentPacket)
    {
        synchronized(contentList)
        {
            this.contentList.add(contentPacket);
        }
    }

    public List<NewContentPacketExtension> getContentList() {
        synchronized(contentList)
        {
            return new ArrayList<>(contentList);
        }
    }
}
