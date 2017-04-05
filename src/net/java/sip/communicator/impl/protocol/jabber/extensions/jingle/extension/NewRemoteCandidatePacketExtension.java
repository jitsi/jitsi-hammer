/*
 * Jitsi, the OpenSource Java VoIP and Instant Messaging client.
 *
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
package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.extension;

/**
 * A representation of the <tt>remote-candidate</tt> ICE transport element.
 *
 * @author Emil Ivov
 */
public class NewRemoteCandidatePacketExtension extends NewCandidatePacketExtension
{
    /**
     * The name of the "candidate" element.
     */
    public static final String ELEMENT_NAME = "remote-candidate";

    /**
     * Creates a new {@link NewCandidatePacketExtension}
     */
    public NewRemoteCandidatePacketExtension()
    {
        super(ELEMENT_NAME);
    }
}
