package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto;

import java.io.Serializable;

public class DiscussionStatsDto implements Serializable {
    private boolean areDiscussionsPublic;

    private Integer discussionsNumber;

    private Integer publicDiscussionsNumber;

    public Integer getDiscussionsNumber() { return discussionsNumber; }

    public void setDiscussionsNumber(Integer discussionsNumber) { this.discussionsNumber = discussionsNumber; }

    public Integer getPublicDiscussionsNumber() { return publicDiscussionsNumber; }

    public void setPublicDiscussionsNumber(Integer publicDiscussionsNumber) {
        this.publicDiscussionsNumber = publicDiscussionsNumber;
    }

    public boolean areDiscussionsPublic() {
        return areDiscussionsPublic;
    }

    public void setAreDiscussionsPublic(boolean areDiscussionsPublic) {
        this.areDiscussionsPublic = areDiscussionsPublic;
    }
}
