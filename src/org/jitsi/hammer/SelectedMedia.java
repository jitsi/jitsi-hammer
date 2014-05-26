package org.jitsi.hammer;

import org.jitsi.service.neomedia.device.MediaDevice;
import org.jitsi.service.neomedia.format.MediaFormat;


class SelectedMedia {
    public MediaDevice mediaDevice;
    public MediaFormat mediaFormat;

    public SelectedMedia(MediaDevice mediaDevice, MediaFormat mediaFormat)
    {
        this.mediaDevice = mediaDevice;
        this.mediaFormat = mediaFormat;
    }
}
