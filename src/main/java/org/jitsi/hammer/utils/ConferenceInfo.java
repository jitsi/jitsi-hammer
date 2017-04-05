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

/**
 * The class encapsulating conference properties
 *
 * @author Maksym Kulish
 */
public class ConferenceInfo {

    /**
     * The "channelLastN" conference property
     * 
     */
    private String channelLastN;

    /**
     * The "adaptiveLastN" conference property
     */
    private String adaptiveLastN;

    /**
     * The "adaptiveSimulcast" conference property
     */
    private String adaptiveSimulcast;

    /**
     * The "openSctp" conference property
     */
    private String openSctp;

    /**
     * The "startAudioMuted" conference property
     */
    private String startAudioMuted;

    /**
     * The "startVideoMuted" conference property
     */
    private String startVideoMuted;

    /**
     * The "simulcastMode" conference property
     */
    private String simulcastMode;

    /**
     * Create new ConferenceInfo instance
     * 
     * @param channelLastN The "channelLastN" conference property
     * @param adaptiveLastN The "adaptiveLastN" conference property
     * @param adaptiveSimulcast The "adaptiveSimulcast" conference property
     * @param openSctp The "openSctp" conference property
     * @param startAudioMuted The "startAudioMuted" conference property
     * @param startVideoMuted The "startVideoMuted" conference property
     * @param simulcastMode The "simulcastMode" conference property
     */
    public ConferenceInfo(
            String channelLastN,
            String adaptiveLastN,
            String adaptiveSimulcast,
            String openSctp,
            String startAudioMuted,
            String startVideoMuted,
            String simulcastMode) 
    {
        this.channelLastN = channelLastN;
        this.adaptiveLastN = adaptiveLastN;
        this.adaptiveSimulcast = adaptiveSimulcast;
        this.openSctp = openSctp;
        this.startAudioMuted = startAudioMuted;
        this.startVideoMuted = startVideoMuted;
        this.simulcastMode = simulcastMode;
    }

    /**
     * Get the "adaptiveLastN" conference property
     * 
     * @return the "adaptiveLastN" conference property
     */
    public String getAdaptiveLastN() {
        return adaptiveLastN;
    }

    /**
     * Get the "channelLastN" conference property
     *
     * @return the "channelLastN" conference property
     */
    public String getChannelLastN() {
        return channelLastN;
    }

    /**
     * Get the "adaptiveSimulcast" conference property
     *
     * @return the "adaptiveSimulcast" conference property
     */
    public String getAdaptiveSimulcast() {
        return adaptiveSimulcast;
    }

    /**
     * Get the "openSctp" conference property
     *
     * @return the "openSctp" conference property
     */
    public String getOpenSctp() {
        return openSctp;
    }

    /**
     * Get the "startAudioMuted" conference property
     *
     * @return the "startAudioMuted" conference property
     */
    public String getStartAudioMuted() {
        return startAudioMuted;
    }

    /**
     * Get the "startVideoMuted" conference property
     *
     * @return the "startVideoMuted" conference property
     */
    public String getStartVideoMuted() {
        return startVideoMuted;
    }

    /**
     * Get the "simulcastMode" conference property
     *
     * @return the "simulcastMode" conference property
     */
    public String getSimulcastMode() {
        return simulcastMode;
    }
    
    
}
