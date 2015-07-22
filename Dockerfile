FROM java:6

ENV INTERVAL=2000 \
    LENGTH=120 \
    LOG_LEVEL="WARNING" \
    MUC_PREFIX="conference" \
    PORT=5222 \
    ROOM_NAME="hammer.test" \
    USERS=20 \
    XMPP_DOMAIN="example.com" \
    XMPP_HOST="127.0.0.1"

RUN /bin/bash -c "while apt-get update | tee >(cat 1>&2) | grep ^[WE]:; do echo apt-get update failed, retrying; sleep 1; done;"
RUN apt-get install -y \
    ant \
    alsa-base \
    alsa-utils \
    curl \
    --no-install-recommends && \
    curl -L https://github.com/docker-infra/reefer/releases/download/v0.0.4/reefer.gz | \
       gunzip >/usr/bin/reefer && \
    chmod a+x /usr/bin/reefer && \
    rm -rf /var/lib/apt/lists/* && \
    apt-get clean

COPY . /jitsi-hammer
WORKDIR /jitsi-hammer

RUN ant make

ENTRYPOINT [ "/usr/bin/reefer", \
    "-t", "templates/logging.properties.tmpl:/jitsi-hammer/lib/logging.properties", \
    "-E" \
]

CMD /etc/init.d/alsa-utils start && env && /jitsi-hammer/jitsi-hammer.sh \
    -XMPPdomain $XMPP_DOMAIN \
    -XMPPhost $HAMMER_XMPP_HOST \
    -port $PORT \
    -MUCdomain $MUC_PREFIX.$XMPP_DOMAIN \
    -allstats \
    -summarystats \
    -overallstats \
    -ivf /jitsi-hammer/resources/big-buck-bunny_trailer_track1_eng.ivf \
    -audiortpdump /jitsi-hammer/resources/hammer-opus.rtpdump \
    -room $ROOM_NAME \
    -interval $INTERVAL \
    -length $LENGTH \
    -users $USERS \
    -focususerjid $FOCUSUSERJID \
    -MUCvideobridge $MUC_VIDEOBRIDGE
