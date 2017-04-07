package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

/**
 * This mimics jitsi-protocol-jabber/net/java/sip/communicator/impl/protocol/jabber/extensions/jingle/CandidateType.class.
 * We don't use the candidate types declared in ice4j because these are specifically listed in preferred order for
 * ease-of-comparison for prioritizing candidates
 */
public enum NewCandidateType {
    host,
    prflx,
    relay,
    srflx,
    stun,
    local;

    private NewCandidateType() {
    }
}