package pt.ulisboa.tecnico.socialsoftware.tutor.dashboard.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;
import pt.ulisboa.tecnico.socialsoftware.tutor.question.dto.QuestionDto;

import java.io.Serializable;

public class DiscussionStatsDto implements Serializable {
    private Integer discussionsNumber;

    private Integer publicDiscussionsNumber;

    public Integer getDiscussionsNumber() { return discussionsNumber; }

    public void setDiscussionsNumber(Integer discussionsNumber) { this.discussionsNumber = discussionsNumber; }

    public Integer getPublicDiscussionsNumber() { return publicDiscussionsNumber; }

    public void setPublicDiscussionsNumber(Integer publicDiscussionsNumber) {
        this.publicDiscussionsNumber = publicDiscussionsNumber;
    }
}
