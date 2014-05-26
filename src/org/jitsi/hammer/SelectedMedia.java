package org.jitsi.hammer;

import org.jitsi.service.neomedia.device.*;
import org.jitsi.service.neomedia.format.*;


class SelectedMedia
{
    public MediaDevice mediaDevice;
    public MediaFormat mediaFormat;

    public SelectedMedia(MediaDevice mediaDevice, MediaFormat mediaFormat)
    {
        this.mediaDevice = mediaDevice;
        this.mediaFormat = mediaFormat;
    }
}
