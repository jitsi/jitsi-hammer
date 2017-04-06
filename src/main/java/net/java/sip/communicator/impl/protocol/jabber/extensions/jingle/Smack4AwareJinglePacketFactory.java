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

import org.jivesoftware.smack.packet.*;


/**
 * This is just <tt>JinglePacketFactory</tt> which return 
 * <tt>Smack4AwareJingleIQ</tt> instead of straightforward <tt>JingleIQ</tt>
 * 
 * @author MaksymKulish
 *
 */
public class Smack4AwareJinglePacketFactory extends JinglePacketFactory {

    /**
     * Creates a {@link JingleIQ} <tt>session-terminate</tt> packet with the
     * specified src, dst, sid, and reason.
     *
     * @param from our JID
     * @param to the destination JID
     * @param sid the ID of the Jingle session that this message will be
     * terminating.
     * @param reason the reason for the termination
     * @param reasonText a human readable reason for the termination or
     * <tt>null</tt> for none.
     *
     * @return the newly constructed {@link JingleIQ} <tt>session-terminate</tt>
     * packet.
     * .
     */
    public static Smack4AwareJingleIQ createSessionTerminate(String from,
                                                  String to,
                                                  String sid,
                                                  Reason reason,
                                                  String reasonText)
    {
        Smack4AwareJingleIQ terminate = new Smack4AwareJingleIQ();

        terminate.setTo(to);
        terminate.setFrom(from);
        terminate.setType(IQ.Type.SET);

        terminate.setSID(sid);
        terminate.setAction(JingleAction.SESSION_TERMINATE);

        ReasonPacketExtension reasonPacketExt
                = new ReasonPacketExtension(reason, reasonText, null);

        terminate.setReason(reasonPacketExt);

        return terminate;
    }

    /**
     * Creates a {@link JingleIQ} <tt>session-accept</tt> packet with the
     * specified <tt>from</tt>, <tt>to</tt>, <tt>sid</tt>, and <tt>content</tt>.
     * Given our role in a conversation, we would assume that the <tt>from</tt>
     * value should also be used for the value of the Jingle <tt>responder</tt>.
     *
     * @param from our JID
     * @param to the destination JID
     * @param sid the ID of the Jingle session that this message will be
     * terminating.
     * @param contentList the content elements containing media and transport
     * descriptions.
     *
     * @return the newly constructed {@link JingleIQ} <tt>session-accept</tt>
     * packet.
     */
    public static Smack4AwareJingleIQ createSessionAccept(
            String                           from,
            String                           to,
            String                           sid,
            Iterable<ContentPacketExtension> contentList)
    {
        Smack4AwareJingleIQ sessionAccept = new Smack4AwareJingleIQ();

        sessionAccept.setTo(to);
        sessionAccept.setFrom(from);
        sessionAccept.setResponder(from);
        sessionAccept.setType(IQ.Type.SET);

        sessionAccept.setSID(sid);
        sessionAccept.setAction(JingleAction.SESSION_ACCEPT);

        for(ContentPacketExtension content : contentList)
            sessionAccept.addContent(content);

        return sessionAccept;
    }
    
}
