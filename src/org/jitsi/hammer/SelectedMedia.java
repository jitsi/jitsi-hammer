package org.jitsi.hammer;

import org.jitsi.service.neomedia.device.*;
import org.jitsi.service.neomedia.format.*;

/**
 * @author Thomas Kuntz
 * 
 * This class exists because I needed to carry around a triplet of value.
 * This class may (90% chance) disappear in the future because instead of
 * using a Map of triplet, I'll use 3 Map...
 * 
 * For now this class contains the MediaDevice and MediaFormat selected
 * that will be used in the MediaStream later.
 * It also contains the dynamic payload type in case the payload type of the
 * MediaFormat is a dynamic one (MediaFormat.getRTPPayloadType() return 
 * RTP_PAYLOAD_TYPE_UNKNOWN).
 */
public class SelectedMedia
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
