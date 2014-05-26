package org.jitsi.hammer;

import org.jitsi.service.neomedia.device.*;
import org.jitsi.service.neomedia.format.*;


class SelectedMedia
{
    public MediaDevice mediaDevice;
    public MediaFormat mediaFormat;
    public byte dynamicPayloadType;

    public SelectedMedia(
            MediaDevice mediaDevice,
            MediaFormat mediaFormat,
            byte dynamicPayloadType)
    {
        this.mediaDevice = mediaDevice;
        this.mediaFormat = mediaFormat;
        this.dynamicPayloadType = dynamicPayloadType;
    }


}
