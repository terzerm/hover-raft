package org.tools4j.hoverraft.antonsroles;

public class RoleProvider {
    private final Role candidateRole;
    private final Role followerRole;
    private final Role leaderRole;

    public RoleProvider(final Role candidateRole,
                        final Role followerRole,
                        final Role leaderRole) {
        this.candidateRole = candidateRole;
        this.followerRole = followerRole;
        this.leaderRole = leaderRole;
    }

    public Role getCandidateRole() {
        return candidateRole;
    }

    public Role getFollowerRole() {
        return followerRole;
    }

    public Role getLeaderRole() {
        return leaderRole;
    }
}
