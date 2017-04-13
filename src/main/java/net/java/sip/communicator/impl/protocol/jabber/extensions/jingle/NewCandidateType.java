package net.java.sip.communicator.impl.protocol.jabber.extensions.jingle;

/**
 * Candidate type enum which list candidates in preferred order
 *
 * This mimics jitsi-protocol-jabber/net/java/sip/communicator/impl/protocol/jabber/extensions/jingle/CandidateType.class.
 * We don't use the candidate types declared in ice4j because these are specifically listed in preferred order for
 * ease-of-comparison for prioritizing candidates.  We don't use the existing one in jitsi-protocol-jabber because
 * this class was written as part of a migration to split out and update jitsi-protocol-jabber logic from the jitsi
 * repo
 *
 * @author Brian Baldino
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