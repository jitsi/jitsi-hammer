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

import java.util.*;

import net.java.sip.communicator.impl.protocol.jabber.extensions.*;

import org.jivesoftware.smack.packet.*;

/**
 * An {@link AbstractPacketExtension} implementation for transport elements.
 *
 * @author Emil Ivov
 * @author Lyubomir Marinov
 */
public class NewIceUdpTransportPacketExtension
        extends NewAbstractExtensionElement
{
    /**
     * The name of the "transport" element.
     */
    public static final String NAMESPACE
            = "urn:xmpp:jingle:transports:ice-udp:1";

    /**
     * The name of the "transport" element.
     */
    public static final String ELEMENT_NAME = "transport";

    /**
     * The name of the <tt>pwd</tt> ICE attribute.
     */
    public static final String PWD_ATTR_NAME = "pwd";

    /**
     * The name of the <tt>ufrag</tt> ICE attribute.
     */
    public static final String UFRAG_ATTR_NAME = "ufrag";

    /**
     * A list of one or more candidates representing each of the initiator's
     * higher-priority transport candidates as determined in accordance with
     * the ICE methodology.
     */
    private final List<NewCandidatePacketExtension> candidateList
            = new ArrayList<NewCandidatePacketExtension>();

    /**
     * Once the parties have connectivity and therefore the initiator has
     * completed ICE as explained in RFC 5245, the initiator MAY communicate
     * the in-use candidate pair in the signalling channel by sending a
     * transport-info message that contains a "remote-candidate" element
     */
    private NewRemoteCandidatePacketExtension remoteCandidate;

    /**
     * Creates a new {@link NewIceUdpTransportPacketExtension} instance.
     */
    public NewIceUdpTransportPacketExtension()
    {
        super(ELEMENT_NAME, NAMESPACE);
    }

    /**
     * Creates a new {@link NewIceUdpTransportPacketExtension} instance with the
     * specified <tt>namespace</tt> and <tt>elementName</tt>. The purpose of
     * this method is to allow {@link NewRawUdpTransportPacketExtension} to
     * extend this class.
     *
     * @param namespace the XML namespace that the instance should belong to.
     * @param elementName the name of the element that we would be representing.
     */
    protected NewIceUdpTransportPacketExtension(String namespace,
                                                String elementName)
    {
        super(namespace, elementName);
    }

    /**
     * Sets the ICE defined password attribute.
     *
     * @param pwd a password <tt>String</tt> as defined in RFC 5245
     */
    public void setPassword(String pwd)
    {
        super.setAttribute(PWD_ATTR_NAME, pwd);
    }

    /**
     * Returns the ICE defined password attribute.
     *
     * @return a password <tt>String</tt> as defined in RFC 5245
     */
    public String getPassword()
    {
        return super.getAttributeAsString(PWD_ATTR_NAME);
    }

    /**
     * Sets the ICE defined user fragment attribute.
     *
     * @param ufrag a user fragment <tt>String</tt> as defined in RFC 5245
     */
    public void setUfrag(String ufrag)
    {
        super.setAttribute(UFRAG_ATTR_NAME, ufrag);
    }

    /**
     * Returns the ICE defined user fragment attribute.
     *
     * @return a user fragment <tt>String</tt> as defined in RFC 5245
     */
    public String getUfrag()
    {
        return super.getAttributeAsString(UFRAG_ATTR_NAME);
    }

    /**
     * Returns this element's child (local or remote) candidate elements.
     *
     * @return this element's child (local or remote) candidate elements.
     */
    @Override
    public List<Element> getChildExtensions()
    {
        List<Element> childExtensions
                = new ArrayList<>();
        List<Element> superChildExtensions
                = super.getChildExtensions();

        childExtensions.addAll(superChildExtensions);

        synchronized (candidateList)
        {
            if (candidateList.size() > 0)
                childExtensions.addAll(candidateList);
            else if (remoteCandidate != null)
                childExtensions.add(remoteCandidate);
        }

        return childExtensions;
    }

    /**
     * Adds <tt>candidate</tt> to the list of {@link NewCandidatePacketExtension}s
     * registered with this transport.
     *
     * @param candidate the new {@link NewCandidatePacketExtension} to add to this
     * transport element.
     */
    public void addCandidate(NewCandidatePacketExtension candidate)
    {
        synchronized(candidateList)
        {
            candidateList.add(candidate);
        }
    }

    /**
     * Removes <tt>candidate</tt> from the list of
     * {@link NewCandidatePacketExtension}s registered with this transport.
     *
     * @param candidate the <tt>NewCandidatePacketExtension</tt> to remove from
     * this transport element
     * @return <tt>true</tt> if the list of <tt>NewCandidatePacketExtension</tt>s
     * registered with this transport contained the specified <tt>candidate</tt>
     */
    public boolean removeCandidate(NewCandidatePacketExtension candidate)
    {
        synchronized (candidateList)
        {
            return candidateList.remove(candidate);
        }
    }

    /**
     * Returns the list of {@link NewCandidatePacketExtension}s currently
     * registered with this transport.
     *
     * @return the list of {@link NewCandidatePacketExtension}s currently
     * registered with this transport.
     */
    public List<NewCandidatePacketExtension> getCandidateList()
    {
        synchronized(candidateList)
        {
            return new ArrayList<NewCandidatePacketExtension>(candidateList);
        }
    }

    /**
     * Sets <tt>candidate</tt> as the in-use candidate after ICE has terminated.
     *
     * @param candidate the new {@link NewCandidatePacketExtension} to set as an
     * in-use candidate for this session.
     */
    public void setRemoteCandidate(NewRemoteCandidatePacketExtension candidate)
    {
        this.remoteCandidate = candidate;
    }

    /**
     * Returns the in-use <tt>candidate</tt> for this session.
     *
     * @return Returns the in-use <tt>candidate</tt> for this session.
     */
    public NewRemoteCandidatePacketExtension getRemoteCandidate()
    {
        return remoteCandidate;
    }

    /**
     * Tries to determine whether  <tt>childExtension</tt> is a {@link
     * NewCandidatePacketExtension}, a {@link NewRemoteCandidatePacketExtension} or
     * something else and then adds it as such.
     *
     * @param childExtension the extension we'd like to add here.
     */
    @Override
    public void addChildExtension(Element childExtension)
    {
        //first check for RemoteCandidate because they extend Candidate.
        if(childExtension instanceof NewRemoteCandidatePacketExtension)
            setRemoteCandidate((NewRemoteCandidatePacketExtension) childExtension);

        else if(childExtension instanceof NewCandidatePacketExtension)
            addCandidate((NewCandidatePacketExtension) childExtension);

        else
            super.addChildExtension(childExtension);
    }

    /**
     * Checks whether an 'rtcp-mux' extension has been added to this
     * <tt>NewIceUdpTransportPacketExtension</tt>.
     * @return <tt>true</tt> if this <tt>NewIceUdpTransportPacketExtension</tt>
     * has a child with the 'rtcp-mux' name.
     */
    public boolean isRtcpMux()
    {
        for (Element packetExtension : getChildExtensions())
        {
            if (packetExtension instanceof NamedElement)
            {
                NamedElement namedPacketExtension = (NamedElement)packetExtension;
                if (NewRtcpmuxPacketExtension.ELEMENT_NAME
                        .equals(namedPacketExtension.getElementName()))
                    return true;
            }
        }
        return false;
    }

    /**
     * Clones a specific <tt>NewIceUdpTransportPacketExtension</tt> and its
     * candidates.
     *
     * @param src the <tt>NewIceUdpTransportPacketExtension</tt> to be cloned
     * @return a new <tt>NewIceUdpTransportPacketExtension</tt> instance which has
     * the same run-time type, attributes, namespace, text and candidates as the
     * specified <tt>src</tt>
     * @throws Exception if an error occurs during the cloning of the specified
     * <tt>src</tt> and its candidates
     */
    public static NewIceUdpTransportPacketExtension cloneTransportAndCandidates(
            NewIceUdpTransportPacketExtension src)
    {
        return cloneTransportAndCandidates(src, false);
    }

    /**
     * Clones a specific <tt>NewIceUdpTransportPacketExtension</tt> and its
     * candidates.
     *
     * @param src the <tt>NewIceUdpTransportPacketExtension</tt> to be cloned
     * @param copyDtls if <tt>true</tt> will also copy
     *                 {@link NewDtlsFingerprintPacketExtension}.
     * @return a new <tt>NewIceUdpTransportPacketExtension</tt> instance which has
     * the same run-time type, attributes, namespace, text and candidates as the
     * specified <tt>src</tt>
     * @throws Exception if an error occurs during the cloning of the specified
     * <tt>src</tt> and its candidates
     */
    public static NewIceUdpTransportPacketExtension cloneTransportAndCandidates(
            NewIceUdpTransportPacketExtension src, boolean copyDtls)
    {
        if (src == null)
            return null;

        NewIceUdpTransportPacketExtension dst = NewAbstractExtensionElement.clone(src);
        // Copy candidates
        for (NewCandidatePacketExtension srcCand : src.getCandidateList())
        {
            if (!(srcCand instanceof NewRemoteCandidatePacketExtension))
                dst.addCandidate(
                        NewAbstractExtensionElement.clone(srcCand));
        }
        // Copy RTCP MUX
        if (src.isRtcpMux())
        {
            dst.addChildExtension(new NewRtcpmuxPacketExtension());
        }
        // Optionally copy DTLS
        if (copyDtls)
        {
            for (NewDtlsFingerprintPacketExtension dtlsFingerprint
                    : src.getChildExtensionsOfType(
                    NewDtlsFingerprintPacketExtension.class))
            {
                NewDtlsFingerprintPacketExtension copy
                        = new NewDtlsFingerprintPacketExtension();

                copy.setFingerprint(dtlsFingerprint.getFingerprint());
                copy.setHash(dtlsFingerprint.getHash());
                copy.setRequired(dtlsFingerprint.getRequired());
                copy.setSetup(dtlsFingerprint.getSetup());

                dst.addChildExtension(copy);
            }
        }
        return dst;
    }
}
