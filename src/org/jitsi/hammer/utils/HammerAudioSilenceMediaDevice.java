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

import org.jitsi.impl.neomedia.device.*;

/**
 * Originally, jitsi-hammer included a modified copy of
 * {@code AudioSilenceMediaDevice.java} from jitsi-videobridge. The modifications
 * consisted of the overridden {@code createPlayer()} and {@code createSession()}
 * methods being removed. It is unclear to me whether the removal is necessary.
 * I was not able to convince myself that it isn't, so I'm including this class,
 * which effectively removes the overridden {@code createSession()} method. I
 * couldn't find an easy way to do the same for {@code createPlayer()} (because
 * {@link AudioSilenceMediaDevice} removes the declaration of a thrown
 * {@code Exception}) so I have left it. Presumably the only reason that it is
 * missing is because the java file was copied from a jitsi-videobridge version
 * which does not include commit 63c6cc0150be98b3a4c93d2f620650a1fe7741b5.
 *
 * @author Boris Grozev
 */
class HammerAudioSilenceMediaDevice
    extends AudioSilenceMediaDevice
{
    /**
     * Jitsi-hammer used to contain a modified copy of {@code
     * AudioSilenceCaptureDevice} which had the value of {@code CLOCK_ONLY}
     * changed to {@code false}.
     */
    HammerAudioSilenceMediaDevice()
    {
        super(false);
    }

    /**
     * Effectively disable the override in {@link AudioSilenceMediaDevice} by
     * doing what its parent ({@link AbstractMediaDevice}) would do.
     */
    @Override
    public MediaDeviceSession createSession()
    {
        return new AudioMediaDeviceSession(this);
    }
}
