package pt.ulisboa.tecnico.socialsoftware.tutor.discussion.dto;

import pt.ulisboa.tecnico.socialsoftware.tutor.discussion.domain.Discussion;

import java.io.Serializable;

public class DiscussionDto implements Serializable {

    private Integer id;

    private String message;

    public DiscussionDto() {}

    public DiscussionDto(Discussion discussion) {
        this.id = discussion.getId();
        this.message = discussion.getMessage();
    }

    public String getMessage() {
        return message;
    }
}
