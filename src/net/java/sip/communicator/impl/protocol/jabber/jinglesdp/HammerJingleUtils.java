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
package net.java.sip.communicator.impl.protocol.jabber.jinglesdp;

import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.*;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension.CreatorEnum;
import net.java.sip.communicator.impl.protocol.jabber.extensions.jingle.ContentPacketExtension.SendersEnum;
import net.java.sip.communicator.service.protocol.media.DynamicPayloadTypeRegistry;
import net.java.sip.communicator.service.protocol.media.DynamicRTPExtensionsRegistry;
import net.java.sip.communicator.util.Logger;
import net.java.sip.communicator.util.NetworkUtils;
import org.jitsi.service.libjitsi.LibJitsi;
import org.jitsi.service.neomedia.*;
import org.jitsi.service.neomedia.format.AudioMediaFormat;
import org.jitsi.service.neomedia.format.MediaFormat;
import org.jitsi.service.neomedia.format.MediaFormatFactory;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class contains a number of utility methods that are meant to facilitate
 * creating and parsing jingle media rtp description descriptions and
 * transports.
 *
 * @author Emil Ivov
 * @author Lyubomir Marinov
 */
public class HammerJingleUtils extends JingleUtils
{

    /**
     * The <tt>Logger</tt> used by the <tt>JingleUtils</tt> class and its
     * instances for logging output.
     */
    private static final Logger logger = 
            Logger.getLogger(HammerJingleUtils.class);
    
    /**
     * Extracts and returns the list of <tt>MediaFormat</tt>s advertised in
     * <tt>description</tt> preserving their oder and registering dynamic payload
     * type numbers in the specified <tt>ptRegistry</tt>. Note that this method
     * would only include in the result list <tt>MediaFormat</tt> instances
     * that are currently supported by our <tt>MediaService</tt> implementation
     * and enabled in its configuration. This means that the method could
     * return an empty list even if there were actually some formats in the
     * <tt>mediaDesc</tt> if we support none of them or if all these we support
     * are not enabled in the <tt>MediaService</tt> configuration form.
     *
     * @param description the <tt>MediaDescription</tt> that we'd like to probe
     * for a list of <tt>MediaFormat</tt>s
     * @param ptRegistry a reference to the <tt>DynamycPayloadTypeRegistry</tt>
     * where we should be registering newly added payload type number to format
     * mappings.
     *
     * @return an ordered list of <tt>MediaFormat</tt>s that are both advertised
     * in the <tt>description</tt> and supported by our <tt>MediaService</tt>
     * implementation.
     */
    public static List<MediaFormat> extractFormats(
                                     RtpDescriptionPacketExtension description,
                                     DynamicPayloadTypeRegistry ptRegistry)
    {
        List<MediaFormat> mediaFmts = new ArrayList<MediaFormat>();
        List<PayloadTypePacketExtension> payloadTypes
                                            = description.getPayloadTypes();

        for(PayloadTypePacketExtension ptExt : payloadTypes)
        {
            MediaFormat format = payloadTypeToMediaFormat(ptExt, ptRegistry);

            //continue if our media service does not know this format
            if(format == null)
            {
                if(logger.isTraceEnabled())
                    logger.trace("Unsupported remote format: " + ptExt.toXML());
            }
            else
                mediaFmts.add(format);
        }

        return mediaFmts;
    }

    /**
     * Returns the {@link MediaFormat} described in the <tt>payloadType</tt>
     * extension or <tt>null</tt> if we don't recognize the format.
     * This method uses <tt>LibJitsi</tt> instead of <tt>JabberActivator</tt>
     *
     * @param payloadType the {@link PayloadTypePacketExtension} which is to be
     * parsed into a {@link MediaFormat}.
     * @param ptRegistry the {@link DynamicPayloadTypeRegistry} that we would
     * use for the registration of possible dynamic payload types or
     * <tt>null</tt> the returned <tt>MediaFormat</tt> is to not be registered
     * into a <tt>DynamicPayloadTypeRegistry</tt>.
     *
     * @return the {@link MediaFormat} described in the <tt>payloadType</tt>
     * extension or <tt>null</tt> if we don't recognize the format.
     */
    public static MediaFormat payloadTypeToMediaFormat(
            PayloadTypePacketExtension payloadType,
            DynamicPayloadTypeRegistry ptRegistry)
    {
        return
                payloadTypeToMediaFormat(
                        payloadType,
                        LibJitsi.getMediaService(),
                        ptRegistry);
    }

}
